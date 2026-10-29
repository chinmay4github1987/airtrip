package com.airtrip.data.mock

import com.airtrip.data.remote.*
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.Calendar
import java.util.UUID
import kotlin.math.*

const val MOCK_HOST = "mock.airtrip.app"

/**
 * Intercepts calls to mock.airtrip.app and returns dynamically generated responses.
 * Business code only uses standard Retrofit interfaces and remains decoupled.
 */
class MockInterceptor(private val gson: Gson) : Interceptor {

    private data class StoredBook(
        val year: Int,
        val month: Int,
        val dto: BookDto
    )

    private val currentCal = Calendar.getInstance()
    private val defaultYear = currentCal.get(Calendar.YEAR)
    private val defaultMonth = currentCal.get(Calendar.MONTH) + 1

    private val store = mutableListOf<StoredBook>().apply {
        add(
            StoredBook(
                defaultYear, defaultMonth,
                BookDto("seed-1", PlaceDto(36.564, 127.001, 30, "Seoul A Location"), PlaceDto(36.567, 127.0, 40, "Seoul B Location"), 10000.0)
            )
        )
        add(
            StoredBook(
                defaultYear, defaultMonth,
                BookDto("seed-2", PlaceDto(36.577, 127.033, 50, "Seoul C Location"), PlaceDto(36.567, 127.0, 60, "Seoul D Location"), 20000.0)
            )
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        if (req.url.host != MOCK_HOST) return chain.proceed(req)

        val json = when {
            req.method == "POST" && req.url.encodedPath == "/books" -> {
                val buf = Buffer().also { req.body?.writeTo(it) }
                val body = gson.fromJson(buf.readUtf8(), BookRequestDto::class.java)

                val km = distanceKm(body.a, body.b)
                // Dynamic pricing formula: base 5,000 + ~1,250 KRW per km, rounded to nearest 500
                val calculatedPrice = max(5000.0, ((5000 + km * 1250) / 500).roundToInt() * 500.0)

                val now = Calendar.getInstance()
                val created = BookDto(
                    id = "book-${UUID.randomUUID().toString().take(8)}",
                    a = body.a,
                    b = body.b,
                    price = calculatedPrice
                )

                synchronized(store) {
                    store.add(0, StoredBook(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, created))
                }
                gson.toJson(created)
            }

            req.method == "GET" && req.url.encodedPath == "/books" -> {
                val yearParam = req.url.queryParameter("year")?.toIntOrNull() ?: defaultYear
                val monthParam = req.url.queryParameter("month")?.toIntOrNull() ?: defaultMonth

                val matching = synchronized(store) {
                    store.filter { it.year == yearParam && it.month == monthParam }.map { it.dto }
                }
                gson.toJson(matching)
            }

            else -> return respond(req, 404, """{"error":"not found"}""")
        }

        return respond(req, 200, json)
    }

    private fun respond(req: Request, code: Int, body: String) = Response.Builder()
        .request(req)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code == 200) "OK" else "ERR")
        .body(body.toResponseBody("application/json".toMediaType()))
        .build()

    private fun distanceKm(a: PlaceDto, b: PlaceDto): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
        return 6371 * 2 * asin(sqrt(h))
    }
}
