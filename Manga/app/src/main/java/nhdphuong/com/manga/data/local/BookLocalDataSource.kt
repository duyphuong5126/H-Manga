package nhdphuong.com.manga.data.local

import io.reactivex.Completable
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.RecentBook
import java.util.LinkedList
import javax.inject.Inject

/*
 * Created by nhdphuong on 6/9/18.
 */
class BookLocalDataSource @Inject constructor(
    private val recentBookDAO: RecentBookDAO
) : BookDataSource.Local {
    override suspend fun saveRecentBook(bookId: String) {
        recentBookDAO.insertRecentBooks(RecentBook(bookId, false, System.currentTimeMillis()))
    }

    override suspend fun saveFavoriteBook(bookId: String, isFavorite: Boolean) {
        recentBookDAO.insertRecentBooks(RecentBook(bookId, isFavorite, System.currentTimeMillis()))
    }

    override suspend fun getRecentBooks(limit: Int, offset: Int): LinkedList<RecentBook> {
        val result = LinkedList<RecentBook>()
        result.addAll(recentBookDAO.getRecentBooks(limit, offset))
        return result
    }

    override suspend fun getFavoriteBook(limit: Int, offset: Int): LinkedList<RecentBook> {
        val result = LinkedList<RecentBook>()
        result.addAll(recentBookDAO.getFavoriteBooks(limit, offset))
        return result
    }

    override suspend fun isFavoriteBook(bookId: String): Boolean {
        return recentBookDAO.isFavoriteBook(bookId) == 1
    }

    override suspend fun isRecentBook(bookId: String): Boolean {
        return recentBookDAO.getRecentBookId(bookId) == bookId
    }

    override suspend fun getRecentCount(): Int = recentBookDAO.getRecentBookCount()

    override suspend fun getFavoriteCount(): Int = recentBookDAO.getFavoriteBookCount()

    override fun addToRecentList(bookId: String): Completable {
        return Completable.fromCallable {
            recentBookDAO.insertRecentBooks(RecentBook(bookId, false, System.currentTimeMillis()))
        }
    }
}
