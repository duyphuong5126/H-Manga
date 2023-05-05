package com.nonoka.nhentai.domain

import com.nonoka.nhentai.domain.entity.book.DoujinshisResult

interface DoujinshiRepository {
    suspend fun getGalleryPage(page: Int, filters: List<String> = arrayListOf()): DoujinshisResult
}