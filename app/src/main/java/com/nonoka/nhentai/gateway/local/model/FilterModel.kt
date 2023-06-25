package com.nonoka.nhentai.gateway.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.gateway.local.model.FilterModel.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class FilterModel(
    @ColumnInfo(name = ID) @PrimaryKey val id: String,
    @ColumnInfo(name = IS_ACTIVE) val isActive: Boolean,
) {
    companion object {
        const val TABLE_NAME = "filter"
        const val IS_ACTIVE = "isActive"
    }
}