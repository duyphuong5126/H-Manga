package nhdphuong.com.manga.data

import io.reactivex.Completable
import io.reactivex.Single
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.CommentResponse
import nhdphuong.com.manga.data.entity.FavoriteBook
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.RecommendBookResponse
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.local.model.ImageUsageType
import java.util.LinkedList

/*
 * Created by nhdphuong on 3/24/18.
 */
interface BookDataSource {
    interface Remote {
        suspend fun getBookByPage(page: Long, sortOption: SortOption): RemoteBookResponse
        suspend fun getBookByPage(
            searchContent: String,
            page: Long,
            sortOption: SortOption
        ): RemoteBookResponse

        suspend fun getBookDetails(bookId: String): BookResponse
        fun getBookDetailsSynchronously(bookId: String): BookResponse
        suspend fun getRecommendBook(bookId: String): RecommendBookResponse
        suspend fun getCommentList(bookId: String): CommentResponse
    }

    interface Local {
        suspend fun saveRecentBook(book: Book)
        suspend fun saveFavoriteBook(book: Book)
        suspend fun removeFavoriteBook(book: Book)
        suspend fun addBookToBlockList(bookId: String)
        fun getEmptyRecentBooks(): Single<List<RecentBook>>
        fun getEmptyFavoriteBooks(): Single<List<FavoriteBook>>
        fun getEmptyRecentBooksCount(): Int
        fun getEmptyFavoriteBooksCount(): Int
        suspend fun getRecentBooks(limit: Int, offset: Int): LinkedList<RecentBook>
        suspend fun getFavoriteBooks(limit: Int, offset: Int): LinkedList<FavoriteBook>
        suspend fun getAllRecentBookIds(): List<String>
        suspend fun getAllFavoriteBookIds(): List<String>
        fun updateRawRecentBook(bookId: String, rawBook: String): Boolean
        fun updateRawFavoriteBook(bookId: String, rawBook: String): Boolean
        suspend fun isFavoriteBook(bookId: String): Boolean
        fun checkIfFavoriteBook(bookId: String): Single<Boolean>
        suspend fun isRecentBook(bookId: String): Boolean
        suspend fun unSeenBook(bookId: String): Boolean
        suspend fun getRecentCount(): Int
        suspend fun getFavoriteCount(): Int
        fun getDownloadedBookList(): Single<List<Book>>
        fun addToRecentList(book: Book): Completable
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

        suspend fun deleteLastVisitedPage(bookId: String): Boolean

        fun saveLastVisitedPage(bookId: String, lastVisitedPage: Int): Completable

        fun getLastVisitedPage(bookId: String): Single<Int>

        fun getMostUsedTags(maximumEntries: Int): Single<List<String>>

        fun getRecentBookIdsForRecommendation(): Single<List<String>>

        suspend fun getBlockedBookIds(): List<String>
    }
}
