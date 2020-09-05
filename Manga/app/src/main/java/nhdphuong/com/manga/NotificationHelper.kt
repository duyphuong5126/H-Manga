package nhdphuong.com.manga

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import nhdphuong.com.manga.features.home.HomeActivity

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

        @Suppress("unused")
        fun sendNotification(
            title: String,
            priority: Int,
            content: String,
            usedAppIcon: Boolean,
            notificationId: Int = System.currentTimeMillis().toInt()
        ): Int {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
                .setOnlyAlertOnce(true)
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

        @Suppress("unused")
        fun sendNotification(
            title: String, priority: Int, content: String, usedAppIcon: Boolean, allowTap: Boolean
        ): Int {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app_notification)
            }

            if (allowTap) {
                val intent = Intent(context, HomeActivity::class.java)
                notificationBuilder.setContentIntent(
                    PendingIntent.getActivity(context, 0, intent, 0)
                )
            }

            return sendNotification(notificationBuilder.build(), System.currentTimeMillis().toInt())
        }

        fun cancelNotification(notificationId: Int) {
            val notificationManagerCompat = NotificationManagerCompat.from(
                NHentaiApp.instance.applicationContext
            )
            notificationManagerCompat.cancel(notificationId)
        }

        @Suppress("unused")
        fun updateNotification(
            notificationId: Int, title: String, priority: Int, content: String, usedAppIcon: Boolean
        ) {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app_notification)
            }

            sendNotification(notificationBuilder.build(), notificationId)
        }

        @Suppress("unused")
        fun updateNotification(
            notificationId: Int,
            title: String,
            priority: Int,
            content: String,
            usedAppIcon: Boolean,
            allowTap: Boolean
        ) {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app_notification)
            }

            if (allowTap) {
                val intent = Intent(context, HomeActivity::class.java)
                notificationBuilder.setContentIntent(
                    PendingIntent.getActivity(context, 0, intent, 0)
                )
            }

            sendNotification(notificationBuilder.build(), notificationId)
        }
    }
}
