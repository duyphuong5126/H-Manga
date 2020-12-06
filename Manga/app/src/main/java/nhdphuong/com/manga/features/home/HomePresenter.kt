package nhdphuong.com.manga.features.home

import android.annotation.SuppressLint
import android.text.TextUtils
import com.google.gson.JsonParseException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.Constants.Companion.EVENT_SEARCH
import nhdphuong.com.manga.Constants.Companion.MAX_PER_PAGE
import nhdphuong.com.manga.Constants.Companion.SEARCH_DATA
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.analytics.AnalyticsParam
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.enum.ErrorEnum
import nhdphuong.com.manga.extension.isNetworkError
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.INetworkUtils
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.usecase.LogAnalyticsEventUseCase
import java.net.SocketTimeoutException
import java.util.Locale
import java.util.Random
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
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
    private val logAnalyticsEventUseCase: LogAnalyticsEventUseCase,
    private val networkUtils: INetworkUtils,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : HomeContract.Presenter {
    companion object {
        private const val TAG = "HomePresenter"
        private const val NUMBER_OF_PREVENTIVE_PAGES = 10
        private const val MAX_TRYING_PAGES = 10
        private const val BOOKS_PER_PAGE = MAX_PER_PAGE
        private const val TIMES_OPEN_APP_NEED_ALTERNATIVE_DOMAINS_MESSAGE = 20
    }

    private var mainList = CopyOnWriteArrayList<Book>()
    private var currentNumOfPages = 0L
    private var currentLimitPerPage = 0
    private var currentPage = 1L

    @SuppressLint("UseSparseArrays")
    private var preventiveData = HashMap<Long, ArrayList<Book>>()

    private var isLoadingPreventiveData = false
    private val jobList = CopyOnWriteArrayList<Job>()
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
                reload(false)
            }
        }

    private val remoteBookErrorSubject = PublishSubject.create<Throwable>()

    private val compositeDisposable = CompositeDisposable()

    init {
        view.setPresenter(this)
        remoteBookErrorSubject
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val errorEnum = when {
                    it.isNetworkError() -> ErrorEnum.NetworkError
                    it is SocketTimeoutException -> ErrorEnum.TimeOutError
                    it is JsonParseException -> ErrorEnum.DataParsingError
                    else -> ErrorEnum.UnknownError
                }
                if (view.isActive()) {
                    view.updateErrorMessage(errorEnum)
                }
            }.addTo(compositeDisposable)

    }

    override fun start() {
        Logger.d(TAG, "start")
        sharedPreferencesManager.timesOpenApp++
        reload(true)
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
                val isLatestVersion = BuildConfig.VERSION_CODE == latestVersion.versionNumber
                val versionAcknowledged = newerVersionAcknowledged.get()
                if (!isLatestVersion && !versionAcknowledged && upgradeNotificationAllowed) {
                    main.launch {
                        if (view.isActive()) {
                            view.showUpgradeNotification(latestVersion.versionCode)
                        }
                    }
                }
            }, onError = { error ->
                Logger.e(TAG, "Failed to get app version with error: $error")
            })
        }
    }

    override fun reloadCurrentPage() {
        if (isRefreshing.compareAndSet(false, true)) {
            io.launch {
                val remoteBooks = getBooksListByPage(currentPage, true)
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
                            view.refreshHomePagination(currentNumOfPages, currentPage.toInt() - 1)
                            view.refreshHomeBookList()
                        }
                        view.finishRefreshing()
                        if (isCurrentPageEmpty) {
                            view.showNothingView()
                        } else {
                            view.hideNothingView()
                        }
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
            logAnalyticsEventUseCase.execute(EVENT_SEARCH, AnalyticsParam(SEARCH_DATA, data))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
                .addTo(compositeDisposable)
            reload(false)
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
            getBooksListByPage(randomPage.toLong(), false)?.bookList.let { randomBooks ->
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

    override fun checkedOutAlternativeDomains() {
        sharedPreferencesManager.checkedOutAlternativeDomains = true
    }

    override fun stop() {
        Logger.d(TAG, "stop")
        io.launch {
            while (jobList.size > 0) {
                val job = jobList.removeAt(0)
                job.cancel()
            }
        }
        clearData()
        compositeDisposable.clear()
    }

    private fun reload(needToAskAlternativeDomains: Boolean) {
        clearData()

        view.showLoading()
        view.setUpHomeBookList(mainList)
        val downloadingJob = io.launch {
            val startTime = System.currentTimeMillis()
            var remoteBook = getBooksListByPage(currentPage, true)
            var triedPages = 1
            while (remoteBook == null && triedPages < MAX_TRYING_PAGES && networkUtils.isNetworkConnected()) {
                Logger.d(TAG, "Page $currentPage is empty, trying page ${currentPage + 1}")
                currentPage++
                triedPages++
                remoteBook = getBooksListByPage(currentPage, true)
            }
            Logger.d(TAG, "Time spent=${System.currentTimeMillis() - startTime}")
            currentNumOfPages = remoteBook?.numOfPages ?: 0
            currentLimitPerPage = remoteBook?.numOfBooksPerPage ?: 0
            Logger.d(TAG, "Remote books: $currentNumOfPages")
            val bookList = remoteBook?.bookList ?: ArrayList()
            mainList.addAll(bookList)
            preventiveData[currentPage] = bookList
            for (book in bookList) {
                Logger.d(TAG, book.logString)
            }

            if (networkUtils.isNetworkConnected()) {
                loadPreventiveData()
            }

            val timesOpenApp = sharedPreferencesManager.timesOpenApp
            val timesOpenAppMatched =
                (timesOpenApp % TIMES_OPEN_APP_NEED_ALTERNATIVE_DOMAINS_MESSAGE == 0 || timesOpenApp == 1)
            val checkedOutAlternativeDomains = sharedPreferencesManager.checkedOutAlternativeDomains
            val alternativeDomainInUsed = sharedPreferencesManager.useAlternativeDomain
            val showAlternativeDomainsQuestion = needToAskAlternativeDomains && timesOpenAppMatched
                    && !checkedOutAlternativeDomains && !alternativeDomainInUsed
            main.launch {
                if (view.isActive()) {
                    view.refreshHomeBookList()
                    if (currentNumOfPages > 0) {
                        view.refreshHomePagination(currentNumOfPages, currentPage.toInt() - 1)
                        view.hideNothingView()
                        view.enableSortOption(sortOption)
                        if (searchData.isNotBlank()) {
                            view.showSortOptionList()
                        }
                    } else {
                        view.showNothingView()
                        view.hideSortOptionList()
                    }
                    view.hideLoading()
                    if (showAlternativeDomainsQuestion) {
                        view.showAlternativeDomainsQuestion()
                    }
                }
            }
        }
        jobList.add(downloadingJob)
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
                val bookList = getBooksListByPage(currentPage, false)?.bookList ?: ArrayList()
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
                        getBooksListByPage(page, false)?.bookList?.let { bookList ->
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
                val startPage = currentPage + 1L
                val endPage = startPage + NUMBER_OF_PREVENTIVE_PAGES.toLong()
                for (page in startPage..endPage) {
                    Logger.d(TAG, "Start loading page $page")
                    io.launch {
                        val remoteBook = getBooksListByPage(page, false)
                        Logger.d(TAG, "Done loading page $page")
                        remoteBook?.bookList?.let { bookList ->
                            if (bookList.isNotEmpty()) {
                                preventiveData[page] = bookList
                            }
                        }
                        if (page == endPage) {
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

    private suspend fun getBooksListByPage(
        pageNumber: Long,
        needToUpdateErrorMessage: Boolean
    ): RemoteBook? {
        val remoteBookResponse = if (isSearching) {
            bookRepository.getBookByPage(searchData, pageNumber, sortOption)
        } else {
            bookRepository.getBookByPage(pageNumber, sortOption)
        }
        return when {
            remoteBookResponse is RemoteBookResponse.Success &&
                    remoteBookResponse.remoteBook.bookList.isNotEmpty() -> {
                remoteBookResponse.remoteBook
            }
            searchData.toLongOrNull() != null -> {
                val bookList = ArrayList<Book>()
                val bookResponse = bookRepository.getBookDetails(searchData)
                when {
                    bookResponse is BookResponse.Success -> bookList.add(bookResponse.book)
                    bookResponse is BookResponse.Failure && needToUpdateErrorMessage -> {
                        remoteBookErrorSubject.onNext(bookResponse.error)
                    }
                }
                RemoteBook(bookList, 1, BOOKS_PER_PAGE)
            }
            remoteBookResponse is RemoteBookResponse.Failure && needToUpdateErrorMessage -> {
                remoteBookErrorSubject.onNext(remoteBookResponse.error)
                null
            }
            else -> null
        }
    }
}
