package com.airtrip.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE year = :year AND month = :month ORDER BY createdAt DESC")
    suspend fun getByYearMonth(year: Int, month: Int): List<BookEntity>

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<BookEntity>)

    @Query("DELETE FROM books")
    suspend fun clear()
}
