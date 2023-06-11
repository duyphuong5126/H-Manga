package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.COVER
import com.nonoka.nhentai.domain.entity.PAGES
import com.nonoka.nhentai.domain.entity.THUMBNAIL

data class DoujinshiImages(
    @field:SerializedName(PAGES) val pages: List<ImageMeasurements>,
    @field:SerializedName(COVER) val cover: ImageMeasurements,
    @field:SerializedName(THUMBNAIL) val thumbnail: ImageMeasurements
)
