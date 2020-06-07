package nhdphuong.com.manga.supports

import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

interface IFileUtils {
    fun isStoragePermissionAccepted(): Boolean

    fun getImageDirectory(bookName: String): String

    fun refreshGallery(needToShowRefreshDialog: Boolean, vararg galleryPaths: String)

    fun getTagDirectory(): String

    fun deleteFile(path: String): Boolean

    fun deleteParentDirectory(childPath: String)
}

class FileUtils : IFileUtils {
    override fun isStoragePermissionAccepted(): Boolean {
        val context = NHentaiApp.instance.applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun getImageDirectory(bookName: String): String {
        return NHentaiApp.instance.getImageDirectory(bookName)
    }

    override fun refreshGallery(needToShowRefreshDialog: Boolean, vararg galleryPaths: String) {
        if (galleryPaths.isEmpty()) {
            return
        }
        val context = NHentaiApp.instance.applicationContext
        if (needToShowRefreshDialog) {
            BroadCastReceiverHelper.sendBroadCast(
                context, Constants.ACTION_SHOW_GALLERY_REFRESHING_DIALOG
            )
        }
        val total = galleryPaths.size
        val refreshedCount = AtomicInteger(0)
        MediaScannerConnection.scanFile(context, galleryPaths, null) { path, _ ->
            Logger.d(TAG, "$path was refreshed")
            val refreshedItems = refreshedCount.incrementAndGet()
            if (needToShowRefreshDialog && refreshedItems == total) {
                BroadCastReceiverHelper.sendBroadCast(
                    context, Constants.ACTION_DISMISS_GALLERY_REFRESHING_DIALOG
                )
            }
        }
    }


    override fun getTagDirectory(): String =
        "${NHentaiApp.instance.getTagDirectory()}/${Constants.TAGS.toLowerCase(Locale.US)}"

    override fun deleteFile(path: String): Boolean = File(path).delete()

    override fun deleteParentDirectory(childPath: String) {
        File(childPath).parentFile?.let { parentFile ->
            if (parentFile.isDirectory) {
                for (file in parentFile.listFiles().orEmpty()) {
                    file.delete()
                }
            }
            parentFile.delete()
        }
    }

    companion object {
        private const val TAG = "FileUtils"
    }
}
