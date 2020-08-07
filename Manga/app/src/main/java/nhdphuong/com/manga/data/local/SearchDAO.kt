package nhdphuong.com.manga.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Single
import nhdphuong.com.manga.Constants.Companion.TABLE_SEARCH
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.SEARCH_INFO
import nhdphuong.com.manga.data.local.model.SearchModel

@Dao
interface SearchDAO {
    @Insert
    fun saveSearchInfo(searchModel: SearchModel): Long

    @Query("select * from $TABLE_SEARCH where $SEARCH_INFO = :searchInfo")
    fun findSearchInfo(searchInfo: String): Single<SearchModel>

    @Query("select * from $TABLE_SEARCH order by $ID desc limit :maximumEntries")
    fun getLatestSearchEntries(maximumEntries: Int): Single<List<SearchModel>>
}
