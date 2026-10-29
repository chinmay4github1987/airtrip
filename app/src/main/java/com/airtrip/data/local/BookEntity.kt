package com.airtrip.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.airtrip.domain.Book
import com.airtrip.domain.Place

/**
 * Room entity representing a completed booking record for persistent offline history.
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val year: Int,
    val month: Int,
    val aLat: Double,
    val aLng: Double,
    val aAqi: Int,
    val aName: String,
    val aNickname: String?,
    val bLat: Double,
    val bLng: Double,
    val bAqi: Int,
    val bName: String,
    val bNickname: String?,
    val price: Double,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Book = Book(
        id = id,
        a = Place(aLat, aLng, aAqi, aName, aNickname),
        b = Place(bLat, bLng, bAqi, bName, bNickname),
        price = price
    )

    companion object {
        fun fromDomain(book: Book, year: Int, month: Int): BookEntity = BookEntity(
            id = book.id!!,
            year = year,
            month = month,
            aLat = book.a.latitude,
            aLng = book.a.longitude,
            aAqi = book.a.aqi,
            aName = book.a.name,
            aNickname = book.a.nickname,
            bLat = book.b.latitude,
            bLng = book.b.longitude,
            bAqi = book.b.aqi,
            bName = book.b.name,
            bNickname = book.b.nickname,
            price = book.price
        )
    }
}
