package nhdphuong.com.manga.data

import io.reactivex.Completable
import io.reactivex.Single
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.local.model.ImageUsageType
import java.util.LinkedList

/*
 * Created by nhdphuong on 3/24/18.
 */
interface BookDataSource {
    interface Remote {
        suspend fun getBookByPage(page: Long): RemoteBook?
        suspend fun getBookByPage(searchContent: String, page: Long): RemoteBook?
        suspend fun getBookDetails(bookId: String): Book?
        suspend fun getRecommendBook(bookId: String): RecommendBook?
    }

    interface Local {
        suspend fun saveRecentBook(bookId: String)
        suspend fun saveFavoriteBook(bookId: String, isFavorite: Boolean)
        suspend fun getRecentBooks(limit: Int, offset: Int): LinkedList<RecentBook>
        suspend fun getFavoriteBook(limit: Int, offset: Int): LinkedList<RecentBook>
        suspend fun isFavoriteBook(bookId: String): Boolean
        fun checkIfFavoriteBook(bookId: String): Single<Boolean>
        suspend fun isRecentBook(bookId: String): Boolean
        suspend fun unSeenBook(bookId: String): Boolean
        suspend fun getRecentCount(): Int
        suspend fun getFavoriteCount(): Int
        fun getDownloadedBookList(): Single<List<Book>>
        fun addToRecentList(bookId: String): Completable
        fun saveDownloadedBook(book: Book): Completable
        fun saveImageOfBook(
            bookId: String,
            imageMeasurements: ImageMeasurements,
            @ImageUsageType usageType: String,
            localPath: String
        ): Completable

        fun getDownloadedBookCoverPath(bookId: String): Single<String>

        fun getDownloadedBookImagePaths(bookId: String): Single<List<String>>

        fun getDownloadedBookThumbnailPaths(bookIds: List<String>): Single<List<Pair<String, String>>>

        fun clearDownloadedImagesOfBook(bookId: String): Completable

        fun deleteBook(bookId: String): Completable

        fun saveLastVisitedPage(bookId: String, lastVisitedPage: Int): Completable

        fun getLastVisitedPage(bookId: String): Single<Int>
    }
}
