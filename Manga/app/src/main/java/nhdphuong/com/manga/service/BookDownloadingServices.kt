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
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingProgress
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingFailure
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.usecase.DownloadBookUseCase
import javax.inject.Inject
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.R


class BookDownloadingServices : IntentService("BookDownloadingServices"), BookDownloadCallback {

    @Inject
    lateinit var downloadBookUseCase: DownloadBookUseCase

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        NHentaiApp.instance.applicationComponent.inject(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                notificationIntent, Intent.FILL_IN_ACTION
            )

            val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app)
                .setContentText(getString(R.string.downloading_book))
                .setContentIntent(pendingIntent).build()

            startForeground(Constants.NOTIFICATION_ID, notification)
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
    }

    override fun onDownloadingEnded(bookId: String, downloaded: Int, total: Int) {
        val data = Bundle().apply {
            putString(BOOK_ID, bookId)
            putInt(DOWNLOADED, downloaded)
            putInt(TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DOWNLOADING_COMPLETED, data)
    }

    override fun onDownloadingEndedWithError(
        bookId: String,
        downloadingFailedCount: Int,
        total: Int
    ) {
        val data = Bundle().apply {
            putString(BOOK_ID, bookId)
            putInt(DOWNLOADING_FAILED_COUNT, downloadingFailedCount)
            putInt(TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DOWNLOADING_FAILED, data)
    }

    companion object {
        private const val TAG = "BookDownloadingServices"
        private const val BOOK = "book"
        @JvmStatic
        fun start(fromContext: Context, book: Book) {
            val intent = Intent(fromContext, BookDownloadingServices::class.java)
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