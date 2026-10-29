package com.airtrip.domain

data class Place(val latitude: Double, val longitude: Double, val aqi: Int, val name: String, val nickname: String? = null) {
    val displayName get() = nickname?.takeIf { it.isNotBlank() } ?: name
}
data class Book(val id: String?, val a: Place, val b: Place, val price: Double)
enum class Slot { A, B }
