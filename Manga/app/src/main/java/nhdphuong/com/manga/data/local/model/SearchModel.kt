package nhdphuong.com.manga.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import nhdphuong.com.manga.Constants.Companion.TABLE_SEARCH
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.SEARCH_INFO
import nhdphuong.com.manga.Constants.Companion.SEARCH_TIMES

@Entity(
    tableName = TABLE_SEARCH,
    indices = [Index(value = [ID])]
)
data class SearchModel(
    @ColumnInfo(name = SEARCH_INFO) var searchInfo: String,
    @ColumnInfo(name = SEARCH_TIMES) var searchTimes: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long = 0
}
