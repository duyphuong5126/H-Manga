package nhdphuong.com.manga.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.RAW_BOOK
import nhdphuong.com.manga.Constants.Companion.TABLE_PENDING_DOWNLOAD_BOOK
import nhdphuong.com.manga.Constants.Companion.TITLE_PRETTY

@Entity(
    tableName = TABLE_PENDING_DOWNLOAD_BOOK,
    indices = [Index(value = [BOOK_ID])]
)
data class PendingDownloadBook(
    @PrimaryKey @ColumnInfo(name = BOOK_ID) var bookId: String,
    @ColumnInfo(name = TITLE_PRETTY) var titlePretty: String,
    @ColumnInfo(name = RAW_BOOK) var rawData: String
)
