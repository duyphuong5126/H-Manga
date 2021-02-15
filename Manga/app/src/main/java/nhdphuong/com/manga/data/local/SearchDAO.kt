package nhdphuong.com.manga.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Single
import nhdphuong.com.manga.Constants.Companion.TABLE_SEARCH
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.SEARCH_INFO
import nhdphuong.com.manga.Constants.Companion.SEARCH_TIMES
import nhdphuong.com.manga.data.local.model.SearchModel

@Dao
interface SearchDAO {
    @Insert
    fun saveSearchInfo(searchModel: SearchModel): Long

    @Query("select * from $TABLE_SEARCH where $SEARCH_INFO = :searchInfo")
    fun findSearchInfo(searchInfo: String): Single<SearchModel>

    @Query("update $TABLE_SEARCH set $SEARCH_TIMES = :searchTimes where $SEARCH_INFO = :searchInfo")
    fun updateSearchTimes(searchInfo: String, searchTimes: Int): Int

    @Query("select * from $TABLE_SEARCH order by $ID desc limit :maximumEntries")
    fun getLatestSearchEntries(maximumEntries: Int): Single<List<SearchModel>>

    @Query("select $SEARCH_INFO from $TABLE_SEARCH order by $SEARCH_TIMES desc limit :maximumEntries")
    fun getMostUsedSearchEntries(maximumEntries: Int): Single<List<String>>

    @Query("delete from $TABLE_SEARCH where $SEARCH_INFO = :searchInfo")
    fun deleteSearchInfo(searchInfo: String): Int
}
