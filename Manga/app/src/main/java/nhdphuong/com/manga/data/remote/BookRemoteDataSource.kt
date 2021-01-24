package nhdphuong.com.manga.data.remote

import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.SerializationService
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.CommentResponse
import nhdphuong.com.manga.data.entity.RecommendBookResponse
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.entity.comment.Comment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
 * Created by nhdphuong on 3/24/18.
 */
// Todo: research EOFException when using retrofit
class BookRemoteDataSource(
    private val serializationService: SerializationService
) : BookDataSource.Remote {
    companion object {
        private const val TAG = "BookRemoteDataSource"
        private const val REQUEST_TIME_OUT = 20000
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
                val url = "${ApiConstants.homeUrl}/api/gallery/$bookId/related"
                val responseData = performGetRequest(url).orEmpty()
                val recommendBook = serializationService.deserialize(responseData, RecommendBook::class.java)
                val recommendBookResult = RecommendBookResponse.Success(recommendBook)
                continuation.resume(recommendBookResult)
            } catch (throwable: Throwable) {
                Logger.d(TAG, "get all recommend book of $bookId failed=$throwable")
                continuation.resume(RecommendBookResponse.Failure(throwable))
            }
        }
    }

    override suspend fun getCommentList(bookId: String): CommentResponse {
        return suspendCoroutine { continuation ->
            try {
                val url =
                    String.format(
                        Locale.US,
                        "${ApiConstants.homeUrl}/api/gallery/%s/comments",
                        bookId
                    )
                val responseData = performGetRequest(url).orEmpty()
                val commentsResponse = serializationService.deserialize(responseData, Array<Comment>::class.java)
                val recommendBookResult = CommentResponse.Success(commentsResponse.toList())
                continuation.resume(recommendBookResult)
            } catch (throwable: Throwable) {
                Logger.d(TAG, "get all recommend book of $bookId failed=$throwable")
                continuation.resume(CommentResponse.Failure(throwable))
            }
        }
    }

    override suspend fun getBookDetails(bookId: String): BookResponse {
        return suspendCoroutine { continuation ->
            try {
                val url = "${ApiConstants.homeUrl}/api/gallery/$bookId"
                val responseData = performGetRequest(url).orEmpty()
                val book = serializationService.deserialize(responseData, Book::class.java)
                val bookResult = BookResponse.Success(book)
                continuation.resume(bookResult)
            } catch (throwable: Throwable) {
                Logger.d(TAG, "get book details of $bookId failed=$throwable")
                continuation.resume(BookResponse.Failure(throwable))
            }
        }
    }

    override fun getBookDetailsSynchronously(bookId: String): BookResponse {
        return try {
            val url = "${ApiConstants.homeUrl}/api/gallery/$bookId"
            val responseData = performGetRequest(url).orEmpty()
            val book = serializationService.deserialize(responseData, Book::class.java)
            BookResponse.Success(book)
        } catch (throwable: Throwable) {
            BookResponse.Failure(throwable)
        }
    }

    private fun getBookByPage(
        page: Long,
        searchContent: String,
        sortOption: SortOption
    ): RemoteBookResponse {
        return try {
            var url = if (searchContent.isNotBlank()) {
                "${ApiConstants.homeUrl}/api/galleries/search?query=$searchContent&page=$page"
            } else {
                "${ApiConstants.homeUrl}/api/galleries/all?page=$page"
            }
            if (sortOption != SortOption.Recent) {
                url += getSortString(sortOption)
            }
            val responseData = performGetRequest(url).orEmpty()
            val remoteBook = serializationService.deserialize(responseData, RemoteBook::class.java)
            RemoteBookResponse.Success(remoteBook)
        } catch (throwable: Throwable) {
            Logger.d(TAG, "get all remote books of page $page failed=$throwable")
            RemoteBookResponse.Failure(throwable)
        }
    }

    private fun performGetRequest(requestUrl: String): String? {
        Logger.d(TAG, "Get from url $requestUrl")
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-length", "0")
        connection.useCaches = false
        connection.readTimeout = REQUEST_TIME_OUT
        connection.connectTimeout = REQUEST_TIME_OUT
        connection.connect()
        when (connection.responseCode) {
            HttpsURLConnection.HTTP_OK -> {
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
