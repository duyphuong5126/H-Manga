package nhdphuong.com.manga.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import nhdphuong.com.manga.Constants.Companion.TAG_UPDATING_NOTIFICATION_ID
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.features.NavigationRedirectActivity
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

class TagsUpdateService : Service() {
    companion object {
        private const val TIME_INTERVAL: Long = 2 * 60 * 1000L
    }

    private val logger: Logger by lazy {
        Logger("TagsUpdateService")
    }

    private var mUpdateTagTimer: Timer? = null

    @Inject
    lateinit var mMasterDataRepository: MasterDataRepository

    @Inject
    lateinit var mSharedPreferencesManager: SharedPreferencesManager

    @Volatile
    private var isUpdateTagSuspended: Boolean = false

    private val mTagsDownloadManager = DownloadManager.Companion.TagsDownloadManager

    override fun onBind(intent: Intent?): IBinder = TagsUpdateServiceBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        NHentaiApp.instance.applicationComponent.inject(this)

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (!isUpdateTagSuspended) {
                    checkForNewVersion()
                }
            }
        }, 0, TIME_INTERVAL)
    }

    fun cancelTask() {
        logger.d("mUpdateTagTimer[$mUpdateTagTimer] will be terminated")
        mUpdateTagTimer?.cancel()
    }

    fun suspendTask() {
        logger.d("Updating task is being suspended, task is running=${!isUpdateTagSuspended}")
        isUpdateTagSuspended = true
    }

    fun resumeTask() {
        logger.d("Updating task is being resumed, task is running=${!isUpdateTagSuspended}")
        isUpdateTagSuspended = false
    }

    inner class TagsUpdateServiceBinder : Binder() {
        val service: TagsUpdateService = this@TagsUpdateService
    }

    private fun checkForNewVersion() {
        mMasterDataRepository.getTagDataVersion(onSuccess = { newVersion ->
            if (mSharedPreferencesManager.currentTagVersion != newVersion) {
                logger.d("New version is available, new version: $newVersion, current version: ${mSharedPreferencesManager.currentTagVersion}")
                mTagsDownloadManager.startDownloading()
                mMasterDataRepository.fetchAllTagLists { isSuccess ->
                    logger.d("Tags fetching completed, isSuccess=$isSuccess")
                    if (isSuccess) {
                        mSharedPreferencesManager.currentTagVersion = newVersion
                        sendDownloadingCompletedNotification(newVersion)
                    } else {
                        sendDownloadingFailedNotification()
                    }
                    mTagsDownloadManager.stopDownloading()
                }
            } else {
                logger.d("App is already updated to the version $newVersion")
            }
        }, onError = {
            logger.e("Version fetching failed")
        })
    }

    private fun sendDownloadingCompletedNotification(newVersion: Long) {
        val successTitle = getString(R.string.downloading_completed)
        val successMessage = getString(R.string.tags_downloading_completed, newVersion)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            successTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            successMessage,
            true,
            TAG_UPDATING_NOTIFICATION_ID,
            pendingIntent
        )
    }

    private fun sendDownloadingFailedNotification() {
        val failureTitle = getString(R.string.downloading_failure)
        val failureMessage = getString(R.string.tags_downloading_failed)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            failureTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            failureMessage,
            true,
            TAG_UPDATING_NOTIFICATION_ID,
            pendingIntent
        )
    }
}
