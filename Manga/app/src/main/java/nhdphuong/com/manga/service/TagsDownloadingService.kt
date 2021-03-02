package nhdphuong.com.manga.service

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import com.google.gson.JsonArray
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Constants.Companion.ACTION_TAGS_DOWNLOADING_COMPLETED
import nhdphuong.com.manga.Constants.Companion.ACTION_TAGS_DOWNLOADING_FAILED
import nhdphuong.com.manga.Constants.Companion.ACTION_TAGS_DOWNLOADING_PROGRESS
import nhdphuong.com.manga.Constants.Companion.DOWNLOADED_PAGES
import nhdphuong.com.manga.Constants.Companion.TAGS_DOWNLOADING_RESULT
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.supports.IFileUtils
import nhdphuong.com.manga.supports.SupportUtils
import java.util.Calendar
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class TagsDownloadingService : IntentService("TagsDownloadingService") {
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var fileUtils: IFileUtils

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private var downloadingTags = ""
    private var downloadingInProgress = ""
    private var downloadingTagProgressTemplate = ""
    private var downloadingCompleted = ""
    private var downloadingTagsCompleted = ""
    private var downloadingFailed = ""
    private var downloadingTagsFailedTemplate = ""

    override fun onCreate() {
        super.onCreate()
        NHentaiApp.instance.applicationComponent.inject(this)

        downloadingTags = getString(R.string.downloading_tags)
        downloadingInProgress = getString(R.string.downloading_in_progress)
        downloadingTagProgressTemplate = getString(R.string.downloading_tags_progress)
        downloadingCompleted = getString(R.string.downloading_completed)
        downloadingTagsCompleted = getString(R.string.downloading_tags_completed)
        downloadingFailed = getString(R.string.downloading_failure)
        downloadingTagsFailedTemplate = getString(R.string.downloading_tags_failed)

        val notificationIntent = Intent(this, NavigationRedirectActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, Intent.FILL_IN_ACTION
        )

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(downloadingTags)
            .setContentIntent(pendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Constants.NOTIFICATION_ID, notification)
        } else {
            NotificationHelper.sendNotification(notification, Constants.NOTIFICATION_ID)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.getLongExtra(NUMBER_OF_PAGES, 0)?.takeIf { it > 0 }?.let { numberOfPages ->
            val pages = arrayListOf<Long>()
            (1..numberOfPages).toCollection(pages)

            val tagResult = TagResult(numberOfPages, fileUtils, sharedPreferencesManager)
            val currentPageTracker = AtomicInteger(0)
            Observable.fromIterable(pages.sorted())
                .takeWhile {
                    !isForcedStopping.get()
                }
                .doOnSubscribe {
                    isRunning.compareAndSet(false, true)
                }
                .flatMap { page ->
                    currentPageTracker.incrementAndGet()
                    getBookByPage(page).map { Pair(page, it) }
                }
                .doOnNext { (_, response) ->
                    when (response) {
                        is RemoteBookResponse.Success -> {
                            response.remoteBook.bookList.map { book -> book.tags }
                                .flatten()
                                .let(tagResult::importTags)
                        }

                        is RemoteBookResponse.Failure -> {
                            logger.e("Failed to download with error ${response.error}")
                            postDownloadingErrorMessages(currentPageTracker.get())
                        }
                    }
                }
                .subscribe({ (currentPage, _) ->
                    logger.d("Downloaded page $currentPage")
                    postProgressMessages(currentPage.toInt(), tagResult.exportCurrentStatus())
                }, {
                    logger.e("Failed to download book list with error $it")
                    postDownloadingErrorMessages(currentPageTracker.get())
                }, {
                    logger.d("Tag data is already downloaded")
                    postDownloadingCompletedMessages()
                    tagResult.exportTags()
                    isRunning.compareAndSet(true, false)
                    isForcedStopping.compareAndSet(true, false)
                })
                .let(compositeDisposable::add)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun postProgressMessages(currentPage: Int, tagDownloadingResult: TagDownloadingResult) {
        NotificationHelper.cancelNotification(Constants.NOTIFICATION_ID)
        val progressTitle = downloadingInProgress
        val notificationDescription = String.format(
            downloadingTagProgressTemplate,
            currentPage,
            tagDownloadingResult.numberOfPages,
            tagDownloadingResult.artists,
            tagDownloadingResult.characters,
            tagDownloadingResult.categories,
            tagDownloadingResult.languages,
            tagDownloadingResult.parodies,
            tagDownloadingResult.groups,
            tagDownloadingResult.tags,
            tagDownloadingResult.unknownTypes
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

        Bundle().apply {
            putParcelable(TAGS_DOWNLOADING_RESULT, tagDownloadingResult)
            putInt(DOWNLOADED_PAGES, currentPage)
        }.let {
            BroadCastReceiverHelper.sendBroadCast(this, ACTION_TAGS_DOWNLOADING_PROGRESS, it)
        }
    }

    private fun postDownloadingErrorMessages(currentPage: Int) {
        NotificationHelper.cancelNotification(Constants.NOTIFICATION_ID)
        val progressTitle = downloadingFailed
        val notificationDescription = String.format(downloadingTagsFailedTemplate, currentPage)
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
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_TAGS_DOWNLOADING_FAILED)
    }

    private fun postDownloadingCompletedMessages() {
        NotificationHelper.cancelNotification(Constants.NOTIFICATION_ID)
        val progressTitle = downloadingCompleted
        val notificationDescription = downloadingTagsCompleted
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
        BroadCastReceiverHelper.sendBroadCast(this, ACTION_TAGS_DOWNLOADING_COMPLETED)
    }

    private fun getBookByPage(page: Long): Observable<RemoteBookResponse> {
        return Observable.create {
            runBlocking {
                try {
                    val response = bookRepository.getBookByPage(page, SortOption.Recent)
                    it.onNext(response)
                } catch (throwable: Throwable) {
                    it.onError(throwable)
                } finally {
                    it.onComplete()
                }
            }
        }
    }

    companion object {
        private val logger = Logger("TagsDownloadingService")
        private const val NUMBER_OF_PAGES = "numberOfPages"

        private val isForcedStopping = AtomicBoolean(false)

        private val isRunning = AtomicBoolean(false)
        val isTagBeingDownloaded: Boolean get() = isRunning.get()

        fun start(fromContext: Context, numberOfPage: Long) {
            Intent(fromContext, TagsDownloadingService::class.java).apply {
                putExtra(NUMBER_OF_PAGES, numberOfPage)
            }.let(fromContext::startService)
        }

        fun stopCurrentTask() {
            isForcedStopping.compareAndSet(false, true)
        }

        @Parcelize
        data class TagDownloadingResult(
            val numberOfPages: Long,
            val artists: Int = 0,
            val characters: Int = 0,
            val categories: Int = 0,
            val languages: Int = 0,
            val parodies: Int = 0,
            val groups: Int = 0,
            val tags: Int = 0,
            val unknownTypes: Int = 0
        ) : Parcelable

        private class TagResult(
            private val numberOfPages: Long,
            private val fileUtils: IFileUtils,
            private val sharedPreferencesManager: SharedPreferencesManager
        ) {
            private val artists = TreeMap<Long, Tag>()
            private val characters = TreeMap<Long, Tag>()
            private val categories = TreeMap<Long, Tag>()
            private val languages = TreeMap<Long, Tag>()
            private val parodies = TreeMap<Long, Tag>()
            private val groups = TreeMap<Long, Tag>()
            private val tags = TreeMap<Long, Tag>()
            private val unknownTypes = TreeMap<Long, Tag>()

            fun importTags(tags: List<Tag>) {
                tags.forEach(this::addTag)
            }

            fun exportTags() {
                saveTagsFiles(artists, Constants.ARTIST)
                saveTagsFiles(characters, Constants.CHARACTER)
                saveTagsFiles(categories, Constants.CATEGORY)
                saveTagsFiles(languages, Constants.LANGUAGE)
                saveTagsFiles(parodies, Constants.PARODY)
                saveTagsFiles(groups, Constants.GROUP)
                saveTagsFiles(tags, Constants.TAG)
                saveTagsFiles(unknownTypes, "unknown")
                saveChangeSummaryData()
                saveChangeLogsFile()
            }

            fun exportCurrentStatus(): TagDownloadingResult {
                return TagDownloadingResult(
                    numberOfPages,
                    artists.size,
                    characters.size,
                    categories.size,
                    languages.size,
                    parodies.size,
                    groups.size,
                    tags.size,
                    unknownTypes.size
                )
            }

            private fun addTag(tag: Tag) {
                when (tag.type.toLowerCase(Locale.US)) {
                    Constants.ARTIST -> {
                        logger.d("Artist - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        artists[tag.id] = tag
                    }
                    Constants.CHARACTER -> {
                        logger.d("Character - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        characters[tag.id] = tag
                    }
                    Constants.CATEGORY -> {
                        logger.d("Category - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        categories[tag.id] = tag
                    }
                    Constants.LANGUAGE -> {
                        logger.d("Language - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        languages[tag.id] = tag
                    }
                    Constants.PARODY -> {
                        logger.d("Parody - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        parodies[tag.id] = tag
                    }
                    Constants.GROUP -> {
                        logger.d("Group - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        groups[tag.id] = tag
                    }
                    Constants.TAG -> {
                        logger.d("Tag - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        tags[tag.id] = tag
                    }
                    else -> {
                        logger.d("Unknown tag - id: ${tag.id}, name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}")
                        unknownTypes[tag.id] = tag
                    }
                }
            }

            private fun saveTagsFiles(tagsMap: Map<Long, Tag>, tagName: String) {
                val jsonArray = JsonArray()
                for (entry in tagsMap.entries) {
                    jsonArray.add(entry.value.jsonValue)
                }
                val saveResult = SupportUtils.saveStringFile(
                    jsonArray.toString(),
                    tagName,
                    fileUtils.getTagDirectory()
                )
                logger.d("$tagName list saving result=$saveResult")
            }

            private fun saveChangeSummaryData() {
                val saveResult = SupportUtils.saveStringFile(
                    System.currentTimeMillis().toString(),
                    "CurrentVersion",
                    fileUtils.getTagDirectory()
                )
                logger.d("Current id saving result=$saveResult")
            }

            private fun saveChangeLogsFile() {
                val stringBuffer = StringBuffer("")
                val calendar = Calendar.getInstance(Locale.US)
                val total = artists.size +
                        categories.size +
                        characters.size +
                        languages.size +
                        parodies.size +
                        groups.size +
                        tags.size +
                        unknownTypes.size

                stringBuffer.append("===========================NHentai Data===========================\n")
                stringBuffer.append(
                    "Created date: ${calendar.get(Calendar.DAY_OF_MONTH)}/${
                        calendar.get(
                            Calendar.MONTH
                        ) + 1
                    }/${calendar.get(Calendar.YEAR)}\n"
                )
                stringBuffer.append("Total: $total items\n")
                stringBuffer.append("Categories: ${categories.size} items, number of old items: ${sharedPreferencesManager.lastCategoriesCount}\n")
                stringBuffer.append("Characters: ${characters.size} items, number of old items: ${sharedPreferencesManager.lastCharactersCount}\n")
                stringBuffer.append("Languages: ${languages.size} items, number of old items: ${sharedPreferencesManager.lastLanguagesCount}\n")
                stringBuffer.append("Parodies: ${parodies.size} items, number of old items: ${sharedPreferencesManager.lastParodiesCount}\n")
                stringBuffer.append("Artists: ${artists.size} items, number of old items: ${sharedPreferencesManager.lastArtistsCount}\n")
                stringBuffer.append("Groups: ${groups.size} items, number of old items: ${sharedPreferencesManager.lastGroupsCount}\n")
                stringBuffer.append("Tags: ${tags.size} items, number of old items: ${sharedPreferencesManager.lastTagsCount}\n")
                stringBuffer.append("UnknownTypes: ${unknownTypes.size} items, number of old items: ${sharedPreferencesManager.lastUnknownTypesCount}\n")
                stringBuffer.append("==================================================================")
                val saveResult = SupportUtils.saveStringFile(
                    stringBuffer.toString(),
                    "ChangeLogs",
                    fileUtils.getTagDirectory()
                )
                logger.d("NHentai.txt list saving result=$saveResult")
                sharedPreferencesManager.lastCategoriesCount = categories.size
                sharedPreferencesManager.lastCharactersCount = characters.size
                sharedPreferencesManager.lastLanguagesCount = languages.size
                sharedPreferencesManager.lastParodiesCount = parodies.size
                sharedPreferencesManager.lastArtistsCount = artists.size
                sharedPreferencesManager.lastGroupsCount = groups.size
                sharedPreferencesManager.lastTagsCount = tags.size
                sharedPreferencesManager.lastUnknownTypesCount = unknownTypes.size
            }
        }
    }
}