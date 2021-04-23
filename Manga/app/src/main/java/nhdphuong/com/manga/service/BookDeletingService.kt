package nhdphuong.com.manga.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import nhdphuong.com.manga.R
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_FAILED
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.ACTION_DELETING_STARTED
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.data.entity.DeletingResult.DeletingProgress
import nhdphuong.com.manga.data.entity.DeletingResult.DeletingFailure
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.usecase.DeleteBookUseCase
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class BookDeletingService : JobIntentService() {
    @Inject
    lateinit var bookDeleteBookUseCase: DeleteBookUseCase

    private val compositeDisposable = CompositeDisposable()

    private var deletingBook = ""
    private var deletingProgress = ""
    private var bookProgressTemplate = ""
    private var deletingCompleted = ""
    private var deletingCompletedTemplate = ""
    private var deletingFailed = ""
    private var deletingFailedTemplate = ""

    override fun onCreate() {
        NHentaiApp.instance.applicationComponent.inject(this)
        super.onCreate()

        deletingBook = getString(R.string.deleting_book)
        deletingProgress = getString(R.string.deleting_in_progress)
        bookProgressTemplate = getString(R.string.book_progress_template)
        deletingCompleted = getString(R.string.deleting_completed)
        deletingCompletedTemplate = getString(R.string.deleting_completed_template)
        deletingFailed = getString(R.string.deleting_failure)
        deletingFailedTemplate = getString(R.string.deleting_failed_template)

        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, Intent.FILL_IN_ACTION
        )

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(deletingBook)
            .setContentIntent(pendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Constants.NOTIFICATION_ID, notification)
        } else {
            NotificationHelper.sendNotification(notification, Constants.NOTIFICATION_ID)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onHandleWork(intent: Intent) {
        intent.extras?.getString(BOOK_ID)?.takeIf { it.isNotBlank() }?.let { bookId ->
            while (isDeletingBook.get()) {
                Logger.d(TAG, "Another book is being deleted")
            }
            if (isDeletingBook.compareAndSet(false, true)) {
                val deletingFailedCount = AtomicInteger(0)
                bookDeleteBookUseCase.execute(bookId)
                    .doOnSubscribe {
                        onDeletingStarted(bookId)
                    }
                    .subscribe({ result ->
                        when (result) {
                            is DeletingProgress -> {
                                updateProgress(bookId, result.progress, result.total)
                            }

                            is DeletingFailure -> {
                                deletingFailedCount.incrementAndGet()
                            }
                        }
                    }, {
                        onDeletingEndedWithError(bookId, deletingFailedCount.get())
                        isDeletingBook.compareAndSet(true, false)
                    }, {
                        val failureCount = deletingFailedCount.get()
                        if (failureCount == 0) {
                            onDeletingEnded(bookId)
                        } else {
                            onDeletingEndedWithError(bookId, failureCount)
                        }
                        isDeletingBook.compareAndSet(true, false)
                    }).addTo(compositeDisposable)
            }
        }
    }


    private fun onDeletingStarted(bookId: String) {
        val data = Bundle().apply {
            putString(Constants.BOOK_ID, bookId)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DELETING_STARTED, data)
    }

    private fun updateProgress(bookId: String, progress: Int, total: Int) {
        val data = Bundle().apply {
            putString(Constants.BOOK_ID, bookId)
            putInt(Constants.PROGRESS, progress)
            putInt(Constants.TOTAL, total)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DELETING_PROGRESS, data)
        sendDeletingProgressNotification(bookId, progress, total)
    }

    private fun onDeletingEnded(bookId: String) {
        val data = Bundle().apply {
            putString(Constants.BOOK_ID, bookId)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DELETING_COMPLETED, data)
        sendDeletingCompletedNotification(bookId)
    }

    private fun onDeletingEndedWithError(bookId: String, deletingFailedCount: Int) {
        val data = Bundle().apply {
            putString(Constants.BOOK_ID, bookId)
            putInt(Constants.DELETING_FAILED_COUNT, deletingFailedCount)
        }
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_DELETING_FAILED, data)
        sendDeletingFailedNotification(bookId)
    }


    private fun sendDeletingProgressNotification(bookId: String, progress: Int, total: Int) {
        val notificationDescription = String.format(bookProgressTemplate, bookId, progress, total)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            deletingProgress,
            NotificationCompat.PRIORITY_DEFAULT,
            notificationDescription,
            true,
            Constants.NOTIFICATION_ID,
            pendingIntent
        )
    }

    private fun sendDeletingCompletedNotification(bookId: String) {
        NotificationHelper.cancelNotification(Constants.NOTIFICATION_ID)
        val successTitle = deletingCompleted
        val successMessage = String.format(deletingCompletedTemplate, bookId)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
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

    private fun sendDeletingFailedNotification(bookId: String) {
        NotificationHelper.cancelNotification(Constants.NOTIFICATION_ID)
        val failureTitle = deletingFailed
        val failureMessage = String.format(deletingFailedTemplate, bookId)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
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
        private const val TAG = "BookDeletingServices"
        private const val BOOK_ID = "bookId"

        private val isDeletingBook = AtomicBoolean(false)

        private const val JOB_ID = 7124

        @JvmStatic
        fun start(fromContext: Context, bookId: String) {
            val intent = Intent(fromContext, BookDeletingService::class.java)
            intent.putExtras(Bundle().apply {
                putString(BOOK_ID, bookId)
            })
            enqueueWork(fromContext, BookDeletingService::class.java, JOB_ID, intent)
        }
    }
}
