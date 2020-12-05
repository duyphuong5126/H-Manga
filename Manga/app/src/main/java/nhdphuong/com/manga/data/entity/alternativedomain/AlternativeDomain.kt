package nhdphuong.com.manga.data.entity.alternativedomain

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants.Companion.ALTERNATIVE_DOMAIN_ID
import nhdphuong.com.manga.Constants.Companion.ALTERNATIVE_HOME_URL
import nhdphuong.com.manga.Constants.Companion.ALTERNATIVE_IMAGE_URL
import nhdphuong.com.manga.Constants.Companion.ALTERNATIVE_THUMBNAIL_URL

data class AlternativeDomain(
    @field:SerializedName(ALTERNATIVE_DOMAIN_ID) val domainId: String,
    @field:SerializedName(ALTERNATIVE_HOME_URL) val homeUrl: String,
    @field:SerializedName(ALTERNATIVE_IMAGE_URL) val imageUrl: String,
    @field:SerializedName(ALTERNATIVE_THUMBNAIL_URL) val thumbnailUrl: String
)