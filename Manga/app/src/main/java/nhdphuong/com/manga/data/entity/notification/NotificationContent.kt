package nhdphuong.com.manga.data.entity.notification

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants.Companion.GLOBAL_NOTIFICATION_ACTION
import nhdphuong.com.manga.Constants.Companion.GLOBAL_NOTIFICATION_MESSAGE
import nhdphuong.com.manga.Constants.Companion.GLOBAL_NOTIFICATION_TITLE
import nhdphuong.com.manga.Constants.Companion.GLOBAL_NOTIFICATION_URL

data class NotificationContent(
    @field:SerializedName(GLOBAL_NOTIFICATION_TITLE) val title: String,
    @field:SerializedName(GLOBAL_NOTIFICATION_MESSAGE) val message: String,
    @field:SerializedName(GLOBAL_NOTIFICATION_ACTION) val action: String,
    @field:SerializedName(GLOBAL_NOTIFICATION_URL) val externalUrl: String
)
