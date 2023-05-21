package com.nonoka.nhentai.domain

import com.nonoka.nhentai.domain.entity.book.Doujinshi
import com.nonoka.nhentai.domain.entity.book.DoujinshisResult
import com.nonoka.nhentai.domain.entity.book.SortOption

interface DoujinshiRepository {
    suspend fun getGalleryPage(
        page: Int,
        filters: List<String> = arrayListOf(),
        sortOption: SortOption = SortOption.Recent
    ): DoujinshisResult

    suspend fun getDoujinshi(doujinshiId: String): Resource<Doujinshi>

    suspend fun getRecommendedDoujinshis(doujinshiId: String): Resource<List<Doujinshi>>
}