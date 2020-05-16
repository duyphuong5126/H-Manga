package nhdphuong.com.manga.data.repository

import io.reactivex.Completable
import io.reactivex.Single
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.scope.Remote
import nhdphuong.com.manga.data.entity.book.RemoteBook
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
    override suspend fun getBookByPage(page: Long): RemoteBook? {
        return bookRemoteDataSource.getBookByPage(page)
    }

    override suspend fun getBookByPage(searchContent: String, page: Long): RemoteBook? {
        return bookRemoteDataSource.getBookByPage(searchContent, page)
    }

    override suspend fun getRecommendBook(bookId: String): RecommendBook? {
        return bookRemoteDataSource.getRecommendBook(bookId)
    }

    override suspend fun getBookDetails(bookId: String): Book? {
        return bookRemoteDataSource.getBookDetails(bookId)
    }

    override suspend fun saveFavoriteBook(bookId: String, isFavorite: Boolean) {
        bookLocalDataSource.saveFavoriteBook(bookId, isFavorite)
    }

    override suspend fun saveRecentBook(bookId: String) {
        bookLocalDataSource.saveRecentBook(bookId)
    }

    override suspend fun getFavoriteBook(limit: Int, offset: Int): LinkedList<RecentBook> {
        return bookLocalDataSource.getFavoriteBook(limit, offset)
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
}
