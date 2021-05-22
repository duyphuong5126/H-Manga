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
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain
import nhdphuong.com.manga.service.TagsUpdateService
import nhdphuong.com.manga.usecase.DownloadBookUseCase
import nhdphuong.com.manga.usecase.LogAnalyticsEventUseCase
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
    }

    private val logger: Logger by lazy {
        Logger("NHentaiApp")
    }

    @Inject
    lateinit var mSharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var downloadBookUseCase: DownloadBookUseCase

    @Inject
    lateinit var logAnalyticsEventUseCase: LogAnalyticsEventUseCase

    private lateinit var mApplicationComponent: ApplicationComponent

    val applicationComponent get() = mApplicationComponent

    private val isExternalStorageWritable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    private val imagesDirectory: String
        get() {
            val rootDirectory = if (isExternalStorageWritable) {
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            } else {
                applicationContext.filesDir
            }.toString()
            return "$rootDirectory/${Constants.NHENTAI_DIRECTORY}"
        }

    private val downloadDirectory: String
        get() {
            return if (isExternalStorageWritable) {
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            } else {
                applicationContext.filesDir
            }.toString()
        }

    private val tagsDirectory: String
        get() {
            return "$downloadDirectory/${Constants.NHENTAI_DIRECTORY}"
        }

    val installationDirectory: String
        get() {
            return "$downloadDirectory/${Constants.NHENTAI_DIRECTORY}/apk"
        }

    fun getImageDirectory(bookName: String): String = "$imagesDirectory/$bookName"
    fun getTagDirectory(): String = "$tagsDirectory/${Constants.TAGS.lowercase(Locale.US)}"

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
        if (mSharedPreferencesManager.useAlternativeDomain) {
            ApiConstants.alternativeDomain = AlternativeDomain(
                domainId = mSharedPreferencesManager.activeAlternativeDomainId,
                homeUrl = mSharedPreferencesManager.alternativeHomeUrl,
                imageUrl = mSharedPreferencesManager.alternativeImageUrl,
                thumbnailUrl = mSharedPreferencesManager.alternativeThumbnailUrl
            )
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
                logger.d("Create notification channel")
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
}
