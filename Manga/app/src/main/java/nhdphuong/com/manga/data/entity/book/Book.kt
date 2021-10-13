package nhdphuong.com.manga.data.entity.book

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Constants.Companion.LANGUAGE
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
    @field:SerializedName(Constants.UPLOAD_DATE) var updateAt: Long,
    @field:SerializedName(Constants.TAGS_LIST) val tags: List<Tag>,
    @field:SerializedName(Constants.NUM_PAGES) var numOfPages: Int,
    @field:SerializedName(Constants.NUM_FAVORITES) var numOfFavorites: Int
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
    private var _language: String? = null
    val language: String
        get() {
            if (!_language.isNullOrBlank()) {
                return _language!!
            }
            var hasEnglishTag = false
            var hasChineseTag = false
            var hasJapaneseTag = false
            tags.forEach {
                val name = it.name.trim().lowercase()
                if (it.type == LANGUAGE && name == ENG_TAG) {
                    hasEnglishTag = true
                    return@forEach
                }

                if (it.type == LANGUAGE && name == CHINESE_TAG) {
                    hasChineseTag = true
                    return@forEach
                }

                if (it.type == LANGUAGE && name == JAPANESE_TAG) {
                    hasJapaneseTag = true
                    return@forEach
                }
            }
            _language = when {
                hasEnglishTag || title.englishName.contains(
                    ENG,
                    ignoreCase = true
                ) -> Constants.ENGLISH_LANG
                hasChineseTag || title.englishName.contains(
                    CN,
                    ignoreCase = true
                ) -> Constants.CHINESE_LANG
                hasJapaneseTag -> Constants.JAPANESE_LANG
                else -> {
                    tags.firstOrNull {
                        val name = it.name.trim().lowercase()
                        it.type == LANGUAGE && name != TRANSLATED_TAG && name != REWRITE_TAG && name != TEXT_CLEANED_TAG
                    }?.name?.let {
                        if (it.length <= 3) {
                            it
                        } else {
                            it.substring(0, 3)
                        }
                    }.orEmpty()
                }
            }
            return _language.orEmpty()
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

    fun correctData() {
        if (numOfPages != bookImages.pages.size) {
            numOfPages = bookImages.pages.size
        }
        if (numOfFavorites < 0) {
            numOfFavorites = 0
        }
        if (updateAt < 0) {
            updateAt = System.currentTimeMillis()
        }
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        private const val ENG = "[English]"
        private const val CN = "[Chinese]"
        private const val NULL = "null"

        private const val ENG_TAG = "english"
        private const val CHINESE_TAG = "chinese"
        private const val JAPANESE_TAG = "japanese"

        private const val TRANSLATED_TAG = "translated"
        private const val REWRITE_TAG = "rewrite"
        private const val TEXT_CLEANED_TAG = "text cleaned"

        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }
}
