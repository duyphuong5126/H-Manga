package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.IMAGE_HEIGHT
import com.nonoka.nhentai.domain.entity.IMAGE_TYPE
import com.nonoka.nhentai.domain.entity.IMAGE_WIDTH
import com.nonoka.nhentai.domain.entity.JPG
import com.nonoka.nhentai.domain.entity.PNG
import com.nonoka.nhentai.domain.entity.PNG_TYPE
import com.nonoka.nhentai.domain.entity.WEBP
import com.nonoka.nhentai.domain.entity.WEBP_TYPE

data class ImageMeasurements(
    @field:SerializedName(IMAGE_TYPE) private val type: String,
    @field:SerializedName(IMAGE_WIDTH) val width: Int,
    @field:SerializedName(IMAGE_HEIGHT) val height: Int
) {
    val imageType: String
        get() = when (type.lowercase()) {
            PNG_TYPE -> PNG
            WEBP_TYPE -> WEBP
            else -> JPG
        }
}
