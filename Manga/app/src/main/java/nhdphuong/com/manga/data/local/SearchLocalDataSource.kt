package nhdphuong.com.manga.data.local

import androidx.room.EmptyResultSetException
import io.reactivex.Completable
import io.reactivex.Single
import nhdphuong.com.manga.data.local.model.SearchModel
import javax.inject.Inject

interface SearchLocalDataSource {
    fun saveSearchEntry(searchInfo: String): Completable
    fun getLatestSearchEntries(maximumEntries: Int): Single<List<String>>
}

class SearchLocalDataSourceImpl @Inject constructor(
    private val searchDAO: SearchDAO
) : SearchLocalDataSource {
    override fun saveSearchEntry(searchInfo: String): Completable {
        return searchDAO.findSearchInfo(searchInfo)
            .onErrorResumeNext {
                if (it is EmptyResultSetException) {
                    val searchEntry = SearchModel(searchInfo)
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
                it.map { searchModel -> searchModel.searchInfo }
            }
    }
}
