package com.airtrip.data.repo

import com.airtrip.BuildConfig
import com.airtrip.data.local.AirTripDatabase
import com.airtrip.data.local.BookDao
import com.airtrip.data.local.BookEntity
import com.airtrip.data.local.CachedPlaceEntity
import com.airtrip.data.local.PlaceDao
import com.airtrip.data.remote.*
import com.airtrip.domain.*
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.*

@Singleton
class PlaceRepository @Inject constructor(
    private val geo: GeoApi,
    private val aqiApi: AqiApi,
    private val placeDao: PlaceDao
) {
    // In-memory cache for speed, synchronized with Room database
    private val memoryCache = ConcurrentHashMap<String, Place>()

    /**
     * Cache key truncates latitude and longitude to 3 decimal places.
     * E.g. 37.5642 and 37.5645 both hash to "37.564,127.001" (within ~100m).
     */
    fun key(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.3f,%.3f", lat, lng)

    suspend fun fetchAqi(lat: Double, lng: Double): Int = try {
        aqiApi.aqi(lat, lng, BuildConfig.WAQI_TOKEN).data?.aqi?.toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }

    /**
     * Resolves a coordinate to a Place with locality name and AQI.
     * Checks in-memory cache and Room DB first; if missing, hits reverse geocoding API.
     */
    suspend fun resolve(lat: Double, lng: Double): Place {
        val coordKey = key(lat, lng)

        // 1. Check memory or Room DB for existing cached name & nickname
        val cached = memoryCache[coordKey] ?: placeDao.getByKey(coordKey)?.toDomain()

        val name = cached?.name ?: addressName(geo.reverseGeocode(lat, lng))
        val currentAqi = fetchAqi(lat, lng)

        val place = Place(
            latitude = lat,
            longitude = lng,
            aqi = currentAqi,
            name = name,
            nickname = cached?.nickname
        )

        // 2. Persist in memory and Room
        memoryCache[coordKey] = place
        placeDao.insertOrUpdate(CachedPlaceEntity.fromDomain(place, coordKey))

        return place
    }

    suspend fun getCachedPlaces(): List<Place> {
        val dbPlaces = placeDao.getAll().map { it.toDomain() }
        if (dbPlaces.isNotEmpty()) {
            dbPlaces.forEach { memoryCache[key(it.latitude, it.longitude)] = it }
            return dbPlaces
        }
        return memoryCache.values.toList()
    }

    suspend fun saveNickname(place: Place, nickname: String?) {
        val coordKey = key(place.latitude, place.longitude)
        val updated = place.copy(nickname = nickname?.take(20)?.ifBlank { null })
        memoryCache[coordKey] = updated
        placeDao.insertOrUpdate(CachedPlaceEntity.fromDomain(updated, coordKey))
    }

    /**
     * Concatenates the name of the two entries with the highest `order` value in
     * localityInfo.administrative, sorted ascending by order.
     * Example: "Seocho District, Yangjae 2(i)-dong".
     */
    private fun addressName(r: GeoResponse): String {
        val adminList = r.localityInfo?.administrative.orEmpty()
        return adminList.sortedByDescending { it.order }
            .take(2)
            .sortedBy { it.order }
            .joinToString(", ") { it.name }
            .ifBlank {
                r.localityInfo?.administrative.orEmpty().firstOrNull()?.name
                    ?: "Selected Location"
            }
    }
}

@Singleton
class BookRepository @Inject constructor(
    private val api: BookApi,
    private val bookDao: BookDao
) {
    suspend fun book(a: Place, b: Place): Book {
        val domainBook = api.createBook(BookRequestDto(a.toDto(), b.toDto())).toDomain()
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1

        // Cache in Room DB
        bookDao.insert(BookEntity.fromDomain(domainBook, year, month))
        return domainBook
    }

    suspend fun history(year: Int, month: Int): List<Book> {
        return try {
            val remote = api.getBooks(year, month).map { it.toDomain() }
            val entities = remote.map { BookEntity.fromDomain(it, year, month) }
            bookDao.insertAll(entities)
            remote
        } catch (_: Exception) {
            // Fallback to local Room cache
            bookDao.getByYearMonth(year, month).map { it.toDomain() }
        }
    }
}
