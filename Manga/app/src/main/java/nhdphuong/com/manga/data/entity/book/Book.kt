package nhdphuong.com.manga.data.entity.book

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.data.entity.book.tags.Tag

/*
 * Created by nhdphuong on 3/24/18.
 */
data class Book(
    @field:SerializedName(Constants.ID) val bookId: String,
    @field:SerializedName(Constants.MEDIA_ID) val mediaId: String,
    @field:SerializedName(Constants.TITLE) val title: BookTitle,
    @field:SerializedName(Constants.IMAGES) val bookImages: BookImages,
    @field:SerializedName(Constants.SCANLATOR) val scanlator: String,
    @field:SerializedName(Constants.UPLOAD_DATE) val updateAt: Long,
    @field:SerializedName(Constants.TAGS_LIST) val tags: List<Tag>,
    @field:SerializedName(Constants.NUM_PAGES) val numOfPages: Int,
    @field:SerializedName(Constants.NUM_FAVORITES) val numOfFavorites: Int
) : Parcelable {

    val thumbnail: String
        get() {
            val imageType = bookImages.thumbnail.imageType
            return ApiConstants.getBookThumbnailById(mediaId, ".$imageType")
        }

    val previewTitle: String
        get() {
            return if (!TextUtils.isEmpty(title.englishName) &&
                !NULL.equals(title.englishName, ignoreCase = true)
            ) {
                title.englishName + "\n"
            } else if (!TextUtils.isEmpty(title.japaneseName) &&
                !NULL.equals(title.japaneseName, ignoreCase = true)
            ) {
                title.japaneseName + "\n"
            } else if (!TextUtils.isEmpty(title.pretty) &&
                !NULL.equals(title.pretty, ignoreCase = true)
            ) {
                title.pretty
            } else ""
        }

    val usefulName: String
        get() {
            return when {
                title.englishName.isNotBlank() -> title.englishName
                title.japaneseName.isNotBlank() -> title.japaneseName
                title.pretty.isNotBlank() -> title.pretty
                else -> ""
            }
        }

    val language: String
        get() {
            return when {
                title.englishName.contains(ENG, ignoreCase = true) -> Constants.ENGLISH_LANG
                title.englishName.contains(CN, ignoreCase = true) -> Constants.CHINESE_LANG
                else -> Constants.JAPANESE_LANG
            }
        }

    val logString: String
        get() {
            return "Book title: eng=${title.englishName}\n" +
                    "            japanese=${title.japaneseName}\n" +
                    "            pretty=${title.pretty}\n"
        }

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readParcelable(
            BookTitle::class.java.classLoader
        ) ?: BookTitle.defaultInstance,
        parcel.readParcelable(
            BookImages::class.java.classLoader
        ) ?: BookImages.defaultInstance,
        parcel.readString().orEmpty(),
        parcel.readLong(),
        parcel.createTypedArrayList(Tag) ?: emptyList(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bookId)
        parcel.writeString(mediaId)
        parcel.writeParcelable(title, flags)
        parcel.writeParcelable(bookImages, flags)
        parcel.writeString(scanlator)
        parcel.writeLong(updateAt)
        parcel.writeTypedList(tags)
        parcel.writeInt(numOfPages)
        parcel.writeInt(numOfFavorites)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        private const val ENG = "[English]"
        private const val CN = "[Chinese]"
        private const val NULL = "null"

        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }
}
