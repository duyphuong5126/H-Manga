package nhdphuong.com.manga.data.local

import androidx.room.EmptyResultSetException
import io.reactivex.Completable
import io.reactivex.Single
import nhdphuong.com.manga.data.local.model.SearchModel
import javax.inject.Inject

interface SearchLocalDataSource {
    fun saveSearchEntry(searchInfo: String): Completable
    fun getLatestSearchEntries(maximumEntries: Int): Single<List<String>>
    fun getMostUsedSearchEntries(maximumEntries: Int): Single<List<String>>
    fun deleteSearchInfo(searchInfo: String): Completable
}

class SearchLocalDataSourceImpl @Inject constructor(
    private val searchDAO: SearchDAO
) : SearchLocalDataSource {
    override fun saveSearchEntry(searchInfo: String): Completable {
        return searchDAO.findSearchInfo(searchInfo)
            .doOnSuccess {
                searchDAO.updateSearchTimes(it.searchInfo, it.searchTimes + 1)
            }
            .onErrorResumeNext {
                if (it is EmptyResultSetException) {
                    val searchEntry = SearchModel(searchInfo, 1)
                    searchDAO.saveSearchInfo(searchEntry)
                    Single.just(searchEntry)
                } else {
                    Single.error(it)
                }
            }
            .ignoreElement()
    }

    override fun getLatestSearchEntries(maximumEntries: Int): Single<List<String>> {
        return searchDAO.getLatestSearchEntries(maximumEntries)
            .map {
                it.map(SearchModel::searchInfo)
            }
    }

    override fun getMostUsedSearchEntries(maximumEntries: Int): Single<List<String>> {
        return searchDAO.getMostUsedSearchEntries(maximumEntries)
    }

    override fun deleteSearchInfo(searchInfo: String): Completable {
        return Completable.create {
            try {
                searchDAO.deleteSearchInfo(searchInfo)
                it.onComplete()
            } catch (throwable: Throwable) {
                it.onError(throwable)
            }
        }
    }
}
