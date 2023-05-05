package com.nonoka.nhentai.gateway.remote

import com.google.gson.Gson
import com.nonoka.nhentai.domain.entity.book.DoujinshisResult
import com.nonoka.nhentai.helper.ClientType
import com.nonoka.nhentai.helper.crawlerMap
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import timber.log.Timber

interface DoujinshiRemoteSource {
    suspend fun loadDoujinshis(page: Int, filters: List<String>): DoujinshisResult
}

class DoujinshiRemoteSourceImpl : DoujinshiRemoteSource {
    override suspend fun loadDoujinshis(page: Int, filters: List<String>): DoujinshisResult {
        return suspendCoroutine { continuation ->
            val url = galleryUrl(page, filters)
            Timber.tag("GalleryLoading>>>")
                .d("url=$url")
            crawlerMap[ClientType.Gallery]?.load(
                url = url, onDataReady = { _, data ->
                    Timber.tag("GalleryLoading>>>").d("Data=$data")
                    continuation.resumeWith(
                        Result.success(
                            Gson().fromJson(
                                data,
                                DoujinshisResult::class.java
                            )
                        )
                    )
                }, onError = { _, error ->
                    Timber.tag("GalleryLoading>>>").d("Error=$error")
                    continuation.resumeWithException(Throwable(error))
                }
            )
        }
    }

    private fun galleryUrl(page: Int, filters: List<String>): String {
        return if (filters.isNotEmpty()) {
            val searchContent = filters.joinToString("+") {
                it.replace(" ", "+")
            }
            "https://nhentai.net/api/galleries/search?query=$searchContent&page=${page + 1}"
        } else "https://nhentai.net/api/galleries/all?page=${page + 1}"
    }
}