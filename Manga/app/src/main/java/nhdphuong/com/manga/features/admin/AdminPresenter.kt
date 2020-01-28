package nhdphuong.com.manga.features.admin

import com.google.gson.JsonArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.IFileUtils
import nhdphuong.com.manga.supports.SupportUtils
import java.util.TreeMap
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class AdminPresenter @Inject constructor(
    private val view: AdminContract.View,
    private val bookRepository: BookRepository,
    private val fileUtils: IFileUtils,
    private val sharedPreferencesManager: SharedPreferencesManager,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : AdminContract.Presenter {
    companion object {
        private const val TAG = "AdminPresenter"
    }

    init {
        view.setPresenter(this)
    }

    private val artists = TreeMap<Long, Tag>()
    private val characters = TreeMap<Long, Tag>()
    private val categories = TreeMap<Long, Tag>()
    private val languages = TreeMap<Long, Tag>()
    private val parodies = TreeMap<Long, Tag>()
    private val groups = TreeMap<Long, Tag>()
    private val tags = TreeMap<Long, Tag>()
    private val unknownTypes = TreeMap<Long, Tag>()

    private var currentPage = 1L
    private var numberOfPage = -1L

    override fun start() {
        clearData()
        io.launch {
            numberOfPage = bookRepository.getBookByPage(currentPage)?.numOfPages ?: 0L
            Logger.d(TAG, "Number Of pages=$numberOfPage")
        }
    }

    override fun stop() {

    }

    override fun startDownloading() {
        if (fileUtils.isStoragePermissionAccepted()) {
            downloadPage(currentPage)
        } else {
            view.showRequestStoragePermission()
        }
    }

    override fun toggleCensored(censored: Boolean) {
        sharedPreferencesManager.isCensored = censored
        view.restartApp()
    }

    private fun downloadPage(page: Long) {
        if (numberOfPage < 0) {
            return
        }

        if (page <= numberOfPage) {
            io.launch {
                val remoteBook = bookRepository.getBookByPage(page)
                if (remoteBook != null) {
                    handleResponse(remoteBook)
                } else {
                    handleError()
                }
            }
        } else {
            Logger.d(TAG, "Downloading completed")

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
    }

    private fun handleError() {
        Logger.d(TAG, "Downloading tags failed")
        downloadPage(++currentPage)
    }

    private fun handleResponse(remoteBook: RemoteBook) {
        Logger.d(TAG, "Downloading tags response: $remoteBook")
        if (numberOfPage <= 0) {
            numberOfPage = remoteBook.numOfPages
            main.launch {
                view.showNumberOfPages(numberOfPage)
                sharedPreferencesManager.run {
                    view.updateDownloadingStatistics(
                        currentPage,
                        lastArtistsCount,
                        lastCharactersCount,
                        lastCategoriesCount,
                        lastLanguagesCount,
                        lastParodiesCount,
                        lastGroupsCount,
                        lastTagsCount,
                        lastUnknownTypesCount
                    )
                }
            }
        } else {
            for (book in remoteBook.bookList) {
                for (tag in book.tags) {
                    addTag(tag)
                }
            }
            main.launch {
                view.updateDownloadingStatistics(
                    currentPage,
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
        }
        downloadPage(++currentPage)
    }

    private fun addTag(tag: Tag) {
        when (tag.type.toLowerCase(Locale.US)) {
            Constants.ARTIST -> {
                Logger.d(
                    TAG, "Artist - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                artists[tag.tagId] = tag
            }
            Constants.CHARACTER -> {
                Logger.d(
                    TAG, "Character - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                characters[tag.tagId] = tag
            }
            Constants.CATEGORY -> {
                Logger.d(
                    TAG, "Category - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                categories[tag.tagId] = tag
            }
            Constants.LANGUAGE -> {
                Logger.d(
                    TAG, "Language - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                languages[tag.tagId] = tag
            }
            Constants.PARODY -> {
                Logger.d(
                    TAG, "Parody - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                parodies[tag.tagId] = tag
            }
            Constants.GROUP -> {
                Logger.d(
                    TAG, "Group - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                groups[tag.tagId] = tag
            }
            Constants.TAG -> {
                Logger.d(
                    TAG, "Tag - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                tags[tag.tagId] = tag
            }
            else -> {
                Logger.d(
                    TAG, "Unknown tag - id: ${tag.tagId}," +
                            " name: ${tag.name}, imageType: ${tag.type}, url: ${tag.url}"
                )
                unknownTypes[tag.tagId] = tag
            }
        }
    }

    private fun saveTagsFiles(tagsMap: Map<Long, Tag>, tagName: String) {
        io.launch {
            val jsonArray = JsonArray()
            for (entry in tagsMap.entries) {
                jsonArray.add(entry.value.jsonValue)
            }
            val saveResult = SupportUtils.saveStringFile(
                jsonArray.toString(),
                tagName,
                fileUtils.getTagDirectory()
            )
            Logger.d(TAG, "$tagName list saving result=$saveResult")
        }
    }

    private fun saveChangeSummaryData() {
        io.launch {
            val saveResult = SupportUtils.saveStringFile(
                System.currentTimeMillis().toString(),
                "CurrentVersion",
                fileUtils.getTagDirectory()
            )
            Logger.d(TAG, "Current id saving result=$saveResult")
        }
    }

    private fun saveChangeLogsFile() {
        io.launch {
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
                "Created date: ${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(
                    Calendar.MONTH
                ) + 1}/${calendar.get(Calendar.YEAR)}\n"
            )
            stringBuffer.append("Total: $total items\n")
            stringBuffer.append("Categories: ${categories.size} items, number of old items: ${sharedPreferencesManager.lastCategoriesCount}\n")
            stringBuffer.append("Characters: ${characters.size} items, number of old items: ${sharedPreferencesManager.lastCharactersCount}\n")
            stringBuffer.append("Languages: ${languages.size} items, number of old items: ${sharedPreferencesManager.lastLanguagesCount}\n")
            stringBuffer.append("Parodies: ${parodies.size} items, number of old items: ${sharedPreferencesManager.lastParodiesCount}\n")
            stringBuffer.append("Artists: ${artists.size} items, number of old items: ${sharedPreferencesManager.lastArtistsCount}\n")
            stringBuffer.append("Groups: ${groups.size} items, number of old items: ${sharedPreferencesManager.lastGroupsCount}\n")
            stringBuffer.append("mTags: ${tags.size} items, number of old items: ${sharedPreferencesManager.lastTagsCount}\n")
            stringBuffer.append("UnknownTypes: ${unknownTypes.size} items, number of old items: ${sharedPreferencesManager.lastUnknownTypesCount}\n")
            stringBuffer.append("==================================================================")
            val saveResult = SupportUtils.saveStringFile(
                stringBuffer.toString(),
                "ChangeLogs",
                fileUtils.getTagDirectory()
            )
            Logger.d(TAG, "NHentai.txt list saving result=$saveResult")
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

    private fun clearData() {
        currentPage = 1
        numberOfPage = -1L
        artists.clear()
        characters.clear()
        categories.clear()
        languages.clear()
        parodies.clear()
        groups.clear()
        tags.clear()
        unknownTypes.clear()
    }
}
