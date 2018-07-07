package nhdphuong.com.manga

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.media.MediaScannerConnection

/*
 * Created by nhdphuong on 3/21/18.
 */
class NHentaiApp : Application() {
    companion object {
        private lateinit var mInstance: NHentaiApp
        val instance
            get() = mInstance
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

    fun getImageDirectory(mediaId: String): String = "$imagesDirectory/$mediaId"

    val isStoragePermissionAccepted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        mApplicationComponent = DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()
    }

    fun refreshGallery(vararg galleryPaths: String) {
        MediaScannerConnection.scanFile(this, galleryPaths, null) { _, _ ->

        }
    }
}