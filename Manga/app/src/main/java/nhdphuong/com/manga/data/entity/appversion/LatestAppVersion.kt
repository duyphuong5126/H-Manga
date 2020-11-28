package nhdphuong.com.manga.data.entity.appversion

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants

data class LatestAppVersion(
    @field:SerializedName(Constants.LATEST_APP_VERSION_NUMBER) val versionNumber: Int,
    @field:SerializedName(Constants.LATEST_APP_VERSION_CODE) val versionCode: String
)
