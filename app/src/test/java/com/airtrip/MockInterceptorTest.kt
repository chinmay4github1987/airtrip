package com.airtrip

import com.airtrip.data.mock.MockInterceptor
import com.airtrip.data.remote.BookDto
import com.airtrip.data.remote.BookRequestDto
import com.airtrip.data.remote.PlaceDto
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MockInterceptorTest {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .addInterceptor(MockInterceptor(gson))
        .build()

    @Test
    fun testCreateBookReturnsDynamicPrice() {
        val reqDto = BookRequestDto(
            a = PlaceDto(37.5665, 126.9780, 40, "Seoul City Hall"),
            b = PlaceDto(37.4980, 127.0276, 55, "Gangnam Station")
        )
        val bodyJson = gson.toJson(reqDto)

        val request = Request.Builder()
            .url("https://mock.airtrip.app/books")
            .post(bodyJson.toRequestBody())
            .build()

        val response = client.newCall(request).execute()
        assertTrue(response.isSuccessful)

        val resJson = response.body?.string()
        assertNotNull(resJson)

        val created = gson.fromJson(resJson, BookDto::class.java)
        assertEquals("Seoul City Hall", created.a.name)
        assertEquals("Gangnam Station", created.b.name)
        assertTrue("Price should be dynamically computed > 5000", created.price >= 5000.0)
    }

    @Test
    fun testGetBooksHistoryWithYearMonth() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1

        val request = Request.Builder()
            .url("https://mock.airtrip.app/books?year=$year&month=$month")
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertTrue(response.isSuccessful)

        val resJson = response.body?.string()
        val list = gson.fromJson(resJson, Array<BookDto>::class.java)
        assertNotNull(list)
        assertTrue("Should return seeded bookings", list.isNotEmpty())
    }
}
