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
import nhdphuong.com.manga.Constants.Companion.EVENT_BLOCK_RECOMMENDED_BOOK
import nhdphuong.com.manga.Constants.Companion.EVENT_CLICK_RECOMMENDED_BOOK
import nhdphuong.com.manga.Constants.Companion.EVENT_FAILED_TO_LOAD_HOME
import nhdphuong.com.manga.Constants.Companion.EVENT_FAILED_TO_SEARCH
import nhdphuong.com.manga.Constants.Companion.EVENT_SEARCH
import nhdphuong.com.manga.Constants.Companion.MAX_PER_PAGE
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_ANALYTICS_BOOK_ID
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_SEARCH_DATA
import nhdphuong.com.manga.DownloadManager.Companion.TagsDownloadManager as tagsDownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.analytics.AnalyticsParam
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.entity.book.SortOption.Recent
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.enum.ErrorEnum
import nhdphuong.com.manga.extension.isNetworkError
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.INetworkUtils
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.usecase.CheckRecentFavoriteMigrationNeededUseCase
import nhdphuong.com.manga.usecase.GetOldestPendingDownloadBookUseCase
import nhdphuong.com.manga.usecase.GetRecommendedBooksFromFavoriteUseCase
import nhdphuong.com.manga.usecase.GetRecommendedBooksFromReadingHistoryUseCase
import nhdphuong.com.manga.usecase.GetRecommendedBooksUseCase
import nhdphuong.com.manga.usecase.LogAnalyticsEventUseCase
import nhdphuong.com.manga.usecase.StartBookDownloadingUseCase
import java.net.SocketTimeoutException
import java.util.Calendar
import java.util.Locale
import java.util.Random
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.HashMap
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
    private val checkRecentFavoriteMigrationNeededUseCase: CheckRecentFavoriteMigrationNeededUseCase,
    private val getRecommendedBooksFromFavoriteUseCase: GetRecommendedBooksFromFavoriteUseCase,
    private val getRecommendedBooksFromReadingHistoryUseCase: GetRecommendedBooksFromReadingHistoryUseCase,
    private val getRecommendedBooksUseCase: GetRecommendedBooksUseCase,
    private val getOldestPendingDownloadBookUseCase: GetOldestPendingDownloadBookUseCase,
    private val startBookDownloadingUseCase: StartBookDownloadingUseCase,
    private val networkUtils: INetworkUtils,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : HomeContract.Presenter {
    companion object {
        private const val NUMBER_OF_PREVENTIVE_PAGES = 10
        private const val MAX_TRYING_PAGES = 5
        private const val BOOKS_PER_PAGE = MAX_PER_PAGE
        private const val TIMES_OPEN_APP_NEED_ALTERNATIVE_DOMAINS_MESSAGE = 20
    }

    private val logger = Logger("HomePresenter")

    private var mainList = CopyOnWriteArrayList<Book>()
    private var currentNumOfPages = 0L
    private var currentPage = 1L

    @SuppressLint("UseSparseArrays")
    private var preventiveData = HashMap<Long, ArrayList<Book>>()

    private var recommendedBooks = arrayListOf<Book>()

    private val jobList = CopyOnWriteArrayList<Job>()
    private var isRefreshing = AtomicBoolean(false)

    private var searchData: String = ""
    private val isSearching: Boolean get() = !TextUtils.isEmpty(searchData)

    private val newerVersionAcknowledged = AtomicBoolean(false)

    private var sortOption = Recent
        set(value) {
            val lastValue = field
            field = value
            if (lastValue != value) {
                reload(false)
            }
        }

    private val remoteBookErrorSubject = PublishSubject.create<Throwable>()

    private val compositeDisposable = CompositeDisposable()

    private val isBeingReloaded = AtomicBoolean(false)

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
        logger.d("start")
        sharedPreferencesManager.timesOpenApp++
        reload(true)
        if (!sharedPreferencesManager.tagsDataDownloaded) {
            tagsDownloadManager.startDownloading()
            masterDataRepository.fetchAllTagLists { isSuccess ->
                logger.d("Tags fetching completed, isSuccess=$isSuccess")
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
                        logger.d("New version is available, new version: $newVersion, current version: ${sharedPreferencesManager.currentTagVersion}")
                        sharedPreferencesManager.currentTagVersion = newVersion
                    } else {
                        logger.d("App is already updated to the version $newVersion")
                    }
                    onVersionFetched()
                }, onError = {
                    logger.e("Version fetching failed")
                    onVersionFetched()
                })
            }
        } else {
            view.startUpdateTagsService()
        }
        checkRecentFavoriteMigrationNeededUseCase.execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ migrationNeeded ->
                logger.d("Need migration $migrationNeeded")
                if (migrationNeeded) {
                    view.startRecentFavoriteMigration()
                }
            }, {
                logger.e("Failed to check migration needed with error $it")
            }).addTo(compositeDisposable)

        checkAndResumeBookDownloading()
    }

    override fun jumpToPage(pageNumber: Long) {
        logger.d("Navigate to page $pageNumber")
        onPageChange(pageNumber)
    }

    override fun jumToFirstPage() {
        logger.d("Navigate to first page")
        onPageChange(1)
    }

    override fun jumToLastPage() {
        logger.d("Navigate to last page: $currentNumOfPages")
        onPageChange(currentNumOfPages)
    }

    override fun setNewerVersionAcknowledged() {
        newerVersionAcknowledged.compareAndSet(false, true)
    }

    override fun reloadIfEmpty() {
        if (mainList.isEmpty()) {
            reload(false)
        }
    }

    override fun refreshAppVersion() {
        io.launch {
            val upgradeNotificationAllowed = sharedPreferencesManager.isUpgradeNotificationAllowed
            masterDataRepository.getAppVersion(onSuccess = { latestVersion ->
                logger.d("Latest version: $latestVersion")
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
                logger.e("Failed to get app version with error: $error")
            })
        }
    }

    override fun reloadCurrentPage() {
        if (!networkUtils.isNetworkConnected()) {
            return
        }
        if (isRefreshing.compareAndSet(false, true)) {
            view.showLoading()
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
                        view.hideLoading()
                        if (!isCurrentPageEmpty) {
                            view.refreshHomePagination(currentNumOfPages, currentPage.toInt() - 1)
                            view.refreshHomeBookList()
                        }
                        view.finishRefreshing()
                        if (isCurrentPageEmpty) {
                            view.showNothingView()
                            logFailedToLoadHomeEvent()
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
            val elapsedTime = System.currentTimeMillis() - lastRefreshTime
            val elapsedTimeLabel = SupportUtils.getTimeElapsed(elapsedTime).lowercase(Locale.US)
            view.showLastBookListRefreshTime(elapsedTimeLabel)
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
            view.changeSearchInfo(searchData)
            val analyticsParam = AnalyticsParam(PARAM_NAME_SEARCH_DATA, data)
            logAnalyticsEventUseCase.execute(EVENT_SEARCH, analyticsParam)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addTo(compositeDisposable)
            reload(false)
        } else {
            logger.d("Search data is not changed")
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
                logger.d("Randomized paged $randomPage, books count=${randomBooks?.size ?: 0}")
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
        logger.d("sortOption $sortOption")
        this.sortOption = sortOption
        if (view.isActive()) {
            view.enableSortOption(sortOption)
        }
    }

    override fun checkedOutAlternativeDomains() {
        sharedPreferencesManager.checkedOutAlternativeDomains = true
    }

    override fun doNoRecommendBook(bookId: String) {
        io.launch {
            bookRepository.addBookToBlockList(bookId)
            recommendedBooks.removeAll { it.bookId == bookId }
            val isNoRecommendedBook = recommendedBooks.isEmpty()
            main.launch {
                view.refreshRecommendBooks()
                if (isNoRecommendedBook) {
                    view.hideRecommendBooks()
                }
            }

            val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, bookId)
            logAnalyticsEventUseCase.execute(EVENT_BLOCK_RECOMMENDED_BOOK, bookIdParam)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addTo(compositeDisposable)
        }
    }

    override fun checkOutRecommendedBook(bookId: String) {
        val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, bookId)
        logAnalyticsEventUseCase.execute(EVENT_CLICK_RECOMMENDED_BOOK, bookIdParam)
            .subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(compositeDisposable)
    }

    override fun checkAndResumeBookDownloading() {
        getOldestPendingDownloadBookUseCase.execute()
            .flatMapCompletable(startBookDownloadingUseCase::execute)
            .subscribeOn(Schedulers.io())
            .subscribe({
                logger.d("Pending list checking completed")
            }, {
                logger.d("Pending list checking failed with error $it")
            }).addTo(compositeDisposable)
    }

    override fun stop() {
        logger.d("stop")
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
        if (!isBeingReloaded.compareAndSet(false, true)) {
            return
        }
        clearData()

        view.showLoading()
        view.setUpHomeBookList(mainList)
        val downloadingJob = io.launch {
            try {
                val startTime = System.currentTimeMillis()
                var remoteBook = getBooksListByPage(currentPage, true)
                var triedPages = 1
                while (remoteBook == null && triedPages < MAX_TRYING_PAGES && networkUtils.isNetworkConnected()) {
                    logger.d("Page $currentPage is empty, trying page ${currentPage + 1}")
                    currentPage++
                    triedPages++
                    remoteBook = getBooksListByPage(currentPage, true)
                }
                logger.d("Time spent=${System.currentTimeMillis() - startTime}")
                currentNumOfPages = remoteBook?.numOfPages ?: 0
                logger.d("Remote books: $currentNumOfPages")
                val bookList = remoteBook?.bookList ?: ArrayList()
                mainList.addAll(bookList)
                preventiveData[currentPage] = bookList

                val timesOpenApp = sharedPreferencesManager.timesOpenApp
                val timesOpenAppMatched =
                    (timesOpenApp % TIMES_OPEN_APP_NEED_ALTERNATIVE_DOMAINS_MESSAGE == 0 || timesOpenApp == 1)
                val checkedOutAlternativeDomains =
                    sharedPreferencesManager.checkedOutAlternativeDomains
                val alternativeDomainInUsed = sharedPreferencesManager.useAlternativeDomain
                val showAlternativeDomainsQuestion =
                    needToAskAlternativeDomains && timesOpenAppMatched
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
                            syncRecommendedBooks()
                        } else {
                            view.showNothingView()
                            view.hideSortOptionList()
                            logFailedToLoadHomeEvent()
                        }
                        view.hideLoading()
                        if (showAlternativeDomainsQuestion) {
                            view.showAlternativeDomainsQuestion()
                        }
                    }
                }
            } catch (throwable: Throwable) {
                isBeingReloaded.compareAndSet(true, false)
            } finally {
                isBeingReloaded.compareAndSet(true, false)
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

            main.launch {
                if (view.isActive()) {
                    view.refreshHomeBookList()
                    if (newPage) {
                        view.hideLoading()
                    }
                }
            }

            val toLoadList: List<Long> = when (currentPage) {
                1L -> listOf(2L)
                currentNumOfPages -> listOf(currentNumOfPages - 1)
                else -> listOf(currentPage - 1, currentPage + 1)
            }

            for (page in toLoadList.iterator()) {
                if (!preventiveData.containsKey(page)) {
                    preventiveData[page] = ArrayList()
                    getBooksListByPage(page, false)?.bookList?.let { bookList ->
                        preventiveData[page]?.addAll(bookList)
                    }
                    logger.d("Page $page loaded")
                }
            }

            if (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                val pageList = sortListPage(currentPage, ArrayList(preventiveData.keys))
                var pageId = 0
                while (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    logger.d("Remove page: $pageId")
                    val page = pageList[pageId++]
                    (preventiveData[page] as ArrayList).clear()
                    preventiveData.remove(page)
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

    private fun clearData() {
        mainList.clear()
        for (entry in preventiveData.entries) {
            entry.value.clear()
        }
        preventiveData.clear()
        recommendedBooks.clear()
        currentNumOfPages = 0L
        currentPage = 1
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

    private fun syncRecommendedBooks() {
        val dayOfWeek = Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_WEEK)
        getRecommendedBooksUseCase.execute(dayOfWeek, searchData)
            .switchIfEmpty(getRecommendedBooksFromReadingHistoryUseCase.execute())
            .switchIfEmpty(getRecommendedBooksFromFavoriteUseCase.execute())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                logger.d("Recommended books: ${it.size}")
                if (it.isNotEmpty()) {
                    recommendedBooks.clear()
                    recommendedBooks.addAll(it)
                    view.showRecommendBooks(recommendedBooks)
                } else {
                    view.hideRecommendBooks()
                }
            }, {
                logger.e("Failed to get recommended books with error $it")
                view.hideRecommendBooks()
            })
            .addTo(compositeDisposable)
    }

    private fun logFailedToLoadHomeEvent() {
        if (searchData.isNotBlank()) {
            logger.d("FailedToSearch event was logged")
            val searchParam = AnalyticsParam(PARAM_NAME_SEARCH_DATA, searchData)
            logAnalyticsEventUseCase.execute(EVENT_FAILED_TO_SEARCH, searchParam)
        } else {
            logger.d("FailedToLoadHome event was logged")
            logAnalyticsEventUseCase.execute(EVENT_FAILED_TO_LOAD_HOME)
        }.subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(compositeDisposable)
    }
}
