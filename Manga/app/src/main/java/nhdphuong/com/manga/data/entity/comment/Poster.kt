package nhdphuong.com.manga.data.entity.comment

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.USER_NAME
import nhdphuong.com.manga.Constants.Companion.SLUG
import nhdphuong.com.manga.Constants.Companion.AVATAR_URL
import nhdphuong.com.manga.Constants.Companion.IS_SUPER_USER
import nhdphuong.com.manga.Constants.Companion.IS_STAFF

data class Poster(
    @field:SerializedName(ID) val id: String,
    @field:SerializedName(USER_NAME) val userName: String,
    @field:SerializedName(SLUG) val slug: String,
    @field:SerializedName(AVATAR_URL) val avatarUrl: String,
    @field:SerializedName(IS_SUPER_USER) val isSuperUser: Boolean,
    @field:SerializedName(IS_STAFF) val isStaff: Boolean
)
