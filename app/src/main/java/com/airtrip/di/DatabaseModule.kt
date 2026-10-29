package com.airtrip.di

import android.content.Context
import androidx.room.Room
import com.airtrip.data.local.AirTripDatabase
import com.airtrip.data.local.BookDao
import com.airtrip.data.local.PlaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAirTripDatabase(@ApplicationContext context: Context): AirTripDatabase =
        Room.databaseBuilder(
            context,
            AirTripDatabase::class.java,
            "airtrip.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePlaceDao(db: AirTripDatabase): PlaceDao = db.placeDao()

    @Provides
    fun provideBookDao(db: AirTripDatabase): BookDao = db.bookDao()
}
