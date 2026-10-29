package com.airtrip.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM cached_places WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): CachedPlaceEntity?

    @Query("SELECT * FROM cached_places ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<CachedPlaceEntity>>

    @Query("SELECT * FROM cached_places ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CachedPlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(place: CachedPlaceEntity)

    @Query("UPDATE cached_places SET nickname = :nickname WHERE `key` = :key")
    suspend fun updateNickname(key: String, nickname: String?)

    @Query("DELETE FROM cached_places")
    suspend fun clear()
}
