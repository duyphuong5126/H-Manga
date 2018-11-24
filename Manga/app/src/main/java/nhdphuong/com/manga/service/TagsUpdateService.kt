package nhdphuong.com.manga.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.repository.TagRepository
import java.util.*
import javax.inject.Inject

class TagsUpdateService : Service() {
    private var mUpdateTagTimer: Timer? = null

    @Inject
    lateinit var mTagRepository: TagRepository

    @Inject
    lateinit var mSharedPreferencesManager: SharedPreferencesManager

    private val mTagsDownloadManager = DownloadManager.Companion.TagsDownloadManager

    override fun onBind(intent: Intent?): IBinder? = TagsUpdateServiceBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "onStartCommand")
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "onCreate")
        NHentaiApp.instance.applicationComponent.inject(this)

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Logger.d(TAG, "TimerTask time: ${System.currentTimeMillis()}, tags updated=${mSharedPreferencesManager.tagsDataDownloaded}")
            }
        }, 0, TIME_INTERVAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy")
    }

    fun cancelTask() {
        Logger.d(TAG, "mUpdateTagTimer[$mUpdateTagTimer] will be terminated")
        mUpdateTagTimer?.cancel()
    }

    companion object {
        private const val TAG = "TagsUpdateService"
        private const val TIME_INTERVAL = 5000L
    }

    inner class TagsUpdateServiceBinder : Binder() {
        val service: TagsUpdateService = this@TagsUpdateService
    }

    private fun checkForNewVersion() {
        mTagRepository.getCurrentVersion(onSuccess = { currentVersion ->
            if (mSharedPreferencesManager.currentTagVersion != currentVersion) {
                mTagsDownloadManager.startDownloading()
                mTagRepository.fetchAllTagLists { isSuccess ->
                    Logger.d(TAG, "Tags fetching completed, isSuccess=$isSuccess")
                    if (isSuccess) {
                        mSharedPreferencesManager.currentTagVersion = currentVersion
                    }
                    mTagsDownloadManager.stopDownloading()
                }
            }
        }, onError = {

        })
    }
}