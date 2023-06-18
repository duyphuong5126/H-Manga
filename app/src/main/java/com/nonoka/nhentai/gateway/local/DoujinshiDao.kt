package com.nonoka.nhentai.gateway.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.gateway.local.DoujinshiModel.Companion.IS_FAVORITE
import com.nonoka.nhentai.gateway.local.DoujinshiModel.Companion.LAST_READ_PAGE
import com.nonoka.nhentai.gateway.local.DoujinshiModel.Companion.TABLE_NAME

@Dao
interface DoujinshiDao {
    @Query("select * from $TABLE_NAME order by rowid desc limit :take offset :skip")
    suspend fun getDoujinshis(skip: Int, take: Int): List<DoujinshiModel>

    @Query("select count($ID) > 0 from $TABLE_NAME where $ID = :doujinshiId")
    suspend fun hasDoujinshi(doujinshiId: String): Boolean

    @Query("select count($ID) from $TABLE_NAME")
    suspend fun countDoujinshi(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDoujinshi(vararg doujinshiModels: DoujinshiModel): List<Long>

    @Query("update $TABLE_NAME set $LAST_READ_PAGE = :lastReadPage where $ID = :doujinshiId")
    suspend fun updateLastReadPage(doujinshiId: String, lastReadPage: Int? = null): Int

    @Query("update $TABLE_NAME set $IS_FAVORITE = :isFavorite where $ID = :doujinshiId")
    suspend fun updateFavoriteStatus(doujinshiId: String, isFavorite: Boolean): Int

    @Query("update $TABLE_NAME set $IS_FAVORITE = :isDownloaded where $ID = :doujinshiId")
    suspend fun updateDownloadedStatus(doujinshiId: String, isDownloaded: Boolean): Int
}