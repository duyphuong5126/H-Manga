package nhdphuong.com.manga.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_IMAGE as DOWNLOADED_IMAGE
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.IMAGE_TYPE
import nhdphuong.com.manga.Constants.Companion.IMAGE_WIDTH
import nhdphuong.com.manga.Constants.Companion.IMAGE_HEIGHT
import nhdphuong.com.manga.Constants.Companion.LOCAL_PATH
import nhdphuong.com.manga.Constants.Companion.TYPE

@Entity(
    tableName = DOWNLOADED_IMAGE,
    indices = [Index(value = [ID]), Index(value = [BOOK_ID])],
    foreignKeys = [ForeignKey(
        entity = DownloadedBookModel::class,
        parentColumns = [ID],
        childColumns = [BOOK_ID]
    )]
)
class BookImageModel(
    @ColumnInfo(name = BOOK_ID) var bookId: String,
    @ColumnInfo(name = IMAGE_TYPE) var imageType: String,
    @ColumnInfo(name = TYPE) var usageType: String,
    @ColumnInfo(name = IMAGE_WIDTH) var width: Int,
    @ColumnInfo(name = IMAGE_HEIGHT) var height: Int,
    @ColumnInfo(name = LOCAL_PATH) var localPath: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long = 0
}