package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.Resource
import com.nonoka.nhentai.domain.Resource.Success
import com.nonoka.nhentai.domain.entity.book.Doujinshi
import com.nonoka.nhentai.domain.entity.book.DoujinshisResult
import com.nonoka.nhentai.domain.entity.book.SortOption
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import javax.inject.Inject

class DoujinshiRepositoryImpl @Inject constructor(
    private val doujinshiRemoteSource: DoujinshiRemoteSource
) : DoujinshiRepository {
    private val doujinshiCacheMap = HashMap<String, Doujinshi>()
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
            remoteResult.doujinshiList.forEach {
                doujinshiCacheMap[it.bookId] = it
            }
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

    override suspend fun getDoujinshi(doujinshiId: String): Resource<Doujinshi> {
        val doujinshi = doujinshiCacheMap[doujinshiId]
        return if (doujinshi != null) {
            Success(doujinshi)
        } else {
            doujinshiRemoteSource.loadDoujinshi(doujinshiId).doOnSuccess {
                doujinshiCacheMap[doujinshiId] = it
            }
        }
    }

    companion object {
        private const val CACHE_DURATION = 60000
    }
}