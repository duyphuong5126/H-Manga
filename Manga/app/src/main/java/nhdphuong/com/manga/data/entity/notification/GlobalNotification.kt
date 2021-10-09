package nhdphuong.com.manga.data.entity.notification

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants.Companion.GLOBAL_NOTIFICATION_CONTENT
import nhdphuong.com.manga.Constants.Companion.HAS_GLOBAL_NOTIFICATION
import nhdphuong.com.manga.Constants.Companion.IS_GLOBAL_NOTIFICATION_INVALIDATED

data class GlobalNotification(
    @field:SerializedName(HAS_GLOBAL_NOTIFICATION) val hasNotification: Boolean,
    @field:SerializedName(IS_GLOBAL_NOTIFICATION_INVALIDATED) val isInvalidated: Boolean,
    @field:SerializedName(GLOBAL_NOTIFICATION_CONTENT) val content: NotificationContent
)
