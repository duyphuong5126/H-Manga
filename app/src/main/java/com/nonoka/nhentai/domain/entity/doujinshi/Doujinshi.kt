package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.CHINESE_LANG
import com.nonoka.nhentai.domain.entity.ENGLISH_LANG
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.domain.entity.IMAGES
import com.nonoka.nhentai.domain.entity.JAPANESE_LANG
import com.nonoka.nhentai.domain.entity.LANGUAGE
import com.nonoka.nhentai.domain.entity.MEDIA_ID
import com.nonoka.nhentai.domain.entity.NHENTAI_I
import com.nonoka.nhentai.domain.entity.NHENTAI_T
import com.nonoka.nhentai.domain.entity.NUM_FAVORITES
import com.nonoka.nhentai.domain.entity.NUM_PAGES
import com.nonoka.nhentai.domain.entity.SCANLATOR
import com.nonoka.nhentai.domain.entity.TAGS_LIST
import com.nonoka.nhentai.domain.entity.TITLE
import com.nonoka.nhentai.domain.entity.UPLOAD_DATE

data class Doujinshi(
    @field:SerializedName(ID) val id: String,
    @field:SerializedName(MEDIA_ID) val mediaId: String,
    @field:SerializedName(TITLE) val title: DoujinshiTitle,
    @field:SerializedName(IMAGES) val images: DoujinshiImages,
    @field:SerializedName(SCANLATOR) val scanlator: String,
    @field:SerializedName(UPLOAD_DATE) var updateAt: Long,
    @field:SerializedName(TAGS_LIST) val tags: List<Tag>,
    @field:SerializedName(NUM_PAGES) var numOfPages: Int,
    @field:SerializedName(NUM_FAVORITES) var numOfFavorites: Int
) {
    val thumbnail: String
        get() = "$NHENTAI_T/galleries/$mediaId/thumb.${images.thumbnail.imageType}"

    val cover: String get() = "$NHENTAI_T/galleries/$mediaId/cover.${images.cover.imageType}"
    val coverRatio: Float
        get() {
            var ratio = 1f
            if (images.cover.width > 0 && images.cover.height > 0) {
                ratio = images.cover.width.toFloat() / images.cover.height
            }
            return ratio
        }

    val previewTitle: String
        get() {
            return if (!title.englishName.isNullOrBlank() &&
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
        }

    val previewThumbnailList: List<String>
        get() = images.pages.mapIndexed { index, imageMeasurements ->
            "$NHENTAI_T/galleries/$mediaId/${index + 1}t.${imageMeasurements.imageType}"
        }

    var usefulName: String = ""
        get() {
            if (field.isBlank()) {
                internalInit()
            }
            return field
        }
        private set
    val language: String
        get() {
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
            return when {
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

    var thumbnailRatio: Float = 0f
        get() {
            if (field == 0f) {
                internalInit()
            }
            return field
        }
        private set

    private fun internalInit() {
        var ratio = 1f
        if (images.thumbnail.width > 0 && images.thumbnail.height > 0) {
            ratio = images.thumbnail.width.toFloat() / images.thumbnail.height
        } else if (images.cover.width > 0 && images.cover.height > 0) {
            ratio = images.cover.width.toFloat() / images.cover.height
        } else {
            images.pages.firstOrNull { it.height > 0 && it.width > 0 }?.run {
                ratio = width.toFloat() / height
            }
        }
        thumbnailRatio = ratio

        usefulName = when {
            !title.englishName.isNullOrBlank() -> title.englishName
            !title.japaneseName.isNullOrBlank() -> title.japaneseName
            !title.prettyName.isNullOrBlank() -> title.prettyName
            else -> ""
        }
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

    private fun getGalleryUrl(mediaId: String): String = "$NHENTAI_I/galleries/$mediaId"

    fun getPageUrl(index: Int): String {
        return "${getGalleryUrl(mediaId)}/${index + 1}.${images.pages[index].imageType}"
    }

    companion object {
        private const val ENG = "[English]"
        private const val CN = "[Chinese]"
        private const val NULL = "null"

        private const val ENG_TAG = "english"
        private const val CHINESE_TAG = "chinese"
        private const val JAPANESE_TAG = "japanese"

        private const val TRANSLATED_TAG = "translated"
        private const val REWRITE_TAG = "rewrite"
        private const val TEXT_CLEANED_TAG = "text cleaned"
    }
}
