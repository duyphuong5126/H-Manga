package nhdphuong.com.manga.data.repository

import io.reactivex.Completable
import io.reactivex.Single
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.CommentResponse
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.RecommendBookResponse
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.scope.Remote
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.scope.Local
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Created by nhdphuong on 3/24/18.
 */
@Singleton
class BookRepository @Inject constructor(
    @Remote private val bookRemoteDataSource: BookDataSource.Remote,
    @Local private val bookLocalDataSource: BookDataSource.Local
) : BookDataSource.Remote, BookDataSource.Local {
    override suspend fun getBookByPage(page: Long, sortOption: SortOption): RemoteBookResponse {
        return bookRemoteDataSource.getBookByPage(page, sortOption)
    }

    override suspend fun getBookByPage(
        searchContent: String,
        page: Long,
        sortOption: SortOption
    ): RemoteBookResponse {
        return bookRemoteDataSource.getBookByPage(searchContent, page, sortOption)
    }

    override suspend fun getRecommendBook(bookId: String): RecommendBookResponse {
        return bookRemoteDataSource.getRecommendBook(bookId)
    }

    override suspend fun getBookDetails(bookId: String): BookResponse {
        return bookRemoteDataSource.getBookDetails(bookId)
    }

    override suspend fun saveFavoriteBook(bookId: String, isFavorite: Boolean) {
        bookLocalDataSource.saveFavoriteBook(bookId, isFavorite)
    }

    override suspend fun saveRecentBook(bookId: String) {
        bookLocalDataSource.saveRecentBook(bookId)
    }

    override suspend fun getFavoriteBooks(limit: Int, offset: Int): LinkedList<RecentBook> {
        return bookLocalDataSource.getFavoriteBooks(limit, offset)
    }

    override suspend fun getRecentBooks(limit: Int, offset: Int): LinkedList<RecentBook> {
        return bookLocalDataSource.getRecentBooks(limit, offset)
    }

    override suspend fun isFavoriteBook(bookId: String): Boolean {
        return bookLocalDataSource.isFavoriteBook(bookId)
    }

    override fun checkIfFavoriteBook(bookId: String): Single<Boolean> {
        return bookLocalDataSource.checkIfFavoriteBook(bookId)
    }

    override suspend fun isRecentBook(bookId: String): Boolean {
        return bookLocalDataSource.isRecentBook(bookId)
    }

    override suspend fun getRecentCount(): Int = bookLocalDataSource.getRecentCount()

    override suspend fun getFavoriteCount(): Int = bookLocalDataSource.getFavoriteCount()

    override fun addToRecentList(bookId: String): Completable {
        return bookLocalDataSource.addToRecentList(bookId)
    }

    override fun saveDownloadedBook(book: Book): Completable {
        return bookLocalDataSource.saveDownloadedBook(book)
    }

    override fun saveImageOfBook(
        bookId: String,
        imageMeasurements: ImageMeasurements,
        usageType: String,
        localPath: String
    ): Completable {
        return bookLocalDataSource.saveImageOfBook(bookId, imageMeasurements, usageType, localPath)
    }

    override fun getDownloadedBookList(): Single<List<Book>> {
        return bookLocalDataSource.getDownloadedBookList()
    }

    override fun getDownloadedBookCoverPath(bookId: String): Single<String> {
        return bookLocalDataSource.getDownloadedBookCoverPath(bookId)
    }

    override fun getDownloadedBookImagePaths(bookId: String): Single<List<String>> {
        return bookLocalDataSource.getDownloadedBookImagePaths(bookId)
    }

    override fun getDownloadedBookThumbnailPaths(bookIds: List<String>): Single<List<Pair<String, String>>> {
        return bookLocalDataSource.getDownloadedBookThumbnailPaths(bookIds)
    }

    override fun clearDownloadedImagesOfBook(bookId: String): Completable {
        return bookLocalDataSource.clearDownloadedImagesOfBook(bookId)
    }

    override fun deleteBook(bookId: String): Completable {
        return bookLocalDataSource.deleteBook(bookId)
    }

    override suspend fun unSeenBook(bookId: String): Boolean {
        return bookLocalDataSource.unSeenBook(bookId)
    }

    override suspend fun deleteLastVisitedPage(bookId: String): Boolean {
        return bookLocalDataSource.deleteLastVisitedPage(bookId)
    }

    override fun saveLastVisitedPage(bookId: String, lastVisitedPage: Int): Completable {
        return bookLocalDataSource.saveLastVisitedPage(bookId, lastVisitedPage)
    }

    override fun getLastVisitedPage(bookId: String): Single<Int> {
        return bookLocalDataSource.getLastVisitedPage(bookId)
    }

    override suspend fun getCommentList(bookId: String): CommentResponse {
        return bookRemoteDataSource.getCommentList(bookId)
    }
}
