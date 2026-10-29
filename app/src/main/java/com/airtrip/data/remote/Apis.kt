package com.airtrip.data.remote

import retrofit2.http.*

/** Real API #1: reverse geocoding (BigDataCloud, no key needed). Absolute URL overrides Retrofit base URL. */
interface GeoApi {
    @GET("https://api.bigdatacloud.net/data/reverse-geocode-client")
    suspend fun reverseGeocode(
        @Query("latitude") lat: Double, @Query("longitude") lng: Double,
        @Query("localityLanguage") lang: String = "en"
    ): GeoResponse
}

interface AqiApi {
    @GET("https://api.waqi.info/feed/geo:{lat};{lng}/")
    suspend fun aqi(@Path("lat") lat: Double, @Path("lng") lng: Double, @Query("token") token: String): AqiResponse
}


interface BookApi {
    @POST("books") suspend fun createBook(@Body body: BookRequestDto): BookDto
    @GET("books") suspend fun getBooks(@Query("year") year: Int, @Query("month") month: Int): List<BookDto>
}
