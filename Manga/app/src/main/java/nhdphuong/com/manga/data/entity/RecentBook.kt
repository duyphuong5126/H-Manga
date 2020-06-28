package nhdphuong.com.manga.data.entity

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.IS_FAVORITE
import nhdphuong.com.manga.Constants.Companion.CREATED_AT

/*
 * Created by nhdphuong on 6/8/18.
 */
@Entity
open class RecentBook(
    @PrimaryKey @ColumnInfo(name = BOOK_ID) var bookId: String,
    @ColumnInfo(name = IS_FAVORITE) var favorite: Int,
    @ColumnInfo(name = CREATED_AT) var createdAt: Long
) {

    constructor(bookId: String, isFavorite: Boolean, createdAt: Long) : this(
        bookId,
        if (isFavorite) 1 else 0,
        createdAt
    )

    @Ignore
    var isFavorite: Boolean = false
        set(value) {
            field = value
            favorite = if (value) 1 else 0
        }
        get() = favorite == 1
}
