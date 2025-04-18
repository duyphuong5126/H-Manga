package com.nonoka.nhentai.gateway

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.Resource
import com.nonoka.nhentai.domain.Resource.Success
import com.nonoka.nhentai.domain.Resource.Error
import com.nonoka.nhentai.domain.entity.comment.Comment
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import com.nonoka.nhentai.domain.entity.doujinshi.SortOption
import com.nonoka.nhentai.gateway.local.datasource.DoujinshiLocalDataSource
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import javax.inject.Inject
import kotlin.random.Random
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

    override suspend fun getRandomDoujinshi(noFilterPageCount: Int?): Resource<Doujinshi> {
        return try {
            val numOfPages = noFilterPageCount ?: remoteSource.loadDoujinshis(
                0,
                emptyList(),
                SortOption.Recent
            ).numOfPages.toInt()
            val randomPage = Random.nextInt(0, numOfPages)
            Timber.d("Get doujinshi from the random page index $randomPage")
            Success(
                remoteSource.loadDoujinshis(
                    randomPage,
                    emptyList(),
                    SortOption.Recent
                ).doujinshiList.random()
            )
        } catch (error: Throwable) {
            Error(error)
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

    override suspend fun getCollectionSize(): Long {
        return localDataSource.getCollectedDoujinshiCount()
    }

    override suspend fun getCollectionPage(page: Int): DoujinshisResult {
        val doujinshiList = localDataSource.getCollectedDoujinshis(PAGE_SIZE * page, PAGE_SIZE)
        val total = localDataSource.getCollectedDoujinshiCount()
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

    override suspend fun isDoujinshiDownloaded(doujinshiId: String): Resource<Boolean> {
        return try {
            Success(localDataSource.getDownloadedStatus(doujinshiId))
        } catch (e: Throwable) {
            Error(e)
        }
    }

    override suspend fun getDownloadedDoujinshis(): List<String> {
        return localDataSource.getDownloadedDoujinshis()
    }

    override suspend fun getFavoriteDoujinshis(): List<String> {
        return localDataSource.getFavoriteDoujinshis()
    }

    override suspend fun getReadDoujinshis(): List<String> {
        return localDataSource.getReadDoujinshis()
    }

    override suspend fun setFavoriteDoujinshi(
        doujinshi: Doujinshi,
        isFavorite: Boolean
    ): Resource<Boolean> {
        return try {
            Success(localDataSource.setFavoriteDoujinshi(doujinshi, isFavorite))
        } catch (error: Throwable) {
            Error(error)
        }
    }

    override suspend fun getFavoriteStatus(doujinshiId: String): Resource<Boolean> {
        return localDataSource.getFavoriteStatus(doujinshiId)
    }

    override suspend fun setReadDoujinshi(doujinshi: Doujinshi, lastReadPage: Int?): Boolean {
        return localDataSource.setReadDoujinshi(doujinshi, lastReadPage)
    }

    override suspend fun getLastReadPageIndex(doujinshiId: String): Resource<Int> {
        return localDataSource.getLastReadPageIndex(doujinshiId)
    }

    companion object {
        private const val CACHE_DURATION = 60000

        private const val PAGE_SIZE = 25
    }
}