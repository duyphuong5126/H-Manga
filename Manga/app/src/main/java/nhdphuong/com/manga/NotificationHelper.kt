package nhdphuong.com.manga

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import nhdphuong.com.manga.features.home.HomeActivity

class NotificationHelper {
    companion object {
        private const val CHANNEL_ID = Constants.NOTIFICATION_CHANNEL_ID

        private fun sendNotification(notification: Notification, notificationId: Int): Int {
            val notificationManagerCompat = NotificationManagerCompat.from(NHentaiApp.instance.applicationContext)
            notificationManagerCompat.notify(notificationId, notification)
            return notificationId
        }

        fun sendNotification(title: String, priority: Int, content: String, usedAppIcon: Boolean): Int {
            val notificationBuilder = NotificationCompat.Builder(NHentaiApp.instance.applicationContext, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app)
            }

            return sendNotification(notificationBuilder.build(), System.currentTimeMillis().toInt())
        }

        fun sendBigContentNotification(title: String, priority: Int, content: String, usedAppIcon: Boolean): Int {
            val bigTextStyle = NotificationCompat.BigTextStyle()
                    .bigText(content)
                    .setSummaryText(content)
            val notificationBuilder = NotificationCompat.Builder(NHentaiApp.instance.applicationContext, CHANNEL_ID)
                    .setStyle(bigTextStyle)
                    .setContentTitle(title)
                    .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app)
            }

            return sendNotification(notificationBuilder.build(), System.currentTimeMillis().toInt())
        }

        fun sendNotification(title: String, priority: Int, content: String, usedAppIcon: Boolean, allowTap: Boolean): Int {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app)
            }

            if (allowTap) {
                val intent = Intent(context, HomeActivity::class.java)
                notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
            }

            return sendNotification(notificationBuilder.build(), System.currentTimeMillis().toInt())
        }

        fun cancelNotification(notificationId: Int) {
            val notificationManagerCompat = NotificationManagerCompat.from(NHentaiApp.instance.applicationContext)
            notificationManagerCompat.cancel(notificationId)
        }

        fun updateNotification(notificationId: Int, title: String, priority: Int, content: String, usedAppIcon: Boolean) {
            val notificationBuilder = NotificationCompat.Builder(NHentaiApp.instance.applicationContext, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app)
            }

            sendNotification(notificationBuilder.build(), notificationId)
        }

        fun updateNotification(notificationId: Int, title: String, priority: Int, content: String, usedAppIcon: Boolean, allowTap: Boolean) {
            val context = NHentaiApp.instance.applicationContext
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(priority)
            if (usedAppIcon) {
                notificationBuilder.setSmallIcon(R.drawable.ic_app)
            }

            if (allowTap) {
                val intent = Intent(context, HomeActivity::class.java)
                notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
            }

            sendNotification(notificationBuilder.build(), notificationId)
        }
    }
}