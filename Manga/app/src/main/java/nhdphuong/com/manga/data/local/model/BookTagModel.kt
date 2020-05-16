package nhdphuong.com.manga.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import nhdphuong.com.manga.Constants.Companion.TABLE_BOOK_TAG as BOOK_TAG
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.TAG_ID
import nhdphuong.com.manga.data.entity.book.tags.Tag

@Entity(
    tableName = BOOK_TAG,
    indices = [Index(value = [BOOK_ID]), Index(value = [TAG_ID])],
    primaryKeys = [BOOK_ID, TAG_ID],
    foreignKeys = [ForeignKey(
        entity = DownloadedBookModel::class,
        parentColumns = [ID],
        childColumns = [BOOK_ID],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Tag::class,
        parentColumns = [ID],
        childColumns = [TAG_ID],
        onDelete = ForeignKey.CASCADE
    )]
)
class BookTagModel(
    @ColumnInfo(name = BOOK_ID) var bookId: String,
    @ColumnInfo(name = TAG_ID) var tagId: Long
)
