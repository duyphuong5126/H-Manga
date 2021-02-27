package nhdphuong.com.manga.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import nhdphuong.com.manga.Constants.Companion.BOOK_ID

@Entity
data class BlockedBook(@PrimaryKey @ColumnInfo(name = BOOK_ID) val bookId: String)
