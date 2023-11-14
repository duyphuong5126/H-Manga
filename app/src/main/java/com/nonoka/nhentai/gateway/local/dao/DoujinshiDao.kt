package com.nonoka.nhentai.gateway.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel.Companion.IS_DOWNLOADED
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel.Companion.IS_FAVORITE
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel.Companion.LAST_READ_PAGE
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel.Companion.TABLE_NAME

@Dao
interface DoujinshiDao {
    @Query("select * from $TABLE_NAME where $LAST_READ_PAGE is not null or $IS_FAVORITE = 1 or $IS_DOWNLOADED = 1 order by rowid desc limit :take offset :skip")
    suspend fun getCollectedDoujinshis(skip: Int, take: Int): List<DoujinshiModel>

    @Query("select count($ID) > 0 from $TABLE_NAME where $ID = :doujinshiId")
    suspend fun hasDoujinshi(doujinshiId: String): Boolean

    @Query("select count($ID) from $TABLE_NAME where $LAST_READ_PAGE is not null or $IS_FAVORITE = 1 or $IS_DOWNLOADED = 1")
    suspend fun countCollectedDoujinshis(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDoujinshi(vararg doujinshiModels: DoujinshiModel): List<Long>

    @Query("update $TABLE_NAME set $LAST_READ_PAGE = :lastReadPage where $ID = :doujinshiId")
    suspend fun updateLastReadPage(doujinshiId: String, lastReadPage: Int? = null): Int

    @Query("select $LAST_READ_PAGE from $TABLE_NAME where $ID = :doujinshiId")
    suspend fun getLastReadPage(doujinshiId: String): Int?

    @Query("update $TABLE_NAME set $IS_FAVORITE = :isFavorite where $ID = :doujinshiId")
    suspend fun updateFavoriteStatus(doujinshiId: String, isFavorite: Boolean): Int

    @Query("select $IS_FAVORITE from $TABLE_NAME where $ID = :doujinshiId")
    suspend fun getFavoriteStatus(doujinshiId: String): Boolean

    @Query("update $TABLE_NAME set $IS_DOWNLOADED = :isDownloaded where $ID = :doujinshiId")
    suspend fun updateDownloadedStatus(doujinshiId: String, isDownloaded: Boolean): Int

    @Query("select $IS_DOWNLOADED from $TABLE_NAME where $ID = :doujinshiId")
    suspend fun getDownloadedStatus(doujinshiId: String): Boolean
}