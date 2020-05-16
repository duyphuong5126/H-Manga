package nhdphuong.com.manga.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Single
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_BOOK as DOWNLOADED_BOOK
import nhdphuong.com.manga.Constants.Companion.TABLE_BOOK_TAG as BOOK_TAG
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_IMAGE as DOWNLOADED_IMAGE
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.IS_FAVORITE
import nhdphuong.com.manga.Constants.Companion.CREATED_AT
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.UPLOAD_DATE
import nhdphuong.com.manga.Constants.Companion.TYPE
import nhdphuong.com.manga.Constants.Companion.LOCAL_PATH
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.local.model.BookImageModel
import nhdphuong.com.manga.data.local.model.BookTagModel
import nhdphuong.com.manga.data.local.model.DownloadedBookModel
import nhdphuong.com.manga.data.local.model.ImageUsageType

/*
 * Created by nhdphuong on 6/8/18.
 */
@Dao
interface BookDAO {
    companion object {
        private const val RECENT_BOOK_TABLE: String = "RecentBook"
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentBooks(vararg recentBookEntities: RecentBook)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDownloadedBook(downloadedBooks: List<DownloadedBookModel>): List<Long>

    @Query("select * from $DOWNLOADED_BOOK order by $UPLOAD_DATE")
    fun getAllDownloadedBooks(): Single<List<DownloadedBookModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTagListOfBook(tagList: List<BookTagModel>): List<Long>

    @Query("select * from $BOOK_TAG where $BOOK_ID = :bookId")
    fun getAllTagsOfBook(bookId: String): Single<List<BookTagModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addImageOfBook(bookImageModel: BookImageModel): Long

    @Query("select $LOCAL_PATH from $DOWNLOADED_IMAGE where $BOOK_ID = :bookId and $TYPE = :usageType limit 1")
    fun getFirstImagePathOfBook(bookId: String, @ImageUsageType usageType: String): Single<String>

    @Query("select $LOCAL_PATH from $DOWNLOADED_IMAGE where $BOOK_ID = :bookId and $TYPE = :usageType and $LOCAL_PATH != '' limit 1")
    fun getFirstNotBlankImagePathOfBook(
        bookId: String, @ImageUsageType usageType: String
    ): Single<String>

    @Query("select $LOCAL_PATH from $DOWNLOADED_IMAGE where $BOOK_ID = :bookId and $TYPE = :usageType")
    fun getImagePathsOfBook(bookId: String, @ImageUsageType usageType: String): Single<List<String>>

    @Query("select * from $DOWNLOADED_IMAGE where $BOOK_ID = :bookId")
    fun getAllImagesOfBook(bookId: String): Single<List<BookImageModel>>

    @Query("delete from $DOWNLOADED_IMAGE where $BOOK_ID = :bookId")
    fun clearDownloadedImages(bookId: String): Int

    @Query("delete from $DOWNLOADED_BOOK where $ID = :bookId")
    fun deleteBook(bookId: String): Int

    @Query("delete from $RECENT_BOOK_TABLE where $BOOK_ID = :bookId")
    fun deleteRecentBook(bookId: String): Int

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
}
