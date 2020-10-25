package nhdphuong.com.manga.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_STARTED
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_DOWNLOADING_FAILED
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.TOTAL
import nhdphuong.com.manga.Constants.Companion.PROGRESS
import nhdphuong.com.manga.Constants.Companion.DOWNLOADED
import nhdphuong.com.manga.Constants.Companion.DOWNLOADING_FAILED_COUNT
import nhdphuong.com.manga.DownloadManager.BookDownloadCallback
import nhdphuong.com.manga.DownloadManager.Companion.BookDownloader as bookDownloader
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingProgress
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingFailure
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.usecase.DownloadBookUseCase
import javax.inject.Inject
import android.app.PendingIntent
import android.content.Intent.FILL_IN_ACTION
import androidx.core.app.NotificationCompat
import nhdphuong.com.manga.R
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.Constants.Companion.NOTIFICATION_ID
import nhdphuong.com.manga.Constants.Companion.NOTIFICATION_CHANNEL_ID
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.features.NavigationRedirectActivity


class BookDownloadingService : IntentService("BookDownloadingService"), BookDownloadCallback {

    @Inject
    lateinit var downloadBookUseCase: DownloadBookUseCase

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        NHentaiApp.instance.applicationComponent.inject(this)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, FILL_IN_ACTION
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(getString(R.string.downloading_book))
            .setContentIntent(pendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            NotificationHelper.sendNotification(notification, NOTIFICATION_ID)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (!bookDownloader.isDownloading) {
            intent?.extras?.getParcelable<Book>(BOOK)?.let { book ->
                downloadBookUseCase.execute(book)
                    .doOnSubscribe {
                        Logger.d(TAG, "Start downloading book ${book.bookId}")
                        bookDownloader.setDownloadCallback(this)
                        bookDownloader.startDownloading(book.bookId, book.numOfPages)
                    }
                    .subscribeBy(onNext = { result ->
                        when (result) {
                            is DownloadingProgress -> {
                                bookDownloader.updateProgress(book.bookId, result.progress)
                                Logger.d(
                                    TAG,
                                    "Downloaded ${result.progress}/${result.total} pages of book ${book.bookId}"
                                )
                            }

                            is DownloadingFailure -> {
                                Logger.d(
                                    TAG,
                                    "Failed to download ${result.fileUrl} of book ${book.bookId}: ${result.error.localizedMessage}"
                                )
                            }
                        }
                    }, onError = {
                        Logger.e(TAG, "Failure in downloading book ${book.mediaId} with error: $it")
                        bookDownloader.endDownloadingWithError(
                            book.bookId,
                            bookDownloader.progress,
                            bookDownloader.total
                        )
                    }, onComplete = {
                        Logger.d(TAG, "Finished downloading book ${book.bookId}")
                        bookDownloader.endDownloading(
                            book.bookId,
                            bookDownloader.progress,
                            bookDownloader.total
                        )
                    }).addTo(compositeDisposable)
            }
        }
    }

    override fun onDownloadingStarted(bookId: String, total: Int) {
        val data = Bundle().apply {
            putString(BOOK_ID, bookId)
            putInt(TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DOWNLOADING_STARTED, data)
    }

    override fun onProgressUpdated(bookId: String, progress: Int, total: Int) {
        val data = Bundle().apply {
            putString(BOOK_ID, bookId)
            putInt(PROGRESS, progress)
            putInt(TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DOWNLOADING_PROGRESS, data)
        sendDownloadingProgressNotification(bookId, progress, total)
    }

    override fun onDownloadingEnded(bookId: String, downloaded: Int, total: Int) {
        val data = Bundle().apply {
            putString(BOOK_ID, bookId)
            putInt(DOWNLOADED, downloaded)
            putInt(TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DOWNLOADING_COMPLETED, data)
        sendDownloadingCompletedNotification(bookId)
    }

    override fun onDownloadingEndedWithError(
        bookId: String,
        downloadingFailedCount: Int,
        total: Int
    ) {
        NotificationHelper.cancelNotification(NOTIFICATION_ID)
        val data = Bundle().apply {
            putString(BOOK_ID, bookId)
            putInt(DOWNLOADING_FAILED_COUNT, downloadingFailedCount)
            putInt(TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DOWNLOADING_FAILED, data)
        sendDownloadingFailedNotification(bookId, downloadingFailedCount, total)
    }

    private fun sendDownloadingProgressNotification(bookId: String, progress: Int, total: Int) {
        NotificationHelper.cancelNotification(NOTIFICATION_ID)
        val progressTitle = getString(R.string.downloading_in_progress)
        val notificationDescription = getString(
            R.string.book_progress_template, bookId, progress, total
        )
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            progressTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            notificationDescription,
            true,
            NOTIFICATION_ID,
            pendingIntent
        )
    }

    private fun sendDownloadingCompletedNotification(bookId: String) {
        NotificationHelper.cancelNotification(NOTIFICATION_ID)
        val successTitle = getString(R.string.downloading_completed)
        val successMessage = getString(R.string.downloading_completed_template, bookId)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            successTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            successMessage,
            true,
            System.currentTimeMillis().toInt(),
            pendingIntent
        )
    }

    private fun sendDownloadingFailedNotification(
        bookId: String, downloadingFailedCount: Int, total: Int
    ) {
        NotificationHelper.cancelNotification(NOTIFICATION_ID)
        val failureTitle = getString(R.string.downloading_failure)
        val failureMessage = getString(
            R.string.downloading_failed_template, downloadingFailedCount, total, bookId
        )
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            failureTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            failureMessage,
            true,
            System.currentTimeMillis().toInt(),
            pendingIntent
        )
    }

    companion object {
        private const val TAG = "BookDownloadingService"
        private const val BOOK = "book"

        @JvmStatic
        fun start(fromContext: Context, book: Book) {
            val intent = Intent(fromContext, BookDownloadingService::class.java)
            intent.putExtras(Bundle().apply {
                putParcelable(BOOK, book)
            })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fromContext.startForegroundService(intent)
            } else {
                fromContext.startService(intent)
            }
        }
    }
}
