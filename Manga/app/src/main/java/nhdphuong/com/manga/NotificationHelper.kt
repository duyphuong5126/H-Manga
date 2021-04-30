package nhdphuong.com.manga

import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper {
    companion object {
        private const val CHANNEL_ID = Constants.NOTIFICATION_CHANNEL_ID

        fun sendNotification(notification: Notification, notificationId: Int): Int {
            val notificationManagerCompat = NotificationManagerCompat.from(
                NHentaiApp.instance.applicationContext
            )
            notificationManagerCompat.notify(notificationId, notification)
            return notificationId
        }

        fun sendNotification(
            title: String,
            priority: Int,
            content: String,
            usedAppIcon: Boolean,
            notificationId: Int = System.currentTimeMillis().toInt(),
            pendingIntent: PendingIntent? = null
        ): Int {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
                .setOnlyAlertOnce(true)
            pendingIntent?.let(notificationBuilder::setContentIntent)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app_notification)
            }

            return sendNotification(notificationBuilder.build(), notificationId)
        }

        fun sendBigContentNotification(
            title: String,
            priority: Int,
            content: String = "",
            usedAppIcon: Boolean = true,
            notificationId: Int = System.currentTimeMillis().toInt(),
            pendingIntent: PendingIntent? = null
        ): Int {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setPriority(priority)
                .setOnlyAlertOnce(true)
            if (content.isNotBlank()) {
                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .bigText(content)
                    .setSummaryText(content)
                notificationBuilder.setStyle(bigTextStyle)
            }
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app_notification)
            }
            pendingIntent?.let { notificationBuilder.setContentIntent(it) }

            return sendNotification(notificationBuilder.build(), notificationId)
        }

        fun cancelNotification(vararg notificationIds: Int) {
            val notificationManagerCompat = NotificationManagerCompat.from(
                NHentaiApp.instance.applicationContext
            )
            notificationIds.forEach(notificationManagerCompat::cancel)
        }
    }
}
