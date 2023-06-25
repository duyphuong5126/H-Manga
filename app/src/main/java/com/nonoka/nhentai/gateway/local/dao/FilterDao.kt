package com.nonoka.nhentai.gateway.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.gateway.local.model.FilterModel
import com.nonoka.nhentai.gateway.local.model.FilterModel.Companion.TABLE_NAME
import com.nonoka.nhentai.gateway.local.model.FilterModel.Companion.IS_ACTIVE

@Dao
interface FilterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFilters(vararg filterModels: FilterModel): List<Long>

    @Query("select * from $TABLE_NAME where $IS_ACTIVE = 1 order by rowid")
    suspend fun getActiveFilters(): List<FilterModel>

    @Query("select count($ID) > 0 from $TABLE_NAME where $ID = :filter")
    suspend fun hasFilter(filter: String): Boolean

    @Query("update $TABLE_NAME set $IS_ACTIVE = :activeStatus where $ID = :filter")
    suspend fun updateFilterActiveStatus(filter: String, activeStatus: Int): Int
}