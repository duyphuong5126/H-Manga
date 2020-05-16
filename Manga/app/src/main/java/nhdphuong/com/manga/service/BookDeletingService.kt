package nhdphuong.com.manga.service

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import nhdphuong.com.manga.*
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

class BookDeletingService : IntentService("BookDeletingService") {
    @Inject
    lateinit var bookDeleteBookUseCase: DeleteBookUseCase

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        NHentaiApp.instance.applicationComponent.inject(this)
        super.onCreate()
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, Intent.FILL_IN_ACTION
        )

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(getString(R.string.deleting_book))
            .setContentIntent(pendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Constants.NOTIFICATION_ID, notification)
        } else {
            NotificationHelper.sendNotification(notification, Constants.NOTIFICATION_ID)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.extras?.getString(BOOK_ID)?.takeIf { it.isNotBlank() }?.let { bookId ->
            while (isDeletingBook.get()) {
                Logger.d(TAG, "Another book is being deleted")
            }
            if (isDeletingBook.compareAndSet(false, true)) {
                val deletingFailedCount = AtomicInteger(0)
                bookDeleteBookUseCase.execute(bookId)
                    .doOnSubscribe {
                        onDeletingStarted(bookId)
                    }
                    .subscribe({ deletingResult ->
                        when (deletingResult) {
                            is DeletingProgress -> {
                                updateProgress(
                                    bookId, deletingResult.progress, deletingResult.total
                                )
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
        val progressTitle = getString(R.string.deleting_in_progress)
        val notificationDescription = getString(
            R.string.book_progress_template, bookId, progress, total
        )
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            progressTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            notificationDescription,
            true,
            Constants.NOTIFICATION_ID,
            pendingIntent
        )
    }

    private fun sendDeletingCompletedNotification(bookId: String) {
        NotificationHelper.cancelNotification(Constants.NOTIFICATION_ID)
        val successTitle = getString(R.string.deleting_completed)
        val successMessage = getString(R.string.deleting_completed_template, bookId)
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
        val failureTitle = getString(R.string.deleting_failure)
        val failureMessage = getString(R.string.deleting_failed_template, bookId)
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

        @JvmStatic
        fun start(fromContext: Context, bookId: String) {
            val intent = Intent(fromContext, BookDeletingService::class.java)
            intent.putExtras(Bundle().apply {
                putString(BOOK_ID, bookId)
            })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fromContext.startForegroundService(intent)
            } else {
                fromContext.startService(intent)
            }
        }
    }
}
