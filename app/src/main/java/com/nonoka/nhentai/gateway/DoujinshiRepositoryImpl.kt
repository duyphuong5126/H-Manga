package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.book.DoujinshisResult
import com.nonoka.nhentai.domain.entity.book.SortOption
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import javax.inject.Inject

class DoujinshiRepositoryImpl @Inject constructor(
    private val doujinshiRemoteSource: DoujinshiRemoteSource
) : DoujinshiRepository {
    private val galleryCacheMap = HashMap<Int, Pair<DoujinshisResult, Long>>()
    private var filterString = ""
    override suspend fun getGalleryPage(
        page: Int,
        filters: List<String>,
        sortOption: SortOption
    ): DoujinshisResult {
        val filterContent = filterContent(filters, sortOption)
        if (filterString != filterContent) {
            filterString = filterContent
            galleryCacheMap.clear()
        }
        val cacheResult = galleryCacheMap[page]
        return if (cacheResult != null && System.currentTimeMillis() - cacheResult.second < CACHE_DURATION) {
            cacheResult.first
        } else {
            val remoteResult = doujinshiRemoteSource.loadDoujinshis(page, filters, sortOption)
            galleryCacheMap[page] = Pair(remoteResult, System.currentTimeMillis())
            remoteResult
        }
    }

    private fun filterContent(filters: List<String>, sortOption: SortOption): String {
        var sortString = ""
        if (sortOption == SortOption.PopularToday) {
            sortString = "&sort=popular-today"
        } else if (sortOption === SortOption.PopularWeek) {
            sortString = "&sort=popular-week"
        } else if (sortOption === SortOption.PopularAllTime) {
            sortString = "&sort=popular"
        }
        val content = filters.joinToString("+") {
            it.replace(" ", "+")
        }
        return content + sortString
    }

    companion object {
        private const val CACHE_DURATION = 60000
    }
}