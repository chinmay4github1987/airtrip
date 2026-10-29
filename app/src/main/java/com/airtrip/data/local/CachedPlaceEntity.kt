package com.airtrip.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.airtrip.domain.Place

/**
 * Room entity for caching places resolved by reverse geocoding & AQI lookup.
 * Coordinates are keyed to 3 decimal places (e.g., "37.564,127.001")
 * so nearby taps within ~100m share the cached locality name.
 */
@Entity(tableName = "cached_places")
data class CachedPlaceEntity(
    @PrimaryKey val key: String,
    val latitude: Double,
    val longitude: Double,
    val aqi: Int,
    val name: String,
    val nickname: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Place = Place(
        latitude = latitude,
        longitude = longitude,
        aqi = aqi,
        name = name,
        nickname = nickname
    )

    companion object {
        fun fromDomain(place: Place, key: String): CachedPlaceEntity =
            CachedPlaceEntity(
                key = key,
                latitude = place.latitude,
                longitude = place.longitude,
                aqi = place.aqi,
                name = place.name,
                nickname = place.nickname
            )
    }
}
