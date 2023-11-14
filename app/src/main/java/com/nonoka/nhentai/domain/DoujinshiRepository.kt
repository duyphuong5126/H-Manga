package com.nonoka.nhentai.domain

import com.nonoka.nhentai.domain.entity.comment.Comment
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import com.nonoka.nhentai.domain.entity.doujinshi.SortOption

interface DoujinshiRepository {
    suspend fun getGalleryPage(
        page: Int,
        filters: List<String> = arrayListOf(),
        sortOption: SortOption = SortOption.Recent
    ): DoujinshisResult

    suspend fun getDoujinshi(doujinshiId: String): Resource<Doujinshi>

    suspend fun getRecommendedDoujinshis(doujinshiId: String): Resource<List<Doujinshi>>

    suspend fun getComments(doujinshiId: String): Resource<List<Comment>>

    suspend fun getCollectionPage(
        page: Int,
    ): DoujinshisResult

    suspend fun getCollectionSize(): Long

    suspend fun setReadDoujinshi(doujinshi: Doujinshi, lastReadPage: Int?): Boolean

    suspend fun getLastReadPageIndex(doujinshiId: String): Resource<Int>

    suspend fun setFavoriteDoujinshi(doujinshi: Doujinshi, isFavorite: Boolean): Resource<Boolean>

    suspend fun getFavoriteStatus(doujinshiId: String): Resource<Boolean>

    suspend fun setDownloadedDoujinshi(doujinshi: Doujinshi, isDownloaded: Boolean): Boolean

    suspend fun isDoujinshiDownloaded(doujinshiId: String): Resource<Boolean>
}