package com.nonoka.nhentai.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.domain.entity.ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@HiltWorker
class DoujinshiDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val doujinshiRepository: DoujinshiRepository,
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val doujinshiId = inputData.getString(ID)
        if (doujinshiId != null) {
            doujinshiRepository.getDoujinshi(doujinshiId).doOnSuccess { doujinshi ->
                val context = applicationContext
                val loader = ImageLoader(context)
                val stepCount = doujinshi.images.pages.size + 1
                var progress = 0.0
                val folderPath = "images/${doujinshi.id}"
                doujinshi.images.pages.forEachIndexed { index, image ->
                    val imageUrl = doujinshi.getPageUrl(index + 1)
                    val imageName = "${index + 1}.${image.imageType}"
                    val downloadSuccess =
                        downloadImage(context, loader, imageUrl, imageName, folderPath)
                    if (downloadSuccess) {
                        progress++
                        setProgress(workDataOf(PROGRESS_KEY to progress / stepCount))
                    }
                }
                doujinshiRepository.setDownloadedDoujinshi(doujinshi, true)
                progress++
                setProgress(workDataOf(PROGRESS_KEY to progress / stepCount))
            }
        }
        Result.success()
    }

    private suspend fun downloadImage(
        context: Context,
        loader: ImageLoader,
        imageUrl: String,
        imageName: String,
        folderPath: String,
    ): Boolean = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .diskCacheKey(imageUrl)
            .data(imageUrl)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            val drawable = result.drawable
            if (drawable is BitmapDrawable) {
                val directory = File(context.filesDir, folderPath)
                if (!directory.exists()) {
                    directory.mkdir()
                }
                val imageFile = File(directory, imageName)
                try {
                    imageFile.createNewFile()
                    val bos = ByteArrayOutputStream()
                    drawable.bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        bos
                    )
                    val bitmapData = bos.toByteArray()
                    val fos = FileOutputStream(imageFile)
                    fos.write(bitmapData)
                    fos.flush()
                    fos.close()
                    return@withContext true
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        false
    }

    companion object {
        const val PROGRESS_KEY = "Progress"

        fun start(context: Context, doujinshiId: String): UUID {
            val workManager = WorkManager.getInstance(context)
            val inputData = Data.Builder()
                .putString(ID, doujinshiId)
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequest.Builder(DoujinshiDownloadWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            workManager.enqueue(request)
            return request.id
        }
    }
}