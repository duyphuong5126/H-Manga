package nhdphuong.com.manga.data.entity.comment

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.GALLERY_ID
import nhdphuong.com.manga.Constants.Companion.POSTER
import nhdphuong.com.manga.Constants.Companion.POST_DATE
import nhdphuong.com.manga.Constants.Companion.BODY

data class Comment(
    @field:SerializedName(ID) val id: String,
    @field:SerializedName(GALLERY_ID) val galleryId: String?,
    @field:SerializedName(POSTER) val poster: Poster?,
    @field:SerializedName(POST_DATE) private val _posDate: Long,
    @field:SerializedName(BODY) val body: String?
) {
    val posDate: Long? get() = _posDate * 1000
}
