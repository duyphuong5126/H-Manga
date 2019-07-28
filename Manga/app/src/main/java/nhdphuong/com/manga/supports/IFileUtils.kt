package nhdphuong.com.manga.supports

import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp

interface IFileUtils {
    fun isStoragePermissionAccepted(): Boolean

    fun getImageDirectory(mediaId: String): String

    fun refreshGallery(vararg galleryPaths: String)

    fun getTagDirectory(): String
}

class FileUtils : IFileUtils {
    override fun isStoragePermissionAccepted(): Boolean {
        val context = NHentaiApp.instance.applicationContext
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun getImageDirectory(mediaId: String): String {
        return NHentaiApp.instance.getImageDirectory(mediaId)
    }

    override fun refreshGallery(vararg galleryPaths: String) {
        val context = NHentaiApp.instance.applicationContext
        MediaScannerConnection.scanFile(context, galleryPaths, null) { _, _ ->
            galleryPaths.size.let { pathCount ->
                if (pathCount > 1) {
                    Logger.d(TAG, "$pathCount paths of galleries are refreshed")
                } else {
                    Logger.d(TAG, "$pathCount path of gallery is refreshed")
                }
            }
        }
    }


    override fun getTagDirectory(): String = "${NHentaiApp.instance.getTagDirectory()}/${Constants.TAGS.toLowerCase()}"

    companion object {
        private const val TAG = "FileUtils"
    }
}
