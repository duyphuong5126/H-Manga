package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.IMAGE_HEIGHT
import com.nonoka.nhentai.domain.entity.IMAGE_TYPE
import com.nonoka.nhentai.domain.entity.IMAGE_WIDTH
import com.nonoka.nhentai.domain.entity.JPG
import com.nonoka.nhentai.domain.entity.PNG
import com.nonoka.nhentai.domain.entity.PNG_TYPE

data class ImageMeasurements(
    @field:SerializedName(IMAGE_TYPE) private val type: String,
    @field:SerializedName(IMAGE_WIDTH) val width: Int,
    @field:SerializedName(IMAGE_HEIGHT) val height: Int
) {
    val imageType: String
        get() = if (PNG_TYPE.equals(type, ignoreCase = true)) {
            PNG
        } else {
            JPG
        }
}
