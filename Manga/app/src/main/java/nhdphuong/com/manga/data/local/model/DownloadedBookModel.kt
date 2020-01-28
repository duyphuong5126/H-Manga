package nhdphuong.com.manga.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_BOOK as DOWNLOADED_BOOK
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.MEDIA_ID
import nhdphuong.com.manga.Constants.Companion.TITLE_ENG
import nhdphuong.com.manga.Constants.Companion.TITLE_JAPANESE
import nhdphuong.com.manga.Constants.Companion.TITLE_PRETTY
import nhdphuong.com.manga.Constants.Companion.SCANLATOR
import nhdphuong.com.manga.Constants.Companion.UPLOAD_DATE
import nhdphuong.com.manga.Constants.Companion.NUM_PAGES
import nhdphuong.com.manga.Constants.Companion.NUM_FAVORITES

@Entity(
    tableName = DOWNLOADED_BOOK,
    indices = [Index(value = [ID])]
)
data class DownloadedBookModel(
    @PrimaryKey @ColumnInfo(name = ID) var bookId: String,
    @ColumnInfo(name = MEDIA_ID) var mediaId: String,
    @ColumnInfo(name = TITLE_ENG) var titleEng: String,
    @ColumnInfo(name = TITLE_JAPANESE) var titleJapanese: String,
    @ColumnInfo(name = TITLE_PRETTY) var titlePretty: String,
    @ColumnInfo(name = SCANLATOR) var scanlator: String,
    @ColumnInfo(name = UPLOAD_DATE) var uploadDate: Long,
    @ColumnInfo(name = NUM_PAGES) var numOfPages: Int,
    @ColumnInfo(name = NUM_FAVORITES) var numOfFavorites: Int
)