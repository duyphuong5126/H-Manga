package nhdphuong.com.manga.features.home

import android.annotation.SuppressLint
import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Constants.Companion.MAX_PER_PAGE
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.data.repository.TagRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.SupportUtils
import java.util.Locale
import java.util.LinkedList
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
    private val tagRepository: TagRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : HomeContract.Presenter {
    companion object {
        private const val TAG = "HomePresenter"
        private const val NUMBER_OF_PREVENTIVE_PAGES = 10
        private const val BOOKS_PER_PAGE = MAX_PER_PAGE
    }

    private var mainList = LinkedList<Book>()
    private var currentNumOfPages = 0L
    private var currentLimitPerPage = 0
    private var currentPage = 1L

    @SuppressLint("UseSparseArrays")
    private var preventiveData = HashMap<Long, LinkedList<Book>>()

    private var isLoadingPreventiveData = false
    private val jobStack = Stack<Job>()
    private var isRefreshing = AtomicBoolean(false)

    private var searchData: String = ""
    private val isSearching: Boolean get() = !TextUtils.isEmpty(searchData)

    private val tagsDownloadManager = DownloadManager.Companion.TagsDownloadManager

    init {
        view.setPresenter(this)
    }

    override fun start() {
        Logger.d(TAG, "start")
        reload()
        if (!sharedPreferencesManager.tagsDataDownloaded) {
            tagsDownloadManager.startDownloading()
            tagRepository.fetchAllTagLists { isSuccess ->
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
                tagRepository.getCurrentVersion(onSuccess = { newVersion ->
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
                    if (!isCurrentPageEmpty) {
                        view.refreshHomePagination(currentNumOfPages)
                        view.refreshHomeBookList()
                    }
                    onRefreshed()
                    view.showNothingView(isCurrentPageEmpty)
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
            val recentList = LinkedList<String>()
            val favoriteList = LinkedList<String>()
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
                if (!recentList.isEmpty()) {
                    view.showRecentBooks(recentList)
                }
                if (!favoriteList.isEmpty()) {
                    view.showFavoriteBooks(favoriteList)
                }
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
            val randomPage = random.nextInt(currentNumOfPages.toInt()) + 1
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
                val bookList = remoteBook?.bookList ?: LinkedList()
                mainList.addAll(bookList)
                preventiveData[currentPage] = bookList
                for (book in bookList) {
                    Logger.d(TAG, book.logString)
                }

                loadPreventiveData()

                main.launch {
                    view.refreshHomeBookList()
                    if (currentNumOfPages > 0) {
                        view.refreshHomePagination(currentNumOfPages)
                        view.showNothingView(false)
                    } else {
                        view.showNothingView(true)
                    }
                    view.hideLoading()
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
            val currentList: LinkedList<Book> = if (preventiveData.containsKey(currentPage)) {
                preventiveData[currentPage] as LinkedList<Book>
            } else {
                newPage = true
                main.launch {
                    view.showLoading()
                }
                val bookList = getBooksListByPage(currentPage)?.bookList ?: LinkedList()
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
                    preventiveData[page] = LinkedList()
                    launch {
                        getBooksListByPage(page)?.bookList?.let { bookList ->
                            preventiveData[page]?.addAll(bookList)
                        }
                    }
                    Logger.d(TAG, "Page $page loaded")
                }
            }

            if (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                val pageList = sortListPage(currentPage, LinkedList(preventiveData.keys))
                var pageId = 0
                logListLong("Before deleted page list: ", pageList)
                while (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    Logger.d(TAG, "Remove page: $pageId")
                    val page = pageList[pageId++]
                    (preventiveData[page] as LinkedList).clear()
                    preventiveData.remove(page)
                }
            }
            logListLong("Final page list: ", LinkedList(preventiveData.keys))

            main.launch {
                view.refreshHomeBookList()
                if (newPage) {
                    view.hideLoading()
                }
            }
        }
    }

    private fun sortListPage(anchor: Long, pageList: LinkedList<Long>): LinkedList<Long> {
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
                            if (!bookList.isEmpty()) {
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
        return if (isSearching) {
            val remoteBook = bookRepository.getBookByPage(searchData, pageNumber)
            if (remoteBook != null && remoteBook.bookList.isNotEmpty()) {
                remoteBook
            } else if (searchData.toLongOrNull() != null) {
                val bookData = bookRepository.getBookDetails(searchData)
                val bookList = LinkedList<Book>().apply {
                    if (bookData != null) {
                        add(bookData)
                    }
                }
                RemoteBook(bookList, 1, BOOKS_PER_PAGE)
            } else null
        } else {
            bookRepository.getBookByPage(pageNumber)
        }
    }
}
