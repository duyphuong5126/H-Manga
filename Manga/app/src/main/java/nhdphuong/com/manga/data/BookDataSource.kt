package nhdphuong.com.manga.data

import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
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
        suspend fun isRecentBook(bookId: String): Boolean
        suspend fun getRecentCount(): Int
        suspend fun getFavoriteCount(): Int
    }
}
