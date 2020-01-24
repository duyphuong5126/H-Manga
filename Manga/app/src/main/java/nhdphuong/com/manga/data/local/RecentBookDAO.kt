package nhdphuong.com.manga.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Query
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.IS_FAVORITE
import nhdphuong.com.manga.Constants.Companion.CREATED_AT
import nhdphuong.com.manga.data.entity.RecentBook

/*
 * Created by nhdphuong on 6/8/18.
 */
@Dao
interface RecentBookDAO {
    companion object {
        private const val RECENT_BOOK_TABLE: String = "RecentBook"
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentBooks(vararg recentBookEntities: RecentBook)

    @Insert
    fun insertRecentBooks(recentBookEntities: List<RecentBook>)

    @Update
    fun updateRecentBook(recentBookEntity: RecentBook)

    @Query("select * from $RECENT_BOOK_TABLE order by $CREATED_AT desc limit :limit offset :offset")
    fun getRecentBooks(limit: Int, offset: Int): List<RecentBook>

    @Query("select * from $RECENT_BOOK_TABLE where $IS_FAVORITE = 1 order by $CREATED_AT desc limit :limit offset :offset")
    fun getFavoriteBooks(limit: Int, offset: Int): List<RecentBook>

    @Query("select $IS_FAVORITE from $RECENT_BOOK_TABLE where $BOOK_ID = :bookId")
    fun isFavoriteBook(bookId: String): Int

    @Query("select $BOOK_ID from $RECENT_BOOK_TABLE where $BOOK_ID = :bookId")
    fun getRecentBookId(bookId: String): String

    @Query("select count(*) from $RECENT_BOOK_TABLE")
    fun getRecentBookCount(): Int

    @Query("select count(*) from $RECENT_BOOK_TABLE where $IS_FAVORITE = 1")
    fun getFavoriteBookCount(): Int

    @Query("select * from $RECENT_BOOK_TABLE")
    fun getRecentBooks(): List<RecentBook>
}
