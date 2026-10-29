package com.airtrip.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedPlaceEntity::class, BookEntity::class], version = 1, exportSchema = false)
abstract class AirTripDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun bookDao(): BookDao
}
