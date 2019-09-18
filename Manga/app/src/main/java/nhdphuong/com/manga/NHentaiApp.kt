package nhdphuong.com.manga

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Environment
import android.os.IBinder
import nhdphuong.com.manga.service.TagsUpdateService
import java.util.Locale
import javax.inject.Inject

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

    @Inject
    lateinit var mSharedPreferencesManager: SharedPreferencesManager

    private lateinit var mApplicationComponent: ApplicationComponent

    val applicationComponent get() = mApplicationComponent

    private val isExternalStorageWritable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    private val imagesDirectory: String
        get() {
            val rootDirectory = if (isExternalStorageWritable) {
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ).toString()
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

    fun getImageDirectory(bookName: String): String = "$imagesDirectory/$bookName"
    fun getTagDirectory(): String = "$tagsDirectory/${Constants.TAGS.toLowerCase(Locale.US)}"

    /**
     * Debug mode only
     */
    val isCensored: Boolean
        get() {
            return if (this::mSharedPreferencesManager.isInitialized) {
                mSharedPreferencesManager.isCensored
            } else {
                false
            }
        }

    private var mUpdateTagsService: TagsUpdateService? = null
    private var mServiceConnection: ServiceConnection? = null

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
        mApplicationComponent.inject(this)
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

    fun suspendUpdateTagsService() {
        mUpdateTagsService?.suspendTask()
    }

    fun resumeUpdateTagsService() {
        mUpdateTagsService?.resumeTask()
    }

    fun restartApp() {
        applicationContext?.run {
            packageManager.getLaunchIntentForPackage(this.packageName)?.let { launchIntent ->
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(launchIntent)
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
