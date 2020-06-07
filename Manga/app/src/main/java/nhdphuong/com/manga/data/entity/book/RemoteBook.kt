package nhdphuong.com.manga.data.entity.book

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

/*
 * Created by nhdphuong on 3/24/18.
 */
data class RemoteBook(
    @field:SerializedName(Constants.RESULT) val bookList: ArrayList<Book>,
    @field:SerializedName(Constants.NUM_PAGES) val numOfPages: Long,
    @field:SerializedName(Constants.PER_PAGE) val numOfBooksPerPage: Int
)
