package com.nonoka.nhentai.domain.entity.book

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.RESULT
import java.util.LinkedList

data class RecommendBook(@field:SerializedName(RESULT) val doujinshiList: LinkedList<Doujinshi>)
