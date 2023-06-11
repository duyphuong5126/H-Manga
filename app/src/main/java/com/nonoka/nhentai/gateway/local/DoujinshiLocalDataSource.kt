package com.nonoka.nhentai.gateway.local

import com.google.gson.Gson
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import javax.inject.Inject

interface DoujinshiLocalDataSource {
    suspend fun getDoujinshiCount(): Long

    suspend fun getDoujinshis(skip: Int, take: Int): List<Doujinshi>

    suspend fun setReadDoujinshi(doujinshi: Doujinshi, lastReadPage: Int?): Boolean

    suspend fun setFavoriteDoujinshi(doujinshi: Doujinshi, isFavorite: Boolean): Boolean

    suspend fun setDownloadedDoujinshi(doujinshi: Doujinshi, isDownloaded: Boolean): Boolean
}

class DoujinshiLocalDataSourceImpl @Inject constructor(
    private val doujinshiDao: DoujinshiDao
) : DoujinshiLocalDataSource {

    override suspend fun getDoujinshiCount(): Long {
        return doujinshiDao.countDoujinshi()
    }

    override suspend fun getDoujinshis(skip: Int, take: Int): List<Doujinshi> {
        return doujinshiDao.getDoujinshis(skip, take).map {
            Gson().fromJson(it.json, Doujinshi::class.java)
        }
    }

    override suspend fun setReadDoujinshi(doujinshi: Doujinshi, lastReadPage: Int?): Boolean {
        return if (doujinshiDao.hasDoujinshi(doujinshi.id)) {
            doujinshiDao.updateLastReadPage(doujinshi.id, lastReadPage) > 0
        } else {
            doujinshiDao.addDoujinshi(
                DoujinshiModel(
                    id = doujinshi.id,
                    json = Gson().toJson(doujinshi, Doujinshi::class.java),
                    lastReadPage = lastReadPage,
                    isFavorite = false,
                    isDownloaded = false
                ),
            ).isNotEmpty()
        }
    }

    override suspend fun setFavoriteDoujinshi(doujinshi: Doujinshi, isFavorite: Boolean): Boolean {
        return if (doujinshiDao.hasDoujinshi(doujinshi.id)) {
            doujinshiDao.updateFavoriteStatus(doujinshi.id, isFavorite) > 0
        } else {
            doujinshiDao.addDoujinshi(
                DoujinshiModel(
                    id = doujinshi.id,
                    json = Gson().toJson(Doujinshi::class.java),
                    lastReadPage = null,
                    isFavorite = isFavorite,
                    isDownloaded = false
                ),
            ).isNotEmpty()
        }
    }

    override suspend fun setDownloadedDoujinshi(
        doujinshi: Doujinshi,
        isDownloaded: Boolean
    ): Boolean {
        return if (doujinshiDao.hasDoujinshi(doujinshi.id)) {
            doujinshiDao.updateDownloadedStatus(doujinshi.id, isDownloaded) > 0
        } else {
            doujinshiDao.addDoujinshi(
                DoujinshiModel(
                    id = doujinshi.id,
                    json = Gson().toJson(Doujinshi::class.java),
                    lastReadPage = null,
                    isFavorite = false,
                    isDownloaded = isDownloaded
                ),
            ).isNotEmpty()
        }
    }

}