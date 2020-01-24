package nhdphuong.com.manga.data.local

import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.RecentBook
import java.util.LinkedList
import javax.inject.Inject

/*
 * Created by nhdphuong on 6/9/18.
 */
class BookLocalDataSource @Inject constructor(
    private val mRecentBookDAO: RecentBookDAO
) : BookDataSource.Local {
    override suspend fun saveRecentBook(bookId: String) {
        mRecentBookDAO.insertRecentBooks(RecentBook(bookId, false, System.currentTimeMillis()))
    }

    override suspend fun saveFavoriteBook(bookId: String, isFavorite: Boolean) {
        mRecentBookDAO.insertRecentBooks(RecentBook(bookId, isFavorite, System.currentTimeMillis()))
    }

    override suspend fun getRecentBooks(limit: Int, offset: Int): LinkedList<RecentBook> {
        val result = LinkedList<RecentBook>()
        result.addAll(mRecentBookDAO.getRecentBooks(limit, offset))
        return result
    }

    override suspend fun getFavoriteBook(limit: Int, offset: Int): LinkedList<RecentBook> {
        val result = LinkedList<RecentBook>()
        result.addAll(mRecentBookDAO.getFavoriteBooks(limit, offset))
        return result
    }

    override suspend fun isFavoriteBook(bookId: String): Boolean {
        return mRecentBookDAO.isFavoriteBook(bookId) == 1
    }

    override suspend fun isRecentBook(bookId: String): Boolean {
        return mRecentBookDAO.getRecentBookId(bookId) == bookId
    }

    override suspend fun getRecentCount(): Int = mRecentBookDAO.getRecentBookCount()

    override suspend fun getFavoriteCount(): Int = mRecentBookDAO.getFavoriteBookCount()
}
