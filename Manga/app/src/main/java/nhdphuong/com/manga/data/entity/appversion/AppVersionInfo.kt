package nhdphuong.com.manga.data.entity.appversion

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants.Companion.LATEST_APP_VERSION_NUMBER
import nhdphuong.com.manga.Constants.Companion.LATEST_APP_VERSION_CODE
import nhdphuong.com.manga.Constants.Companion.IS_ACTIVATED
import nhdphuong.com.manga.Constants.Companion.WHATS_NEW
import nhdphuong.com.manga.Constants.Companion.DOWNLOAD_URL

data class AppVersionInfo(
    @field:SerializedName(LATEST_APP_VERSION_NUMBER) val versionNumber: Int,
    @field:SerializedName(LATEST_APP_VERSION_CODE) val versionCode: String,
    @field:SerializedName(IS_ACTIVATED) val isActivated: Boolean,
    @field:SerializedName(WHATS_NEW) val whatsNew: String,
    @field:SerializedName(DOWNLOAD_URL) val downloadUrl: String
)