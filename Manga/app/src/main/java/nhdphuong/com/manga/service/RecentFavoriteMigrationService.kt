package nhdphuong.com.manga.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.RecentFavoriteMigrationResult.MigratedBook
import nhdphuong.com.manga.data.entity.RecentFavoriteMigrationResult.BookMigrationError
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.usecase.RecentFavoriteMigrationUseCase
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class RecentFavoriteMigrationService : JobIntentService() {
    @Inject
    lateinit var recentFavoriteMigrationUseCase: RecentFavoriteMigrationUseCase

    private val compositeDisposable = CompositeDisposable()

    private var migratingBook = ""
    private var migrationProgressTemplate = ""
    private var migrationFailureTemplate = ""
    private var migrationCompleted = ""
    private var migrationResultTemplate = ""

    override fun onCreate() {
        super.onCreate()
        NHentaiApp.instance.applicationComponent.inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        migratingBook = getString(R.string.migrating_books)
        migrationProgressTemplate = getString(R.string.migration_progress_template)
        migrationFailureTemplate = getString(R.string.migration_failure_template)
        migrationCompleted = getString(R.string.migration_completed)
        migrationResultTemplate = getString(R.string.migration_result)

        var progress = 0
        var failed = 0
        var total = 0
        recentFavoriteMigrationUseCase.execute()
            .doOnSubscribe {
                isBooksMigrating.compareAndSet(false, true)
            }
            .subscribe({
                when (it) {
                    is MigratedBook -> {
                        progress = it.progress
                        total = it.total
                        sendMigrationProgressNotification(progress, total)
                    }
                    is BookMigrationError -> {
                        failed = it.failedItems
                        total = it.total
                        sendMigrationFailureNotification(failed, total)
                    }
                }
                Logger.d(
                    TAG,
                    "Progress: migrated $progress item(s), failed $failed item(s), total: $total"
                )
            }, {
                Logger.d(TAG, "Failed to migrate books with error $it")
                isBooksMigrating.compareAndSet(true, false)
            }, {
                Logger.d(TAG, "Migration completed")
                sendMigrationCompletedNotification(progress, failed, total)
                isBooksMigrating.compareAndSet(true, false)
            }).addTo(compositeDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun sendMigrationProgressNotification(progress: Int, total: Int) {
        NotificationHelper.cancelNotification(MIGRATION_PROGRESS_NOTIFICATION_ID)
        val progressTitle = migratingBook
        val notificationDescription = String.format(migrationProgressTemplate, progress, total)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            progressTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            notificationDescription,
            true,
            MIGRATION_PROGRESS_NOTIFICATION_ID,
            pendingIntent
        )
    }

    private fun sendMigrationFailureNotification(failed: Int, total: Int) {
        NotificationHelper.cancelNotification(MIGRATION_FAILURE_NOTIFICATION_ID)
        val progressTitle = migratingBook
        val notificationDescription = String.format(migrationFailureTemplate, failed, total)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
        )

        NotificationHelper.sendBigContentNotification(
            progressTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            notificationDescription,
            true,
            MIGRATION_FAILURE_NOTIFICATION_ID,
            pendingIntent
        )
    }

    private fun sendMigrationCompletedNotification(migrated: Int, failed: Int, total: Int) {
        NotificationHelper.cancelNotification(MIGRATION_COMPLETED_NOTIFICATION_ID)
        val progressTitle = migrationCompleted
        val notificationDescription =
            String.format(migrationResultTemplate, migrated, total, failed, total)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
        )

        NotificationHelper.cancelNotification(
            MIGRATION_PROGRESS_NOTIFICATION_ID,
            MIGRATION_FAILURE_NOTIFICATION_ID
        )

        NotificationHelper.sendBigContentNotification(
            progressTitle,
            NotificationCompat.PRIORITY_DEFAULT,
            notificationDescription,
            true,
            MIGRATION_COMPLETED_NOTIFICATION_ID,
            pendingIntent
        )
    }

    companion object {
        private const val JOB_ID = 7126

        private val isBooksMigrating = AtomicBoolean(false)
        val isMigrating: Boolean get() = isBooksMigrating.get()

        private const val MIGRATION_PROGRESS_NOTIFICATION_ID = 12345
        private const val MIGRATION_FAILURE_NOTIFICATION_ID = 67890
        private const val MIGRATION_COMPLETED_NOTIFICATION_ID = 123456789

        private const val TAG = "RecentFavoriteMigrationService"

        @JvmStatic
        fun enqueueWork(context: Context) {
            val intent = Intent(context, RecentFavoriteMigrationService::class.java)
            enqueueWork(context, RecentFavoriteMigrationService::class.java, JOB_ID, intent)
        }
    }
}