package com.nonoka.nhentai.gateway.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class DoujinshiModel(
    @ColumnInfo(name = ID) @PrimaryKey val id: String,
    @ColumnInfo(name = JSON) val json: String,
    @ColumnInfo(name = LAST_READ_PAGE) val lastReadPage: Int?,
    @ColumnInfo(name = IS_FAVORITE) val isFavorite: Boolean,
    @ColumnInfo(name = IS_DOWNLOADED) val isDownloaded: Boolean,
) {
    companion object {
        const val TABLE_NAME = "doujinshi"
        const val JSON = "json"
        const val LAST_READ_PAGE = "last_read_page"
        const val IS_FAVORITE = "is_favorite"
        const val IS_DOWNLOADED = "is_downloaded"
    }
}