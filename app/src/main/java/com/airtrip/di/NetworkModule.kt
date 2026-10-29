package com.airtrip.di

import com.airtrip.data.mock.*
import com.airtrip.data.remote.*
import com.google.gson.Gson
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun gson() = Gson()
    @Provides @Singleton fun okHttp(gson: Gson) = OkHttpClient.Builder().addInterceptor(MockInterceptor(gson)).build()
    @Provides @Singleton fun retrofit(c: OkHttpClient, g: Gson): Retrofit = Retrofit.Builder()
        .baseUrl("https://$MOCK_HOST/").client(c).addConverterFactory(GsonConverterFactory.create(g)).build()
    @Provides @Singleton fun geoApi(r: Retrofit): GeoApi = r.create(GeoApi::class.java)
    @Provides @Singleton fun aqiApi(r: Retrofit): AqiApi = r.create(AqiApi::class.java)
    @Provides @Singleton fun bookApi(r: Retrofit): BookApi = r.create(BookApi::class.java)
}
