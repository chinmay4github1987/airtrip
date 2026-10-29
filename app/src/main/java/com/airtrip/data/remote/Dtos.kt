package com.airtrip.data.remote

import com.airtrip.domain.*

data class PlaceDto(val latitude: Double, val longitude: Double, val aqi: Int, val name: String)
data class BookRequestDto(val a: PlaceDto, val b: PlaceDto)
data class BookDto(val id: String? = null, val a: PlaceDto, val b: PlaceDto, val price: Double)

data class GeoResponse(val localityInfo: LocalityInfo?)
data class LocalityInfo(val administrative: List<Admin> = emptyList())
data class Admin(val order: Int, val name: String)
data class AqiResponse(val status: String?, val data: AqiData?)
data class AqiData(val aqi: String?)   // WAQI returns "-" when no data, so parse leniently

fun Place.toDto() = PlaceDto(latitude, longitude, aqi, name)
fun PlaceDto.toDomain() = Place(latitude, longitude, aqi, name)
fun BookDto.toDomain() = Book(id, a.toDomain(), b.toDomain(), price)
