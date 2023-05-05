package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.book.DoujinshisResult
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import javax.inject.Inject

class DoujinshiRepositoryImpl @Inject constructor(
    private val doujinshiRemoteSource: DoujinshiRemoteSource
) : DoujinshiRepository {
    private val galleryCacheMap = HashMap<Int, Pair<DoujinshisResult, Long>>()
    override suspend fun getGalleryPage(page: Int): DoujinshisResult {
        val cacheResult = galleryCacheMap[page]
        return if (cacheResult != null && System.currentTimeMillis() - cacheResult.second < CACHE_DURATION) {
            cacheResult.first
        } else {
            val remoteResult = doujinshiRemoteSource.loadDoujinshis(page)
            galleryCacheMap[page] = Pair(remoteResult, System.currentTimeMillis())
            remoteResult
        }
    }

    companion object {
        private const val CACHE_DURATION = 60000
    }
}