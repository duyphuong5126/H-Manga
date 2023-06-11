package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.COUNT
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.domain.entity.NAME
import com.nonoka.nhentai.domain.entity.TYPE
import com.nonoka.nhentai.domain.entity.URL

data class Tag(
    @field:SerializedName(ID) val id: Long,

    @field:SerializedName(TYPE) val type: String,
    @field:SerializedName(NAME) val name: String,
    @field:SerializedName(URL) val url: String,
    @field:SerializedName(COUNT) val count: Long
)
