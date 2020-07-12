package nhdphuong.com.manga.features.home

import android.annotation.SuppressLint
import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.Constants.Companion.MAX_PER_PAGE
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.SupportUtils
import java.util.Locale
import java.util.Random
import java.util.Collections
import java.util.Stack
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

/*
 * Created by nhdphuong on 3/18/18.
 */

class HomePresenter @Inject constructor(
    private val view: HomeContract.View,
    private val bookRepository: BookRepository,
    private val masterDataRepository: MasterDataRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : HomeContract.Presenter {
    companion object {
        private const val TAG = "HomePresenter"
        private const val NUMBER_OF_PREVENTIVE_PAGES = 10
        private const val BOOKS_PER_PAGE = MAX_PER_PAGE
    }

    private var mainList = ArrayList<Book>()
    private var currentNumOfPages = 0L
    private var currentLimitPerPage = 0
    private var currentPage = 1L

    @SuppressLint("UseSparseArrays")
    private var preventiveData = HashMap<Long, ArrayList<Book>>()

    private var isLoadingPreventiveData = false
    private val jobStack = Stack<Job>()
    private var isRefreshing = AtomicBoolean(false)

    private var searchData: String = ""
    private val isSearching: Boolean get() = !TextUtils.isEmpty(searchData)

    private val newerVersionAcknowledged = AtomicBoolean(false)

    private val tagsDownloadManager = DownloadManager.Companion.TagsDownloadManager

    private var sortOption = SortOption.Recent
        set(value) {
            val lastValue = field
            field = value
            if (lastValue != value) {
                reload()
            }
        }

    init {
        view.setPresenter(this)
    }

    override fun start() {
        Logger.d(TAG, "start")
        reload()
        if (!sharedPreferencesManager.tagsDataDownloaded) {
            tagsDownloadManager.startDownloading()
            masterDataRepository.fetchAllTagLists { isSuccess ->
                Logger.d(TAG, "Tags fetching completed, isSuccess=$isSuccess")
                if (isSuccess) {
                    sharedPreferencesManager.tagsDataDownloaded = true
                }
                tagsDownloadManager.stopDownloading()

                val onVersionFetched = {
                    main.launch {
                        view.startUpdateTagsService()
                    }
                }
                masterDataRepository.getTagDataVersion(onSuccess = { newVersion ->
                    if (sharedPreferencesManager.currentTagVersion != newVersion) {
                        Logger.d(
                            TAG, "New version is available, " +
                                    "new version: $newVersion," +
                                    " current version: ${sharedPreferencesManager.currentTagVersion}"
                        )
                        sharedPreferencesManager.currentTagVersion = newVersion
                    } else {
                        Logger.d(TAG, "App is already updated to the version $newVersion")
                    }
                    onVersionFetched()
                }, onError = {
                    Logger.d(TAG, "Version fetching failed")
                    onVersionFetched()
                })
            }
        } else {
            view.startUpdateTagsService()
        }
    }

    override fun jumpToPage(pageNumber: Long) {
        Logger.d(TAG, "Navigate to page $pageNumber")
        onPageChange(pageNumber)
    }

    override fun jumToFirstPage() {
        Logger.d(TAG, "Navigate to first page")
        onPageChange(1)
    }

    override fun jumToLastPage() {
        Logger.d(TAG, "Navigate to last page: $currentNumOfPages")
        onPageChange(currentNumOfPages)
    }

    override fun setNewerVersionAcknowledged() {
        newerVersionAcknowledged.compareAndSet(false, true)
    }

    override fun refreshAppVersion() {
        io.launch {
            val upgradeNotificationAllowed = sharedPreferencesManager.isUpgradeNotificationAllowed
            masterDataRepository.getAppVersion(onSuccess = { latestVersion ->
                Logger.d(TAG, "Latest version: $latestVersion")
                val isLatestVersion = BuildConfig.VERSION_CODE == latestVersion
                val versionAcknowledged = newerVersionAcknowledged.get()
                if (!isLatestVersion && !versionAcknowledged && upgradeNotificationAllowed) {
                    main.launch {
                        if (view.isActive()) {
                            view.showUpgradeNotification()
                        }
                    }
                }
            }, onError = { error ->
                Logger.e(TAG, "Failed to get app version with error: $error")
            })
        }
    }

    override fun reloadCurrentPage(onRefreshed: () -> Unit) {
        if (isRefreshing.compareAndSet(false, true)) {
            io.launch {
                val remoteBooks = getBooksListByPage(currentPage)
                currentNumOfPages = remoteBooks?.numOfPages ?: 0L
                val isCurrentPageEmpty = if (remoteBooks != null) {
                    remoteBooks.bookList.let { bookList ->
                        preventiveData[currentPage]?.let { page ->
                            page.clear()
                            page.addAll(bookList)
                        }
                        mainList.clear()
                        mainList.addAll(bookList)
                    }
                    false
                } else {
                    true
                }
                isRefreshing.compareAndSet(true, false)

                main.launch {
                    if (view.isActive()) {
                        if (!isCurrentPageEmpty) {
                            view.refreshHomePagination(currentNumOfPages)
                            view.refreshHomeBookList()
                        }
                        onRefreshed()
                        view.showNothingView(isCurrentPageEmpty)
                        if (isCurrentPageEmpty || searchData.isBlank()) {
                            view.hideSortOptionList()
                        } else {
                            view.enableSortOption(sortOption)
                            view.showSortOptionList()
                        }
                    }
                }
            }
        } else {
            view.showRefreshingDialog()
        }
    }

    override fun reloadLastBookListRefreshTime() {
        sharedPreferencesManager.getLastBookListRefreshTime().let { lastRefreshTime ->
            view.showLastBookListRefreshTime(
                SupportUtils.getTimeElapsed(
                    System.currentTimeMillis() - lastRefreshTime
                ).toLowerCase(Locale.US)
            )
        }
    }

    override fun reloadRecentBooks() {
        io.launch {
            val recentList = ArrayList<String>()
            val favoriteList = ArrayList<String>()
            mainList.forEach {
                val bookId = it.bookId
                when {
                    bookRepository.isFavoriteBook(bookId) -> favoriteList.add(bookId)
                    bookRepository.isRecentBook(bookId) -> recentList.add(bookId)
                    else -> {
                    }
                }
            }

            main.launch {
                view.showRecentBooks(recentList)
                view.showFavoriteBooks(favoriteList)
            }
        }
    }

    override fun saveLastBookListRefreshTime() {
        sharedPreferencesManager.setLastBookListRefreshTime(System.currentTimeMillis())
    }

    override fun updateSearchData(data: String) {
        if (!searchData.equals(data, ignoreCase = true)) {
            searchData = data
            view.changeSearchResult(data)
            reload()
        } else {
            Logger.d(TAG, "Search data is not changed")
        }
    }

    override fun pickBookRandomly() {
        view.showLoading()
        io.launch {
            val random = Random()
            var bound = currentNumOfPages.toInt()
            if (bound <= 0) {
                bound = 1
            }
            val randomPage = random.nextInt(bound) + 1
            getBooksListByPage(randomPage.toLong())?.bookList.let { randomBooks ->
                Logger.d(
                    TAG, "Randomized paged $randomPage," +
                            " books count=${randomBooks?.size ?: 0}"
                )
                if (randomBooks?.isEmpty() == false) {
                    val randomIndex = random.nextInt(randomBooks.size)
                    main.launch {
                        view.hideLoading()
                        view.showBookPreview(randomBooks[randomIndex])
                    }
                }
            }
        }
    }

    override fun updateSortOption(sortOption: SortOption) {
        Logger.d(TAG, "sortOption $sortOption")
        this.sortOption = sortOption
        if (view.isActive()) {
            view.enableSortOption(sortOption)
        }
    }

    override fun stop() {
        Logger.d(TAG, "stop")
        io.launch {
            while (jobStack.size > 0) {
                val job = jobStack.pop()
                job.cancel()
            }
        }
        clearData()
    }

    private fun reload() {
        clearData()

        view.showLoading()
        view.setUpHomeBookList(mainList)
        val downloadingJob = io.launch {
            val startTime = System.currentTimeMillis()
            getBooksListByPage(currentPage).let { remoteBook ->
                Logger.d(TAG, "Time spent=${System.currentTimeMillis() - startTime}")
                currentNumOfPages = remoteBook?.numOfPages ?: 0L
                currentLimitPerPage = remoteBook?.numOfBooksPerPage ?: 0
                Logger.d(TAG, "Remote books: $currentNumOfPages")
                val bookList = remoteBook?.bookList ?: ArrayList()
                mainList.addAll(bookList)
                preventiveData[currentPage] = bookList
                for (book in bookList) {
                    Logger.d(TAG, book.logString)
                }

                loadPreventiveData()

                main.launch {
                    if (view.isActive()) {
                        view.refreshHomeBookList()
                        if (currentNumOfPages > 0) {
                            view.refreshHomePagination(currentNumOfPages)
                            view.showNothingView(false)
                            view.enableSortOption(sortOption)
                            if (searchData.isNotBlank()) {
                                view.showSortOptionList()
                            }
                        } else {
                            view.showNothingView(true)
                            view.hideSortOptionList()
                        }
                        view.hideLoading()
                    }
                }
            }
        }
        jobStack.push(downloadingJob)
    }

    private fun onPageChange(pageNumber: Long) {
        currentPage = pageNumber
        io.launch {
            mainList.clear()
            var newPage = false
            val currentList: ArrayList<Book> = if (preventiveData.containsKey(currentPage)) {
                preventiveData[currentPage] as ArrayList<Book>
            } else {
                newPage = true
                main.launch {
                    view.showLoading()
                }
                val bookList = getBooksListByPage(currentPage)?.bookList ?: ArrayList()
                preventiveData[currentPage] = bookList
                bookList
            }
            mainList.addAll(currentList)

            val toLoadList: List<Long> = when (currentPage) {
                1L -> listOf(2L)
                currentNumOfPages -> listOf(currentNumOfPages - 1)
                else -> listOf(currentPage - 1, currentPage + 1)
            }
            logListLong("To load list: ", toLoadList)

            for (page in toLoadList.iterator()) {
                if (!preventiveData.containsKey(page)) {
                    preventiveData[page] = ArrayList()
                    launch {
                        getBooksListByPage(page)?.bookList?.let { bookList ->
                            preventiveData[page]?.addAll(bookList)
                        }
                    }
                    Logger.d(TAG, "Page $page loaded")
                }
            }

            if (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                val pageList = sortListPage(currentPage, ArrayList(preventiveData.keys))
                var pageId = 0
                logListLong("Before deleted page list: ", pageList)
                while (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    Logger.d(TAG, "Remove page: $pageId")
                    val page = pageList[pageId++]
                    (preventiveData[page] as ArrayList).clear()
                    preventiveData.remove(page)
                }
            }
            logListLong("Final page list: ", ArrayList(preventiveData.keys))

            main.launch {
                if (view.isActive()) {
                    view.refreshHomeBookList()
                    if (newPage) {
                        view.hideLoading()
                    }
                }
            }
        }
    }

    private fun sortListPage(anchor: Long, pageList: ArrayList<Long>): ArrayList<Long> {
        if (pageList.isEmpty()) {
            return pageList
        }
        val size = pageList.size
        for (i in 0 until size - 1) {
            for (j in i + 1 until size) {
                if (abs(pageList[i] - anchor) < abs(pageList[j] - anchor)) {
                    Collections.swap(pageList, i, j)
                }
            }
        }
        return pageList
    }

    private fun logListLong(messageString: String, listInt: List<Long>) {
        var message = "$messageString["
        for (i in 0 until listInt.size - 1) {
            message += "${listInt[i]}, "
        }
        message += "${listInt[listInt.size - 1]}]"
        Logger.d(TAG, message)
    }

    private suspend fun loadPreventiveData() {
        isLoadingPreventiveData = true

        suspendCoroutine<Boolean> { continuation ->
            NUMBER_OF_PREVENTIVE_PAGES.toLong().let {
                for (page in currentPage + 1L..NUMBER_OF_PREVENTIVE_PAGES.toLong()) {
                    Logger.d(TAG, "Start loading page $page")
                    io.launch {
                        val remoteBook = getBooksListByPage(page)
                        Logger.d(TAG, "Done loading page $page")
                        remoteBook?.bookList?.let { bookList ->
                            if (bookList.isNotEmpty()) {
                                preventiveData[page] = bookList
                            }
                        }
                        if (page == NUMBER_OF_PREVENTIVE_PAGES.toLong()) {
                            continuation.resume(true)
                        }
                    }
                }
            }
        }

        Logger.d(TAG, "Load preventive data successfully")
        isLoadingPreventiveData = false
    }

    private fun clearData() {
        mainList.clear()
        for (entry in preventiveData.entries) {
            entry.value.clear()
        }
        preventiveData.clear()
        currentNumOfPages = 0L
        currentLimitPerPage = 0
        currentPage = 1
        isLoadingPreventiveData = false
        isRefreshing.compareAndSet(true, false)
    }

    private suspend fun getBooksListByPage(pageNumber: Long): RemoteBook? {
        val remoteBookResponse = if (isSearching) {
            bookRepository.getBookByPage(searchData, pageNumber, sortOption)
        } else {
            bookRepository.getBookByPage(pageNumber, sortOption)
        }
        return when {
            remoteBookResponse is RemoteBookResponse.Success -> remoteBookResponse.remoteBook
            searchData.toLongOrNull() != null -> {
                val bookList = ArrayList<Book>()
                bookRepository.getBookDetails(searchData)?.let(bookList::add)
                RemoteBook(bookList, 1, BOOKS_PER_PAGE)
            }
            else -> null
        }
    }
}
