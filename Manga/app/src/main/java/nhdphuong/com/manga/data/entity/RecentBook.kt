package nhdphuong.com.manga.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import nhdphuong.com.manga.Constants

/*
 * Created by nhdphuong on 6/8/18.
 */
@Entity
open class RecentBook(
        @PrimaryKey @ColumnInfo(name = Constants.BOOK_ID) var bookId: String,
        @ColumnInfo(name = Constants.IS_FAVORITE) var mIsFavorite: Int
) {

    constructor(bookId: String, isFavorite: Boolean) : this(bookId, if (isFavorite) 1 else 0)

    @Ignore
    var favorite: Boolean = false
        set(value) {
            field = value
            mIsFavorite = if (value) 1 else 0
        }
        get() = mIsFavorite == 1
}
