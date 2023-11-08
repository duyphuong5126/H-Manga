package com.nonoka.nhentai.gateway.local.datasource

import com.google.gson.Gson
import com.nonoka.nhentai.domain.Resource
import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi
import com.nonoka.nhentai.gateway.local.dao.DoujinshiDao
import com.nonoka.nhentai.gateway.local.model.DoujinshiModel
import javax.inject.Inject
import timber.log.Timber

interface DoujinshiLocalDataSource {
    suspend fun getCollectedDoujinshiCount(): Long

    suspend fun getCollectedDoujinshis(skip: Int, take: Int): List<Doujinshi>

    suspend fun setReadDoujinshi(doujinshi: Doujinshi, lastReadPage: Int?): Boolean

    suspend fun getLastReadPageIndex(doujinshiId: String): Resource<Int>

    suspend fun setFavoriteDoujinshi(doujinshi: Doujinshi, isFavorite: Boolean): Boolean

    suspend fun setDownloadedDoujinshi(doujinshi: Doujinshi, isDownloaded: Boolean): Boolean

    suspend fun getDownloadedStatus(doujinshiId: String): Boolean
}

class DoujinshiLocalDataSourceImpl @Inject constructor(
    private val doujinshiDao: DoujinshiDao
) : DoujinshiLocalDataSource {

    override suspend fun getCollectedDoujinshiCount(): Long {
        return doujinshiDao.countCollectedDoujinshis()
    }

    override suspend fun getCollectedDoujinshis(skip: Int, take: Int): List<Doujinshi> {
        return doujinshiDao.getCollectedDoujinshis(skip, take).map {
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

    override suspend fun getLastReadPageIndex(doujinshiId: String): Resource<Int> {
        return try {
            Resource.Success(doujinshiDao.getLastReadPage(doujinshiId) ?: -1)
        } catch (error: Throwable) {
            Resource.Error(error)
        }
    }

    override suspend fun setFavoriteDoujinshi(doujinshi: Doujinshi, isFavorite: Boolean): Boolean {
        return if (doujinshiDao.hasDoujinshi(doujinshi.id)) {
            doujinshiDao.updateFavoriteStatus(doujinshi.id, isFavorite) > 0
        } else {
            doujinshiDao.addDoujinshi(
                DoujinshiModel(
                    id = doujinshi.id,
                    json = Gson().toJson(doujinshi, Doujinshi::class.java),
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
                    json = Gson().toJson(doujinshi, Doujinshi::class.java),
                    lastReadPage = null,
                    isFavorite = false,
                    isDownloaded = isDownloaded
                ),
            ).isNotEmpty()
        }
    }

    override suspend fun getDownloadedStatus(doujinshiId: String): Boolean {
        return doujinshiDao.getDownloadedStatus(doujinshiId)
    }

}