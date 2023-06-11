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
}