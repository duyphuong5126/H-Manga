package nhdphuong.com.manga.data.entity.book

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants
import java.util.*

/*
 * Created by nhdphuong on 3/24/18.
 */
data class RemoteBook(
        @field:SerializedName(Constants.RESULT) val bookList: LinkedList<Book>,
        @field:SerializedName(Constants.NUM_PAGES) val numOfPages: Long,
        @field:SerializedName(Constants.PER_PAGE) val numOfBooksPerPage: Int
)
