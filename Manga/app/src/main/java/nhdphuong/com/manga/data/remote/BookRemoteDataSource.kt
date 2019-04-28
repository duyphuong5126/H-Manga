package nhdphuong.com.manga.data.remote

import com.google.gson.Gson
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.BookApiService
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
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

    override suspend fun getBookByPage(page: Long): RemoteBook? {
        getBookByPage(page, "")
        return suspendCoroutine { continuation ->
            val remoteBook = getBookByPage(page, "")
            continuation.resume(remoteBook)
        }
    }

    override suspend fun getBookByPage(searchContent: String, page: Long): RemoteBook? {
        return suspendCoroutine { continuation ->
            val remoteBook = getBookByPage(page, searchContent.replace(" ", "+"))
            continuation.resume(remoteBook)
        }
    }

    override suspend fun getRecommendBook(bookId: String): RecommendBook? {
        return suspendCoroutine { continuation ->
            try {
                val url = "https://nhentai.net/api/gallery/$bookId/related"
                val responseData = performGetRequest(url)
                val recommendBook = Gson().fromJson(responseData, RecommendBook::class.java)
                continuation.resume(recommendBook)
            } catch (exception: Exception) {
                Logger.d(TAG, "get all recommend book of $bookId failed=$exception")
                continuation.resume(null)
            }
        }
    }

    override suspend fun getBookDetails(bookId: String): Book? {
        return suspendCoroutine { continuation ->
            try {
                val url = "https://nhentai.net/api/gallery/$bookId"
                val responseData = performGetRequest(url)
                val book = Gson().fromJson(responseData, Book::class.java)
                continuation.resume(book)
            } catch (exception: Exception) {
                Logger.d(TAG, "get book details of $bookId failed=$exception")
                continuation.resume(null)
            }
        }
    }

    private fun getBookByPage(page: Long, searchContent: String): RemoteBook? {
        return try {
            val url = if (searchContent.isNotBlank()) {
                "https://nhentai.net/api/galleries/search?query=$searchContent&page=$page"
            } else {
                "https://nhentai.net/api/galleries/all?page=$page"
            }
            val responseData = performGetRequest(url)
            Gson().fromJson(responseData, RemoteBook::class.java)
        } catch (exception: Exception) {
            Logger.d(TAG, "get all remote books of page $page failed=$exception")
            null
        }
    }

    private fun performGetRequest(requestUrl: String): String? {
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-length", "0")
        connection.useCaches = false
        connection.connect()
        val status = connection.responseCode
        when (status) {
            200 -> {
                val br = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line = br.readLine()
                while (line != null) {
                    sb.append("$line\n")
                    line = br.readLine()
                }
                br.close()
                Logger.d(TAG, "Data=$sb")
                return sb.toString()
            }
        }
        return null
    }
}
