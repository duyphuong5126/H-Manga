package nhdphuong.com.manga.data.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

/*
 * Created by nhdphuong on 3/24/18.
 */
data class BookTitle(
    @field:SerializedName(Constants.TITLE_ENG) private val mEnglishName: String?,
    @field:SerializedName(Constants.TITLE_JAPANESE) private val mJapaneseName: String?,
    @field:SerializedName(Constants.TITLE_PRETTY) private val mPretty: String?
) : Parcelable {
    val japaneseName: String
        get() = mJapaneseName.orEmpty()

    val englishName: String
        get() = mEnglishName.orEmpty()

    val pretty: String
        get() = mPretty.orEmpty()

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mEnglishName)
        parcel.writeString(mJapaneseName)
        parcel.writeString(mPretty)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BookTitle> {
        val defaultInstance
            get() = BookTitle("", "", "")

        override fun createFromParcel(parcel: Parcel): BookTitle {
            return BookTitle(parcel)
        }

        override fun newArray(size: Int): Array<BookTitle?> {
            return arrayOfNulls(size)
        }
    }
}
