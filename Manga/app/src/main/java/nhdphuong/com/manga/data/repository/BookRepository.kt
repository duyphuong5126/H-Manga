package nhdphuong.com.manga.data.repository

import io.reactivex.Completable
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.Book
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

    override suspend fun isRecentBook(bookId: String): Boolean {
        return bookLocalDataSource.isRecentBook(bookId)
    }

    override suspend fun getRecentCount(): Int = bookLocalDataSource.getRecentCount()

    override suspend fun getFavoriteCount(): Int = bookLocalDataSource.getFavoriteCount()

    override fun addToRecentList(bookId: String): Completable {
        return bookLocalDataSource.addToRecentList(bookId)
    }
}
