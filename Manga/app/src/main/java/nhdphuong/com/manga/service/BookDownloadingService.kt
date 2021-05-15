package nhdphuong.com.manga.service

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
import nhdphuong.com.manga.data.entity.DownloadingResult
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingProgress
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingFailure
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingCompleted
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.usecase.DownloadBookUseCase
import javax.inject.Inject
import android.app.PendingIntent
import android.content.Intent.FILL_IN_ACTION
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import nhdphuong.com.manga.Constants.Companion.EVENT_DOWNLOADED_BOOK
import nhdphuong.com.manga.Constants.Companion.EVENT_DOWNLOAD_BOOK
import nhdphuong.com.manga.Constants.Companion.EVENT_FAILED_TO_DOWNLOAD_BOOK
import nhdphuong.com.manga.R
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.Constants.Companion.BOOK_DOWNLOADING_NOTIFICATION_ID
import nhdphuong.com.manga.Constants.Companion.NOTIFICATION_CHANNEL_ID
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_ANALYTICS_BOOK_ID
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_ERROR
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.analytics.AnalyticsParam
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.usecase.GetOldestPendingDownloadBookUseCase
import nhdphuong.com.manga.usecase.LogAnalyticsEventUseCase
import nhdphuong.com.manga.usecase.RemoveBookFromPendingDownloadListUseCase
import nhdphuong.com.manga.usecase.StartBookDownloadingUseCase

class BookDownloadingService : JobIntentService(), BookDownloadCallback {

    @Inject
    lateinit var downloadBookUseCase: DownloadBookUseCase

    @Inject
    lateinit var removeBookFromPendingDownloadListUseCase: RemoveBookFromPendingDownloadListUseCase

    @Inject
    lateinit var getOldestPendingDownloadBookUseCase: GetOldestPendingDownloadBookUseCase

    @Inject
    lateinit var startBookDownloadingUseCase: StartBookDownloadingUseCase

    @Inject
    lateinit var logAnalyticsEventUseCase: LogAnalyticsEventUseCase

    private val compositeDisposable = CompositeDisposable()

    private var downloadingBook = ""
    private var downloadingInProgress = ""
    private var bookProgressTemplate = ""
    private var downloadingCompleted = ""
    private var downloadingCompletedTemplate = ""
    private var downloadingFailed = ""
    private var downloadingFailedTemplate = ""

    private val logger: Logger by lazy {
        Logger("BookDownloadingService")
    }

    override fun onCreate() {
        super.onCreate()
        NHentaiApp.instance.applicationComponent.inject(this)

        downloadingBook = getString(R.string.downloading_book)
        downloadingInProgress = getString(R.string.downloading_in_progress)
        bookProgressTemplate = getString(R.string.book_progress_template)
        downloadingCompleted = getString(R.string.downloading_completed)
        downloadingCompletedTemplate = getString(R.string.downloading_completed_template)
        downloadingFailed = getString(R.string.downloading_failure)
        downloadingFailedTemplate = getString(R.string.downloading_failed_template)

        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, FILL_IN_ACTION
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(downloadingBook)
            .setContentIntent(pendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(BOOK_DOWNLOADING_NOTIFICATION_ID, notification)
        } else {
            NotificationHelper.sendNotification(notification, BOOK_DOWNLOADING_NOTIFICATION_ID)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onHandleWork(intent: Intent) {
        if (!bookDownloader.isDownloading) {
            intent.extras?.getParcelable<Book>(BOOK)?.let { book ->
                downloadBookUseCase.execute(book)
                    .doOnSubscribe {
                        logger.d("Start downloading book ${book.bookId}")
                        bookDownloader.setDownloadCallback(this)
                        bookDownloader.startDownloading(book.bookId, book.numOfPages)

                        val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, book.bookId)
                        logAnalyticsEventUseCase.execute(EVENT_DOWNLOAD_BOOK, bookIdParam)
                            .subscribe()
                            .addTo(compositeDisposable)
                    }
                    .subscribeBy(onNext = { result ->
                        downloadingStatusMap[book.bookId] = result
                        when (result) {
                            is DownloadingProgress -> {
                                bookDownloader.updateProgress(book.bookId, result.progress)
                                logger.d("Downloaded ${result.progress}/${result.total} pages of book ${book.bookId}")
                            }

                            is DownloadingFailure -> {
                                logger.e("Failed to download ${result.fileUrl} of book ${book.bookId}: ${result.error.localizedMessage}")
                            }
                            else -> {
                            }
                        }
                    }, onError = {
                        logger.e("Failure in downloading book ${book.mediaId} with error: $it")
                        downloadingStatusMap[book.bookId] = DownloadingFailure("", it)
                        bookDownloader.endDownloadingWithError(
                            book.bookId,
                            bookDownloader.progress,
                            bookDownloader.total
                        )

                        val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, book.bookId)
                        val error = AnalyticsParam(PARAM_NAME_ERROR, it.javaClass.simpleName)
                        logAnalyticsEventUseCase.execute(
                            EVENT_FAILED_TO_DOWNLOAD_BOOK,
                            bookIdParam,
                            error
                        ).subscribe()
                            .addTo(compositeDisposable)
                    }, onComplete = {
                        logger.d("Finished downloading book ${book.bookId}")
                        downloadingStatusMap[book.bookId] = DownloadingCompleted
                        bookDownloader.endDownloading(
                            book.bookId,
                            bookDownloader.progress,
                            bookDownloader.total
                        )

                        val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, book.bookId)
                        logAnalyticsEventUseCase.execute(EVENT_DOWNLOADED_BOOK, bookIdParam)
                            .subscribe()
                            .addTo(compositeDisposable)

                        removeBookFromPendingDownloadListUseCase.execute(book.bookId)
                            .andThen(getOldestPendingDownloadBookUseCase.execute())
                            .flatMapCompletable(startBookDownloadingUseCase::execute)
                            .subscribe({
                                logger.d("Pending list checking completed")
                            }, {
                                logger.d("Pending list checking failed with error $it")
                            }).addTo(compositeDisposable)

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
        val data = Bundle().apply {
            putString(BOOK_ID, bookId)
            putInt(DOWNLOADING_FAILED_COUNT, downloadingFailedCount)
            putInt(TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DOWNLOADING_FAILED, data)
        sendDownloadingFailedNotification(bookId, downloadingFailedCount, total)
    }

    private fun sendDownloadingProgressNotification(bookId: String, progress: Int, total: Int) {
        val progressTitle = downloadingInProgress
        val notificationDescription = String.format(bookProgressTemplate, bookId, progress, total)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            progressTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            notificationDescription,
            true,
            BOOK_DOWNLOADING_NOTIFICATION_ID,
            pendingIntent
        )
    }

    private fun sendDownloadingCompletedNotification(bookId: String) {
        val successTitle = downloadingCompleted
        val successMessage = String.format(downloadingCompletedTemplate, bookId)
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
        val failureTitle = downloadingFailed
        val failureMessage =
            String.format(downloadingFailedTemplate, downloadingFailedCount, total, bookId)
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
        private const val BOOK = "book"

        private val downloadingStatusMap = HashMap<String, DownloadingResult>()

        fun clearStatus(bookId: String) {
            downloadingStatusMap.remove(bookId)
        }

        fun getLastStatus(bookId: String): DownloadingResult? {
            return if (downloadingStatusMap.containsKey(bookId)) {
                downloadingStatusMap[bookId]
            } else null
        }

        private const val JOB_ID = 7125

        @JvmStatic
        fun start(fromContext: Context, book: Book) {
            val intent = Intent(fromContext, BookDownloadingService::class.java)
            intent.putExtras(Bundle().apply {
                putParcelable(BOOK, book)
            })
            enqueueWork(fromContext, BookDownloadingService::class.java, JOB_ID, intent)
        }
    }
}
