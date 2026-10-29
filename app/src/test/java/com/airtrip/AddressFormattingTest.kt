package com.airtrip

import com.airtrip.data.remote.Admin
import com.airtrip.data.remote.GeoResponse
import com.airtrip.data.remote.LocalityInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AddressFormattingTest {

    /**
     * Requirement:
     * When resolving an address from coordinates, use the response's
     * localityInfo -> administrative and concatenate the name of the two entries
     * with the highest order value.
     *
     * Example given in spec:
     * Seocho District (order 4) and Yangjae 2(i)-dong (order 5)
     * should format as: "Seocho District, Yangjae 2(i)-dong"
     */
    @Test
    fun testAdministrativeOrderFormatting() {
        val geoResponse = GeoResponse(
            localityInfo = LocalityInfo(
                administrative = listOf(
                    Admin(order = 2, name = "South Korea"),
                    Admin(order = 3, name = "Seoul"),
                    Admin(order = 4, name = "Seocho District"),
                    Admin(order = 5, name = "Yangjae 2(i)-dong")
                )
            )
        )

        val result = formatAddress(geoResponse)
        assertEquals("Seocho District, Yangjae 2(i)-dong", result)
    }

    private fun formatAddress(r: GeoResponse): String {
        val adminList = r.localityInfo?.administrative.orEmpty()
        return adminList.sortedByDescending { it.order }
            .take(2)
            .sortedBy { it.order }
            .joinToString(", ") { it.name }
            .ifBlank { "Selected Location" }
    }

    /**
     * Optional requirement:
     * Cache previously searched locations locally by coordinates.
     * Consider two locations the same if latitude and longitude match up to the third decimal place.
     * Example: 37.5642, 127.0016 is considered the same as 37.5645, 127.0018
     */
    @Test
    fun test3DecimalCoordinateEquivalence() {
        val key1 = formatKey(37.5642, 127.0016)
        val key2 = formatKey(37.5645, 127.0018)
        val keyDifferent = formatKey(37.5671, 127.0016)

        assertEquals("37.564,127.002", key1)
        assertEquals("37.565,127.002", key2)

        // Or truncation key
        val truncKey1 = formatTruncatedKey(37.5642, 127.0016)
        val truncKey2 = formatTruncatedKey(37.5645, 127.0018)
        assertEquals("37.564,127.001", truncKey1)
        assertEquals("37.564,127.001", truncKey2)
        assertEquals(truncKey1, truncKey2)
    }

    private fun formatKey(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.3f,%.3f", lat, lng)

    private fun formatTruncatedKey(lat: Double, lng: Double): String {
        val latT = (lat * 1000).toLong() / 1000.0
        val lngT = (lng * 1000).toLong() / 1000.0
        return String.format(Locale.US, "%.3f,%.3f", latT, lngT)
    }
}
