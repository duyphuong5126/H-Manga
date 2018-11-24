package nhdphuong.com.manga

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.media.MediaScannerConnection
import android.os.IBinder
import nhdphuong.com.manga.service.TagsUpdateService

/*
 * Created by nhdphuong on 3/21/18.
 */
class NHentaiApp : Application() {
    companion object {
        private lateinit var mInstance: NHentaiApp
        val instance
            get() = mInstance
        private const val TAG = "NHentaiApp"
    }

    private lateinit var mApplicationComponent: ApplicationComponent

    val applicationComponent
        get() = mApplicationComponent

    private val isExternalStorageWritable: Boolean get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    private val imagesDirectory: String
        get() {
            val rootDirectory = if (isExternalStorageWritable) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            } else {
                applicationContext.filesDir.toString()
            }
            return "$rootDirectory/${Constants.NHENTAI_DIRECTORY}"
        }
    private val tagsDirectory: String
        get() {
            val rootDirectory = if (isExternalStorageWritable) {
                Environment.getExternalStorageDirectory().toString()
            } else {
                applicationContext.filesDir.toString()
            }
            return "$rootDirectory/${Constants.NHENTAI_DIRECTORY}"
        }

    fun getImageDirectory(mediaId: String): String = "$imagesDirectory/$mediaId"
    fun getTagDirectory(): String = "$tagsDirectory/${Constants.TAGS.toLowerCase()}"

    val isStoragePermissionAccepted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

    private var mUpdateTagsService: TagsUpdateService? = null
    private var mServiceConnection: ServiceConnection? = null

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        mApplicationComponent = DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()
        createNotificationChannel()
        mServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                mUpdateTagsService?.cancelTask()
                mUpdateTagsService = null
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mUpdateTagsService = (service as TagsUpdateService.TagsUpdateServiceBinder).service
            }
        }
    }

    fun startUpdateTagsService() {
        val intent = Intent(applicationContext, TagsUpdateService::class.java)
        mServiceConnection?.run {
            bindService(intent, this, BIND_AUTO_CREATE)
        }
    }

    fun refreshGallery(vararg galleryPaths: String) {
        MediaScannerConnection.scanFile(this, galleryPaths, null) { _, _ ->
            galleryPaths.size.let { pathCount ->
                if (pathCount > 1) {
                    Logger.d(TAG, "$pathCount paths of galleries are refreshed")
                } else {
                    Logger.d(TAG, "$pathCount path of gallery is refreshed")
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = Constants.NOTIFICATION_CHANNEL_ID
            val channelName = getString(R.string.notification_channel_name)
            val channelDescription = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.description = channelDescription
            getSystemService(NotificationManager::class.java)?.let { notificationManager ->
                Logger.d(TAG, "Create notification channel")
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
}