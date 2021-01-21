package nhdphuong.com.manga.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.data.entity.book.Book

@Entity
data class FavoriteBook(
    @PrimaryKey @ColumnInfo(name = Constants.BOOK_ID) var bookId: String,
    @ColumnInfo(name = Constants.CREATED_AT) var createdAt: Long,
    @ColumnInfo(name = Constants.RAW_BOOK) var _rawBook: String
) {
    @Ignore
    val rawBook: Book? = try {
        Gson().fromJson(_rawBook, Book::class.java)
    } catch (throwable: Throwable) {
        null
    }
}
