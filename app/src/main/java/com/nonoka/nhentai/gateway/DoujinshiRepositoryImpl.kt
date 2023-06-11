package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.Resource
import com.nonoka.nhentai.domain.Resource.Success
import com.nonoka.nhentai.domain.entity.comment.Comment
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import com.nonoka.nhentai.domain.entity.doujinshi.SortOption
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import javax.inject.Inject

class DoujinshiRepositoryImpl @Inject constructor(
    private val doujinshiRemoteSource: DoujinshiRemoteSource
) : DoujinshiRepository {
    private val doujinshiCacheMap = HashMap<String, Doujinshi>()
    private val recommendedDoujinshisCacheMap = HashMap<String, List<Doujinshi>>()
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
                doujinshiCacheMap[it.id] = it
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

    override suspend fun getRecommendedDoujinshis(doujinshiId: String): Resource<List<Doujinshi>> {
        val recommendedList = recommendedDoujinshisCacheMap[doujinshiId]
        return if (recommendedList != null) {
            Success(recommendedList)
        } else {
            doujinshiRemoteSource.getRecommendedDoujinshis(doujinshiId).doOnSuccess { recommended ->
                recommendedDoujinshisCacheMap[doujinshiId] = recommended
                recommended.forEach {
                    doujinshiCacheMap[it.id] = it
                }
            }
        }
    }

    override suspend fun getComments(doujinshiId: String): Resource<List<Comment>> {
        return doujinshiRemoteSource.getComments(doujinshiId)
    }

    companion object {
        private const val CACHE_DURATION = 60000
    }
}