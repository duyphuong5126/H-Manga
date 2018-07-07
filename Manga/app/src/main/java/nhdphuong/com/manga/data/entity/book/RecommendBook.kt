package nhdphuong.com.manga.data.entity.book

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants
import java.util.*

/*
 * Created by nhdphuong on 4/28/18.
 */
class RecommendBook(@field:SerializedName(Constants.RESULT) val bookList: LinkedList<Book>)