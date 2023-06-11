package com.nonoka.nhentai.domain.entity.comment

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.BODY
import com.nonoka.nhentai.domain.entity.GALLERY_ID
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.domain.entity.POSTER
import com.nonoka.nhentai.domain.entity.POST_DATE

data class Comment(
    @field:SerializedName(ID) val id: Int,
    @field:SerializedName(GALLERY_ID) val galleryId: Int,
    @field:SerializedName(POSTER) val poster: CommentPoster,
    @field:SerializedName(POST_DATE) private val rawPostDate: Int,
    @field:SerializedName(BODY) val body: String,
) {
    val postDate: Long get() = rawPostDate * 1000L
}
