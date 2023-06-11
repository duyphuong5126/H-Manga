package com.nonoka.nhentai.gateway.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nonoka.nhentai.domain.Resource
import com.nonoka.nhentai.domain.Resource.Error
import com.nonoka.nhentai.domain.Resource.Success
import com.nonoka.nhentai.domain.entity.GalleryPageNotExistException
import com.nonoka.nhentai.domain.entity.NHENTAI_HOME
import com.nonoka.nhentai.domain.entity.comment.Comment
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import com.nonoka.nhentai.domain.entity.doujinshi.RecommendedDoujinshis
import com.nonoka.nhentai.domain.entity.doujinshi.SortOption
import com.nonoka.nhentai.helper.ClientType
import com.nonoka.nhentai.helper.crawlerMap
import java.lang.reflect.Type
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber


interface DoujinshiRemoteSource {
    suspend fun loadDoujinshis(
        page: Int, filters: List<String>,
        sortOption: SortOption
    ): DoujinshisResult

    suspend fun loadDoujinshi(doujinshiId: String): Resource<Doujinshi>

    suspend fun getRecommendedDoujinshis(doujinshiId: String): Resource<List<Doujinshi>>

    suspend fun getComments(doujinshiId: String): Resource<List<Comment>>
}

class DoujinshiRemoteSourceImpl : DoujinshiRemoteSource {
    override suspend fun loadDoujinshis(
        page: Int,
        filters: List<String>,
        sortOption: SortOption
    ): DoujinshisResult {
        return suspendCancellableCoroutine { continuation ->
            val url = galleryUrl(page, filters, sortOption)
            crawlerMap[ClientType.Gallery]?.load(
                url = url, onDataReady = { responseUrl, data ->
                    Timber.d("Test>>> Response of url $responseUrl")
                    if (continuation.isActive) {
                        if (data.contains(notExistJson)) {
                            Timber.d("Test>>> Not exist")
                            continuation.resumeWith(Result.failure(GalleryPageNotExistException()))
                        } else {
                            Timber.d("Test>>> Exist")
                            continuation.resumeWith(
                                Result.success(
                                    Gson().fromJson(
                                        data,
                                        DoujinshisResult::class.java
                                    )
                                )
                            )
                        }
                    }
                }, onError = { _, error ->
                    Timber.d("Test>>> error=$error")
                    continuation.resumeWithException(Throwable(error))
                }
            )
        }
    }

    override suspend fun loadDoujinshi(doujinshiId: String): Resource<Doujinshi> {
        return try {
            suspendCoroutine { continuation ->
                val doujinshiUrl = buildDetailUrl(doujinshiId)
                Timber.d("Test>>> doujinshiUrl=$doujinshiUrl")
                crawlerMap[ClientType.Detail]?.load(
                    url = doujinshiUrl, onDataReady = { _, data ->
                        Timber.d("Test>>> doujinshi $doujinshiId - data=$data")
                        continuation.resumeWith(
                            Result.success(
                                Success(
                                    Gson().fromJson(
                                        data,
                                        Doujinshi::class.java
                                    ),
                                )
                            )
                        )
                    }, onError = { _, error ->
                        continuation.resumeWithException(Throwable(error))
                    }
                )
            }
        } catch (error: Throwable) {
            Error(error)
        }
    }

    override suspend fun getRecommendedDoujinshis(doujinshiId: String): Resource<List<Doujinshi>> {
        return try {
            suspendCoroutine { continuation ->
                val recommendationUrl = buildDetailRecommendationUrl(doujinshiId)
                crawlerMap[ClientType.Detail]?.load(
                    url = recommendationUrl, onDataReady = { _, data ->
                        val result = Gson().fromJson(
                            data,
                            RecommendedDoujinshis::class.java
                        ).doujinshiList
                        continuation.resumeWith(Result.success(Success(result)))
                    }, onError = { _, error ->
                        continuation.resumeWithException(Exception(error))
                    }
                )
            }
        } catch (error: Throwable) {
            Error(error)
        }
    }

    override suspend fun getComments(doujinshiId: String): Resource<List<Comment>> {
        return try {
            suspendCoroutine { continuation ->
                val recommendationUrl = buildCommentsUrl(doujinshiId)
                crawlerMap[ClientType.Comment]?.load(
                    url = recommendationUrl, onDataReady = { _, data ->
                        val listType: Type = object : TypeToken<List<Comment>>() {}.type
                        val result = Gson().fromJson<List<Comment>>(data, listType)
                        continuation.resumeWith(Result.success(Success(result)))
                    }, onError = { _, error ->
                        continuation.resumeWithException(Exception(error))
                    }
                )
            }
        } catch (error: Throwable) {
            Error(error)
        }
    }

    private fun galleryUrl(
        page: Int, filters: List<String>,
        sortOption: SortOption
    ): String {
        var sortString = ""
        if (sortOption == SortOption.PopularToday) {
            sortString = "&sort=popular-today"
        } else if (sortOption === SortOption.PopularWeek) {
            sortString = "&sort=popular-week"
        } else if (sortOption === SortOption.PopularAllTime) {
            sortString = "&sort=popular"
        }
        val url = if (filters.isNotEmpty()) {
            val searchContent = filters.joinToString("+") {
                it.replace(" ", "+")
            }
            "$NHENTAI_HOME/api/galleries/search?query=$searchContent&page=${page + 1}"
        } else "$NHENTAI_HOME/api/galleries/all?page=${page + 1}"
        return url + sortString
    }

    private fun buildDetailUrl(doujinshiId: String): String =
        "$NHENTAI_HOME/api/gallery/$doujinshiId"

    private fun buildDetailRecommendationUrl(doujinshiId: String): String =
        "$NHENTAI_HOME/api/gallery/$doujinshiId/related"

    private fun buildCommentsUrl(doujinshiId: String): String =
        "$NHENTAI_HOME/api/gallery/$doujinshiId/comments"

    companion object {
        private const val notExistJson = "{\"error\": \"does not exist\"}"
    }
}