package com.nonoka.nhentai.domain.entity.doujinshi

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.NUM_PAGES
import com.nonoka.nhentai.domain.entity.PER_PAGE
import com.nonoka.nhentai.domain.entity.RESULT

data class DoujinshisResult(
    @field:SerializedName(RESULT) val doujinshiList: List<Doujinshi>,
    @field:SerializedName(NUM_PAGES) var numOfPages: Long,
    @field:SerializedName(PER_PAGE) val numOfBooksPerPage: Int
) {
    init {
        doujinshiList.forEach(Doujinshi::correctData)
        if (numOfPages < 0) {
            numOfPages = DEFAULT_PAGES
        }
    }

    companion object {
        private const val DEFAULT_PAGES = 10L
    }
}
