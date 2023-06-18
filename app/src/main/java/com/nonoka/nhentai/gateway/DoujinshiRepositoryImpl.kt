package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.Resource
import com.nonoka.nhentai.domain.Resource.Success
import com.nonoka.nhentai.domain.entity.comment.Comment
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import com.nonoka.nhentai.domain.entity.doujinshi.SortOption
import com.nonoka.nhentai.gateway.local.DoujinshiLocalDataSource
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import javax.inject.Inject
import timber.log.Timber

class DoujinshiRepositoryImpl @Inject constructor(
    private val remoteSource: DoujinshiRemoteSource,
    private val localDataSource: DoujinshiLocalDataSource
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
            Timber.d("Gallery>>> Use cache")
            cacheResult.first
        } else {
            Timber.d("Gallery>>> Not use cache")
            val remoteResult = remoteSource.loadDoujinshis(page, filters, sortOption)
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
            remoteSource.loadDoujinshi(doujinshiId).doOnSuccess {
                doujinshiCacheMap[doujinshiId] = it
            }
        }
    }

    override suspend fun getRecommendedDoujinshis(doujinshiId: String): Resource<List<Doujinshi>> {
        val recommendedList = recommendedDoujinshisCacheMap[doujinshiId]
        return if (recommendedList != null) {
            Success(recommendedList)
        } else {
            remoteSource.getRecommendedDoujinshis(doujinshiId).doOnSuccess { recommended ->
                recommendedDoujinshisCacheMap[doujinshiId] = recommended
                recommended.forEach {
                    doujinshiCacheMap[it.id] = it
                }
            }
        }
    }

    override suspend fun getComments(doujinshiId: String): Resource<List<Comment>> {
        return remoteSource.getComments(doujinshiId)
    }

    override suspend fun getDoujinshiCount(): Long {
        return localDataSource.getDoujinshiCount()
    }

    override suspend fun getCollectionPage(page: Int): DoujinshisResult {
        val doujinshiList = localDataSource.getDoujinshis(PAGE_SIZE * page, PAGE_SIZE)
        val total = localDataSource.getDoujinshiCount()
        var numOfPages = total / PAGE_SIZE
        if (total % PAGE_SIZE > 0) {
            numOfPages++
        }
        doujinshiList.forEach {
            doujinshiCacheMap[it.id] = it
        }
        return DoujinshisResult(
            doujinshiList = doujinshiList,
            numOfPages = numOfPages,
            numOfBooksPerPage = PAGE_SIZE
        )
    }

    override suspend fun setDownloadedDoujinshi(
        doujinshi: Doujinshi,
        isDownloaded: Boolean
    ): Boolean {
        return localDataSource.setDownloadedDoujinshi(doujinshi, isDownloaded)
    }

    override suspend fun setFavoriteDoujinshi(doujinshi: Doujinshi, isFavorite: Boolean): Boolean {
        return localDataSource.setFavoriteDoujinshi(doujinshi, isFavorite)
    }

    override suspend fun setReadDoujinshi(doujinshi: Doujinshi, lastReadPage: Int?): Boolean {
        return localDataSource.setReadDoujinshi(doujinshi, lastReadPage)
    }

    companion object {
        private const val CACHE_DURATION = 60000

        private const val PAGE_SIZE = 25
    }
}