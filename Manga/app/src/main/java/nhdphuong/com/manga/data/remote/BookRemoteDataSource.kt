package nhdphuong.com.manga.data.remote

import com.google.gson.Gson
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.BookApiService
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.RecommendBookResponse
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.entity.book.SortOption
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.io.BufferedReader
import java.io.InputStreamReader

/*
 * Created by nhdphuong on 3/24/18.
 */
// Todo: research EOFException when using retrofit
class BookRemoteDataSource(
    @Suppress("unused")
    private val mBookApiService: BookApiService
) : BookDataSource.Remote {
    companion object {
        private const val TAG = "BookRemoteDataSource"
    }

    override suspend fun getBookByPage(page: Long, sortOption: SortOption): RemoteBookResponse {
        return suspendCoroutine { continuation ->
            val remoteBook = getBookByPage(page, "", sortOption)
            continuation.resume(remoteBook)
        }
    }

    override suspend fun getBookByPage(
        searchContent: String,
        page: Long,
        sortOption: SortOption
    ): RemoteBookResponse {
        return suspendCoroutine { continuation ->
            val remoteBook = getBookByPage(page, searchContent.replace(" ", "+"), sortOption)
            continuation.resume(remoteBook)
        }
    }

    override suspend fun getRecommendBook(bookId: String): RecommendBookResponse {
        return suspendCoroutine { continuation ->
            try {
                val url = "https://nhentai.net/api/gallery/$bookId/related"
                val responseData = performGetRequest(url)
                val recommendBook = Gson().fromJson(responseData, RecommendBook::class.java)
                val recommendBookResult = RecommendBookResponse.Success(recommendBook)
                continuation.resume(recommendBookResult)
            } catch (throwable: Throwable) {
                Logger.d(TAG, "get all recommend book of $bookId failed=$throwable")
                continuation.resume(RecommendBookResponse.Failure(throwable))
            }
        }
    }

    override suspend fun getBookDetails(bookId: String): BookResponse {
        return suspendCoroutine { continuation ->
            try {
                val url = "https://nhentai.net/api/gallery/$bookId"
                val responseData = performGetRequest(url)
                val book = Gson().fromJson(responseData, Book::class.java)
                val bookResult = BookResponse.Success(book)
                continuation.resume(bookResult)
            } catch (throwable: Throwable) {
                Logger.d(TAG, "get book details of $bookId failed=$throwable")
                continuation.resume(BookResponse.Failure(throwable))
            }
        }
    }

    private fun getBookByPage(
        page: Long,
        searchContent: String,
        sortOption: SortOption
    ): RemoteBookResponse {
        return try {
            var url = if (searchContent.isNotBlank()) {
                "https://nhentai.net/api/galleries/search?query=$searchContent&page=$page"
            } else {
                "https://nhentai.net/api/galleries/all?page=$page"
            }
            if (sortOption != SortOption.Recent) {
                url += getSortString(sortOption)
            }
            Logger.d("BookRemoteDataSource", "url $url")
            val responseData = performGetRequest(url)
            val remoteBook = Gson().fromJson(responseData, RemoteBook::class.java)
            RemoteBookResponse.Success(remoteBook)
        } catch (throwable: Throwable) {
            Logger.d(TAG, "get all remote books of page $page failed=$throwable")
            RemoteBookResponse.Failure(throwable)
        }
    }

    private fun performGetRequest(requestUrl: String): String? {
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-length", "0")
        connection.useCaches = false
        connection.connect()
        when (connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val br = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line = br.readLine()
                while (line != null) {
                    sb.append("$line\n")
                    line = br.readLine()
                }
                br.close()
                connection.disconnect()
                Logger.d(TAG, "Data=$sb")
                return sb.toString()
            }
        }
        connection.disconnect()
        return null
    }

    private fun getSortString(sortOption: SortOption): String {
        return when (sortOption) {
            SortOption.Recent -> ""
            SortOption.PopularAllTime -> "&sort=popular"
            SortOption.PopularToday -> "&sort=popular-today"
            SortOption.PopularWeek -> "&sort=popular-week"
        }
    }
}
