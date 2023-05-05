package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.book.DoujinshisResult
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import javax.inject.Inject

class DoujinshiRepositoryImpl @Inject constructor(
    private val doujinshiRemoteSource: DoujinshiRemoteSource
) : DoujinshiRepository {
    private val galleryCacheMap = HashMap<Int, Pair<DoujinshisResult, Long>>()
    private var filterString = ""
    override suspend fun getGalleryPage(page: Int, filters: List<String>): DoujinshisResult {
        val searchContent = filters.joinToString("+") {
            it.replace(" ", "+")
        }
        if (filterString != searchContent) {
            filterString = searchContent
            galleryCacheMap.clear()
        }
        val cacheResult = galleryCacheMap[page]
        return if (cacheResult != null && System.currentTimeMillis() - cacheResult.second < CACHE_DURATION) {
            cacheResult.first
        } else {
            val remoteResult = doujinshiRemoteSource.loadDoujinshis(page, filters)
            galleryCacheMap[page] = Pair(remoteResult, System.currentTimeMillis())
            remoteResult
        }
    }

    companion object {
        private const val CACHE_DURATION = 60000
    }
}