package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.RESULT

data class RecommendedDoujinshis(@field:SerializedName(RESULT) val doujinshiList: ArrayList<Doujinshi>)
