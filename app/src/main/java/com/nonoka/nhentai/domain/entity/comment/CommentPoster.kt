package com.nonoka.nhentai.domain.entity.comment

import com.google.gson.annotations.SerializedName
import com.nonoka.nhentai.domain.entity.AVATAR_URL
import com.nonoka.nhentai.domain.entity.ID
import com.nonoka.nhentai.domain.entity.IS_STAFF
import com.nonoka.nhentai.domain.entity.IS_SUPER_USER
import com.nonoka.nhentai.domain.entity.NHENTAI_I
import com.nonoka.nhentai.domain.entity.SLUG
import com.nonoka.nhentai.domain.entity.USER_NAME

data class CommentPoster(
    @field:SerializedName(ID) val id: Int,
    @field:SerializedName(USER_NAME) val userName: String,
    @field:SerializedName(SLUG) val slug: String,
    @field:SerializedName(AVATAR_URL) private val avatarPath: String,
    @field:SerializedName(IS_SUPER_USER) val isSuperUser: Boolean,
    @field:SerializedName(IS_STAFF) val isStaff: Boolean,
) {
    val avatarUrl get() = "$NHENTAI_I/$avatarPath"
}