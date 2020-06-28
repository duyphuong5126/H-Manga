package nhdphuong.com.manga.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import nhdphuong.com.manga.Constants.Companion.TABLE_LAST_VISITED_PAGE
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.LAST_VISITED_PAGE

@Entity(
    tableName = TABLE_LAST_VISITED_PAGE,
    indices = [Index(value = [BOOK_ID])]
)
data class LastVisitedPage(
    @PrimaryKey @ColumnInfo(name = BOOK_ID) var bookId: String,
    @ColumnInfo(name = LAST_VISITED_PAGE) var lastVisitedPage: Int
)
