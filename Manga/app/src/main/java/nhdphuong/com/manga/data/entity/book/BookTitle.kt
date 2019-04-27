package nhdphuong.com.manga.data.entity.book

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants
import java.io.Serializable

/*
 * Created by nhdphuong on 3/24/18.
 */
data class BookTitle(
        @field:SerializedName(Constants.TITLE_ENG) private val mEnglishName: String?,
        @field:SerializedName(Constants.TITLE_JAPANESE) private val mJapaneseName: String?,
        @field:SerializedName(Constants.TITLE_PRETTY) private val mPretty: String?
) : Serializable {
    val japaneseName: String
        get() = mJapaneseName ?: ""

    val englishName: String
        get() = mEnglishName ?: ""

    val pretty: String
        get() = mPretty ?: ""
}
