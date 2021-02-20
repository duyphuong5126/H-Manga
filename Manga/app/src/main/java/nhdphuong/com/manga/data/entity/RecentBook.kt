package nhdphuong.com.manga.data.entity

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.Gson
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.CREATED_AT
import nhdphuong.com.manga.Constants.Companion.RAW_BOOK
import nhdphuong.com.manga.Constants.Companion.READING_TIMES
import nhdphuong.com.manga.data.entity.book.Book

/*
 * Created by nhdphuong on 6/8/18.
 */
@Entity
open class RecentBook(
    @PrimaryKey @ColumnInfo(name = BOOK_ID) var bookId: String,
    @ColumnInfo(name = CREATED_AT) var createdAt: Long,
    @ColumnInfo(name = RAW_BOOK) var _rawBook: String,
    @ColumnInfo(name = READING_TIMES) var readingTimes: Long
) {
    @Ignore
    val rawBook: Book? = try {
        Gson().fromJson(_rawBook, Book::class.java)
    } catch (throwable: Throwable) {
        null
    }
}
