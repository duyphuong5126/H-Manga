package nhdphuong.com.manga.data.remote

import android.util.Log
import nhdphuong.com.manga.api.BookApiService
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch

/*
 * Created by nhdphuong on 3/24/18.
 */
class BookRemoteDataSource(private val mBookApiService: BookApiService) : BookDataSource.Remote {
    companion object {
        private val TAG = BookRemoteDataSource::class.java.simpleName
    }

    override suspend fun getBookByPage(page: Int): RemoteBook? {
        val countDownLatch = CountDownLatch(1)
        var remoteBook: RemoteBook? = null
        mBookApiService.getBookListByPage(page).enqueue(object : Callback<RemoteBook> {
            override fun onResponse(call: Call<RemoteBook>?, response: Response<RemoteBook>?) {
                Log.d(TAG, "get all remote books of page $page successfully")
                remoteBook = response?.body()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call<RemoteBook>?, t: Throwable?) {
                Log.d(TAG, "get all remote books of page $page fail")
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        return remoteBook
    }

    override suspend fun getBookByPage(searchContent: String, page: Int): RemoteBook? {
        val countDownLatch = CountDownLatch(1)
        var remoteBook: RemoteBook? = null
        mBookApiService.searchByPage(searchContent.replace(" ", "+"), page).enqueue(object : Callback<RemoteBook> {
            override fun onResponse(call: Call<RemoteBook>?, response: Response<RemoteBook>?) {
                Log.d(TAG, "search books by $searchContent of page $page successfully")
                remoteBook = response?.body()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call<RemoteBook>?, t: Throwable?) {
                Log.d(TAG, "search books by $searchContent of page $page fail")
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        return remoteBook
    }

    override suspend fun getRecommendBook(bookId: String): RecommendBook? {
        var recommendBook: RecommendBook? = null
        val countDownLatch = CountDownLatch(1)
        mBookApiService.getRecommendBook(bookId).enqueue(object : Callback<RecommendBook> {
            override fun onResponse(call: Call<RecommendBook>?, response: Response<RecommendBook>?) {
                Log.d(TAG, "get recommend books of $bookId successfully")
                recommendBook = response?.body()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call<RecommendBook>?, t: Throwable?) {
                Log.d(TAG, "get recommend books of $bookId fail")
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        return recommendBook
    }

    override suspend fun getBookDetails(bookId: String): Book? {
        val countDownLatch = CountDownLatch(1)
        var book: Book? = null
        mBookApiService.getBookDetails(bookId).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>?, response: Response<Book>?) {
                Log.d(TAG, "Get book details of $bookId successfully")
                book = response?.body()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call<Book>?, t: Throwable?) {
                Log.d(TAG, "Get book details of $bookId failed")
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        return book
    }
}