package nhdphuong.com.manga.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.Constants.Companion.APP_UPGRADE_NOTIFICATION_ID
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.features.NavigationRedirectActivity
import java.util.concurrent.TimeUnit

class VersionCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    private val masterDataRepository: MasterDataRepository
        get() {
            return (applicationContext as NHentaiApp).masterDataRepository
        }

    private var upgradeTitleTemplate = appContext.getString(R.string.app_upgrade_notification_title)
    private var upgradeMessage = appContext.getString(R.string.app_upgrade_notification_message)

    private val logger = Logger("VersionCheckWorker")

    override fun doWork(): Result {
        logger.d("Start checking new version")
        masterDataRepository.getAppVersion(onSuccess = { latestVersion ->
            logger.d("Latest version: $latestVersion")
            val isLatestVersion = BuildConfig.VERSION_CODE == latestVersion.versionNumber
            if (!isLatestVersion) {
                sendUpgradeNotification(latestVersion.versionCode)
            }
        }, onError = { error ->
            logger.e("Failed to get app version with error: $error")
        })
        return Result.success()
    }

    private fun sendUpgradeNotification(latestVersionCode: String) {
        val title = String.format(upgradeTitleTemplate, latestVersionCode)
        val message = upgradeMessage
        val notificationIntent = Intent(applicationContext, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            notificationIntent, Intent.FILL_IN_ACTION
        )
        NotificationHelper.cancelNotification(APP_UPGRADE_NOTIFICATION_ID)
        NotificationHelper.sendNotification(
            title, PRIORITY_DEFAULT, message, true, APP_UPGRADE_NOTIFICATION_ID, pendingIntent
        )
    }

    companion object {
        private const val WORK_NAME = "VersionCheckWorker"

        @JvmStatic
        fun start(context: Context) {
            val versionCheckWorkRequest = PeriodicWorkRequest.Builder(
                VersionCheckWorker::class.java,
                12,
                TimeUnit.HOURS,
                10,
                TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                versionCheckWorkRequest
            )
        }
    }
}