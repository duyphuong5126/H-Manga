package com.nonoka.nhentai.domain.entity.book

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.CHINESE_LANG
import com.nonoka.nhentai.domain.entity.ENGLISH_LANG
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.domain.entity.IMAGES
import com.nonoka.nhentai.domain.entity.JAPANESE_LANG
import com.nonoka.nhentai.domain.entity.LANGUAGE
import com.nonoka.nhentai.domain.entity.MEDIA_ID
import com.nonoka.nhentai.domain.entity.NHENTAI_T
import com.nonoka.nhentai.domain.entity.NUM_FAVORITES
import com.nonoka.nhentai.domain.entity.NUM_PAGES
import com.nonoka.nhentai.domain.entity.SCANLATOR
import com.nonoka.nhentai.domain.entity.TAGS_LIST
import com.nonoka.nhentai.domain.entity.TITLE
import com.nonoka.nhentai.domain.entity.UPLOAD_DATE

data class Doujinshi(
    @field:SerializedName(ID) val bookId: String,
    @field:SerializedName(MEDIA_ID) val mediaId: String,
    @field:SerializedName(TITLE) val title: DoujinshiTitle,
    @field:SerializedName(IMAGES) val images: DoujinshiImages,
    @field:SerializedName(SCANLATOR) val scanlator: String,
    @field:SerializedName(UPLOAD_DATE) var updateAt: Long,
    @field:SerializedName(TAGS_LIST) val tags: List<Tag>,
    @field:SerializedName(NUM_PAGES) var numOfPages: Int,
    @field:SerializedName(NUM_FAVORITES) var numOfFavorites: Int
) : Parcelable {
    val thumbnail: String
    val previewTitle: String
    val usefulName: String
    val language: String

    init {
        val imageType = images.thumbnail.imageType
        thumbnail = "$NHENTAI_T/galleries/$mediaId/thumb$imageType"

        previewTitle = if (!title.englishName.isNullOrBlank() &&
            !NULL.equals(title.englishName, ignoreCase = true)
        ) {
            title.englishName + "\n"
        } else if (!title.japaneseName.isNullOrBlank() &&
            !NULL.equals(title.japaneseName, ignoreCase = true)
        ) {
            "${title.japaneseName}\n"
        } else if (!title.prettyName.isNullOrBlank() &&
            !NULL.equals(title.prettyName, ignoreCase = true)
        ) {
            title.prettyName
        } else ""

        usefulName = when {
            !title.englishName.isNullOrBlank() -> title.englishName
            !title.japaneseName.isNullOrBlank() -> title.japaneseName
            !title.prettyName.isNullOrBlank() -> title.prettyName
            else -> ""
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
        language = when {
            hasEnglishTag || title.englishName?.contains(
                ENG,
                ignoreCase = true
            ) == true -> ENGLISH_LANG

            hasChineseTag || title.englishName?.contains(
                CN,
                ignoreCase = true
            ) == true -> CHINESE_LANG

            hasJapaneseTag -> JAPANESE_LANG

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
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readParcelable(
            DoujinshiTitle::class.java.classLoader,
        ) ?: DoujinshiTitle.defaultInstance,
        parcel.readParcelable(
            DoujinshiImages::class.java.classLoader
        ) ?: DoujinshiImages.defaultInstance,
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
        parcel.writeParcelable(images, flags)
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
        if (numOfPages != images.pages.size) {
            numOfPages = images.pages.size
        }
        if (numOfFavorites < 0) {
            numOfFavorites = 0
        }
        if (updateAt < 0) {
            updateAt = System.currentTimeMillis()
        }
    }

    companion object CREATOR : Parcelable.Creator<Doujinshi> {
        private const val ENG = "[English]"
        private const val CN = "[Chinese]"
        private const val NULL = "null"

        private const val ENG_TAG = "english"
        private const val CHINESE_TAG = "chinese"
        private const val JAPANESE_TAG = "japanese"

        private const val TRANSLATED_TAG = "translated"
        private const val REWRITE_TAG = "rewrite"
        private const val TEXT_CLEANED_TAG = "text cleaned"

        override fun createFromParcel(parcel: Parcel): Doujinshi {
            return Doujinshi(parcel)
        }

        override fun newArray(size: Int): Array<Doujinshi?> {
            return arrayOfNulls(size)
        }
    }
}
