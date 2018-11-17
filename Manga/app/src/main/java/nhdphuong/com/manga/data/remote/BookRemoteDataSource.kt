package nhdphuong.com.manga.data.remote

import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.BookApiService
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
 * Created by nhdphuong on 3/24/18.
 */
class BookRemoteDataSource(private val mBookApiService: BookApiService) : BookDataSource.Remote {
    companion object {
        private const val TAG = "BookRemoteDataSource"
    }

    override suspend fun getBookByPage(page: Int): RemoteBook? {
        return suspendCoroutine { continuation ->
            mBookApiService.getBookListByPage(page).enqueue(object : Callback<RemoteBook> {
                override fun onResponse(call: Call<RemoteBook>?, response: Response<RemoteBook>?) {
                    Logger.d(TAG, "get all remote books of page $page successfully")
                    continuation.resume(response?.body())
                }

                override fun onFailure(call: Call<RemoteBook>?, t: Throwable?) {
                    Logger.d(TAG, "get all remote books of page $page fail")
                    continuation.resume(null)
                }
            })
        }
    }

    override suspend fun getBookByPage(searchContent: String, page: Int): RemoteBook? {
        return suspendCoroutine { continuation ->
            mBookApiService.searchByPage(searchContent.replace(" ", "+"), page).enqueue(object : Callback<RemoteBook> {
                override fun onResponse(call: Call<RemoteBook>?, response: Response<RemoteBook>?) {
                    Logger.d(TAG, "search books by $searchContent of page $page successfully")
                    continuation.resume(response?.body())
                }

                override fun onFailure(call: Call<RemoteBook>?, t: Throwable?) {
                    Logger.d(TAG, "search books by $searchContent of page $page fail")
                    continuation.resume(null)
                }
            })
        }
    }

    override suspend fun getRecommendBook(bookId: String): RecommendBook? {
        return suspendCoroutine { continuation ->
            mBookApiService.getRecommendBook(bookId).enqueue(object : Callback<RecommendBook> {
                override fun onResponse(call: Call<RecommendBook>?, response: Response<RecommendBook>?) {
                    Logger.d(TAG, "get recommend books of $bookId successfully")
                    continuation.resume(response?.body())
                }

                override fun onFailure(call: Call<RecommendBook>?, t: Throwable?) {
                    Logger.d(TAG, "get recommend books of $bookId fail")
                    continuation.resume(null)
                }
            })
        }
    }

    override suspend fun getBookDetails(bookId: String): Book? {
        return suspendCoroutine { continuation ->
            mBookApiService.getBookDetails(bookId).enqueue(object : Callback<Book> {
                override fun onResponse(call: Call<Book>?, response: Response<Book>?) {
                    Logger.d(TAG, "Get book details of $bookId successfully")
                    continuation.resume(response?.body())
                }

                override fun onFailure(call: Call<Book>?, t: Throwable?) {
                    Logger.d(TAG, "Get book details of $bookId failed")
                    continuation.resume(null)
                }
            })
        }
    }
}