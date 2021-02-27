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
import javax.inject.Inject

class RecentFavoriteMigrationService : JobIntentService() {
    @Inject
    lateinit var recentFavoriteMigrationUseCase: RecentFavoriteMigrationUseCase

    private val compositeDisposable = CompositeDisposable()

    private var migratingBook = ""
    private var migrationProgressTemplate = ""
    private var migrationProgressWithFailureTemplate = ""
    private var migrationFailureTemplate = ""
    private var migrationCompleted = ""
    private var migrationResultTemplate = ""

    private val logger: Logger by lazy {
        Logger("RecentFavoriteMigrationService")
    }

    override fun onCreate() {
        super.onCreate()
        NHentaiApp.instance.applicationComponent.inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        migratingBook = getString(R.string.migrating_books)
        migrationProgressTemplate = getString(R.string.migration_progress_template)
        migrationProgressWithFailureTemplate =
            getString(R.string.migration_progress_with_failure_template)
        migrationFailureTemplate = getString(R.string.migration_failure_template)
        migrationCompleted = getString(R.string.migration_completed)
        migrationResultTemplate = getString(R.string.migration_result)

        var progress = 0
        var failed = 0
        var total = 0
        recentFavoriteMigrationUseCase.execute()
            .subscribe({
                when (it) {
                    is MigratedBook -> {
                        progress = it.progress
                        total = it.total
                        sendMigrationProgressNotification(progress, failed, total)
                    }
                    is BookMigrationError -> {
                        failed = it.failedItems
                        total = it.total
                        sendMigrationProgressNotification(progress, failed, total)
                    }
                }
                logger.d("Progress: migrated $progress item(s), failed $failed item(s), total: $total")
            }, {
                logger.e("Failed to migrate books with error $it")
            }, {
                logger.d("Migration completed")
                sendMigrationCompletedNotification(progress, failed, total)
            }).addTo(compositeDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun sendMigrationProgressNotification(progress: Int, failure: Int, total: Int) {
        NotificationHelper.cancelNotification(MIGRATION_COMPLETED_NOTIFICATION_ID)
        val progressTitle = migratingBook
        val notificationDescription = if (failure == 0) {
            String.format(migrationProgressTemplate, progress, total)
        } else {
            String.format(migrationProgressWithFailureTemplate, progress, failure, total)
        }
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

    private fun sendMigrationCompletedNotification(migrated: Int, failed: Int, total: Int) {
        NotificationHelper.cancelNotification(MIGRATION_PROGRESS_NOTIFICATION_ID)
        val progressTitle = migrationCompleted
        val notificationDescription =
            String.format(migrationResultTemplate, migrated, total, failed, total)
        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, Intent.FILL_IN_ACTION
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

        private const val MIGRATION_PROGRESS_NOTIFICATION_ID = 12345
        private const val MIGRATION_COMPLETED_NOTIFICATION_ID = 67890

        @JvmStatic
        fun enqueueWork(context: Context) {
            val intent = Intent(context, RecentFavoriteMigrationService::class.java)
            enqueueWork(context, RecentFavoriteMigrationService::class.java, JOB_ID, intent)
        }
    }
}