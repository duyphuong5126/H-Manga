package nhdphuong.com.manga.data.repository

import io.reactivex.Completable
import io.reactivex.Single
import nhdphuong.com.manga.data.local.SearchLocalDataSource
import javax.inject.Inject

interface SearchRepository {
    fun saveSearchEntry(searchInfo: String): Completable
    fun getLatestSearchEntries(maximumEntries: Int): Single<List<String>>
    fun getMostUsedSearchEntries(maximumEntries: Int): Single<List<String>>
    fun deleteSearchInfo(searchInfo: String): Completable
}

class SearchRepositoryImpl @Inject constructor(
    private val searchLocalDataSource: SearchLocalDataSource
) : SearchRepository {
    override fun saveSearchEntry(searchInfo: String): Completable {
        return searchLocalDataSource.saveSearchEntry(searchInfo)
    }

    override fun getLatestSearchEntries(maximumEntries: Int): Single<List<String>> {
        return searchLocalDataSource.getLatestSearchEntries(maximumEntries)
    }

    override fun getMostUsedSearchEntries(maximumEntries: Int): Single<List<String>> {
        return searchLocalDataSource.getMostUsedSearchEntries(maximumEntries)
    }

    override fun deleteSearchInfo(searchInfo: String): Completable {
        return searchLocalDataSource.deleteSearchInfo(searchInfo)
    }
}
