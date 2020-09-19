package nhdphuong.com.manga.api

import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RecommendBook
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.entity.comment.Comment
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*
 * Created by nhdphuong on 3/24/18.
 */
interface BookApiService {
    @Suppress("unused")
    @GET("/api/galleries/all")
    fun getBookListByPage(@Query("page") pageNumber: Long): Call<RemoteBook>

    @Suppress("unused")
    @GET("/api/galleries/search")
    fun searchByPage(
            @Query("query") condition: String,
            @Query("page") pageNumber: Long
    ): Call<RemoteBook>

    @Suppress("unused")
    @GET("/api/gallery/{bookId}")
    fun getBookDetails(@Path("bookId") bookId: String): Call<Book>

    @Suppress("unused")
    @GET("/api/gallery/{bookId}/related")
    fun getRecommendBook(@Path("bookId") bookId: String): Call<RecommendBook>

    @GET("/api/gallery/{bookId}/comments")
    fun getCommentList(@Path("bookId") bookId: String): Call<List<Comment>>
}
