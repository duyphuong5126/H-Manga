package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.TITLE_ENG
import com.nonoka.nhentai.domain.entity.TITLE_JAPANESE
import com.nonoka.nhentai.domain.entity.TITLE_PRETTY

data class DoujinshiTitle(
    @field:SerializedName(TITLE_ENG) val englishName: String?,
    @field:SerializedName(TITLE_JAPANESE) val japaneseName: String?,
    @field:SerializedName(TITLE_PRETTY) val prettyName: String?
)
