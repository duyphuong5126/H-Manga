package nhdphuong.com.manga.features.home

import android.annotation.SuppressLint
import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
        private val mView: HomeContract.View,
        private val mBookRepository: BookRepository,
        private val mTagRepository: TagRepository,
        private val mSharedPreferencesManager: SharedPreferencesManager,
        @IO private val io: CoroutineScope,
        @Main private val main: CoroutineScope
) : HomeContract.Presenter {
    companion object {
        private const val TAG = "HomePresenter"
        private const val NUMBER_OF_PREVENTIVE_PAGES = 10
    }

    private var mMainList = LinkedList<Book>()
    private var mCurrentNumOfPages = 0L
    private var mCurrentLimitPerPage = 0
    private var mCurrentPage = 1L

    @SuppressLint("UseSparseArrays")
    private var mPreventiveData = HashMap<Long, LinkedList<Book>>()

    private var isLoadingPreventiveData = false
    private val mJobStack = Stack<Job>()
    private var isRefreshing = AtomicBoolean(false)

    private var mSearchData: String = ""
    private val isSearching: Boolean get() = !TextUtils.isEmpty(mSearchData)

    private val mTagsDownloadManager = DownloadManager.Companion.TagsDownloadManager

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        Logger.d(TAG, "start")
        reload()
        if (!mSharedPreferencesManager.tagsDataDownloaded) {
            mTagsDownloadManager.startDownloading()
            mTagRepository.fetchAllTagLists { isSuccess ->
                Logger.d(TAG, "Tags fetching completed, isSuccess=$isSuccess")
                if (isSuccess) {
                    mSharedPreferencesManager.tagsDataDownloaded = true
                }
                mTagsDownloadManager.stopDownloading()

                val onVersionFetched = {
                    main.launch {
                        mView.startUpdateTagsService()
                    }
                }
                mTagRepository.getCurrentVersion(onSuccess = { newVersion ->
                    if (mSharedPreferencesManager.currentTagVersion != newVersion) {
                        Logger.d(TAG, "New version is available, " +
                                "new version: $newVersion," +
                                " current version: ${mSharedPreferencesManager.currentTagVersion}")
                        mSharedPreferencesManager.currentTagVersion = newVersion
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
            mView.startUpdateTagsService()
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
        Logger.d(TAG, "Navigate to last page: $mCurrentNumOfPages")
        onPageChange(mCurrentNumOfPages)
    }

    override fun reloadCurrentPage(onRefreshed: () -> Unit) {
        if (isRefreshing.compareAndSet(false, true)) {
            io.launch {
                val remoteBooks = getBooksListByPage(mCurrentPage)
                mCurrentNumOfPages = remoteBooks?.numOfPages ?: 0L
                val isCurrentPageEmpty = if (remoteBooks != null) {
                    remoteBooks.bookList.let { bookList ->
                        mPreventiveData[mCurrentPage]?.let { page ->
                            page.clear()
                            page.addAll(bookList)
                        }
                        mMainList.clear()
                        mMainList.addAll(bookList)
                    }
                    false
                } else {
                    true
                }
                isRefreshing.compareAndSet(true, false)

                main.launch {
                    if (!isCurrentPageEmpty) {
                        mView.refreshHomePagination(mCurrentNumOfPages)
                        mView.refreshHomeBookList()
                    }
                    onRefreshed()
                    mView.showNothingView(isCurrentPageEmpty)
                }
            }
        } else {
            mView.showRefreshingDialog()
        }
    }

    override fun reloadLastBookListRefreshTime() {
        mSharedPreferencesManager.getLastBookListRefreshTime().let { lastRefreshTime ->
            mView.showLastBookListRefreshTime(
                    SupportUtils.getTimeElapsed(
                            System.currentTimeMillis() - lastRefreshTime
                    ).toLowerCase(Locale.US)
            )
        }
    }

    override fun reloadRecentBooks() {
        io.launch {
            val recentList = LinkedList<Int>()
            val favoriteList = LinkedList<Int>()
            for (id in 0 until mMainList.size) {
                mMainList[id].bookId.let { bookId ->
                    when {
                        mBookRepository.isFavoriteBook(bookId) -> favoriteList.add(id)
                        mBookRepository.isRecentBook(bookId) -> recentList.add(id)
                        else -> {
                        }
                    }
                }
            }

            main.launch {
                if (!recentList.isEmpty()) {
                    mView.showRecentBooks(recentList)
                }
                if (!favoriteList.isEmpty()) {
                    mView.showFavoriteBooks(favoriteList)
                }
            }
        }
    }

    override fun saveLastBookListRefreshTime() {
        mSharedPreferencesManager.setLastBookListRefreshTime(System.currentTimeMillis())
    }

    override fun updateSearchData(data: String) {
        if (!mSearchData.equals(data, ignoreCase = true)) {
            mSearchData = data
            mView.changeSearchResult(data)
            reload()
        } else {
            Logger.d(TAG, "Search data is not changed")
        }
    }

    override fun pickBookRandomly() {
        mView.showLoading()
        io.launch {
            val random = Random()
            val randomPage = random.nextInt(mCurrentNumOfPages.toInt()) + 1
            getBooksListByPage(randomPage.toLong())?.bookList.let { randomBooks ->
                Logger.d(TAG, "Randomized paged $randomPage," +
                        " books count=${randomBooks?.size ?: 0}")
                if (randomBooks?.isEmpty() == false) {
                    val randomIndex = random.nextInt(randomBooks.size)
                    main.launch {
                        mView.hideLoading()
                        mView.showRandomBook(randomBooks[randomIndex])
                    }
                }
            }
        }
    }

    override fun stop() {
        Logger.d(TAG, "stop")
        io.launch {
            while (mJobStack.size > 0) {
                val job = mJobStack.pop()
                job.cancel()
            }
        }
        clearData()
    }

    private fun reload() {
        clearData()

        mView.showLoading()
        mView.setUpHomeBookList(mMainList)
        val downloadingJob = io.launch {
            val startTime = System.currentTimeMillis()
            getBooksListByPage(mCurrentPage).let { remoteBook ->
                Logger.d(TAG, "Time spent=${System.currentTimeMillis() - startTime}")
                mCurrentNumOfPages = remoteBook?.numOfPages ?: 0L
                mCurrentLimitPerPage = remoteBook?.numOfBooksPerPage ?: 0
                Logger.d(TAG, "Remote books: $mCurrentNumOfPages")
                val bookList = remoteBook?.bookList ?: LinkedList()
                mMainList.addAll(bookList)
                mPreventiveData[mCurrentPage] = bookList
                for (book in bookList) {
                    Logger.d(TAG, book.logString)
                }

                loadPreventiveData()

                main.launch {
                    mView.refreshHomeBookList()
                    if (mCurrentNumOfPages > 0) {
                        mView.refreshHomePagination(mCurrentNumOfPages)
                        mView.showNothingView(false)
                    } else {
                        mView.showNothingView(true)
                    }
                    mView.hideLoading()
                }
            }
        }
        mJobStack.push(downloadingJob)
    }

    private fun onPageChange(pageNumber: Long) {
        mCurrentPage = pageNumber
        io.launch {
            mMainList.clear()
            var newPage = false
            val currentList: LinkedList<Book> = if (mPreventiveData.containsKey(mCurrentPage)) {
                mPreventiveData[mCurrentPage] as LinkedList<Book>
            } else {
                newPage = true
                main.launch {
                    mView.showLoading()
                }
                val bookList = getBooksListByPage(mCurrentPage)?.bookList ?: LinkedList()
                mPreventiveData[mCurrentPage] = bookList
                bookList
            }
            mMainList.addAll(currentList)

            val toLoadList: List<Long> = when (mCurrentPage) {
                1L -> listOf(2L)
                mCurrentNumOfPages -> listOf(mCurrentNumOfPages - 1)
                else -> listOf(mCurrentPage - 1, mCurrentPage + 1)
            }
            logListLong("To load list: ", toLoadList)

            for (page in toLoadList.iterator()) {
                if (!mPreventiveData.containsKey(page)) {
                    mPreventiveData[page] = LinkedList()
                    launch {
                        getBooksListByPage(page)?.bookList?.let { bookList ->
                            mPreventiveData[page]?.addAll(bookList)
                        }
                    }
                    Logger.d(TAG, "Page $page loaded")
                }
            }

            if (mPreventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                val pageList = sortListPage(mCurrentPage, LinkedList(mPreventiveData.keys))
                var pageId = 0
                logListLong("Before deleted page list: ", pageList)
                while (mPreventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    Logger.d(TAG, "Remove page: $pageId")
                    val page = pageList[pageId++]
                    (mPreventiveData[page] as LinkedList).clear()
                    mPreventiveData.remove(page)
                }
            }
            logListLong("Final page list: ", LinkedList(mPreventiveData.keys))

            main.launch {
                mView.refreshHomeBookList()
                if (newPage) {
                    mView.hideLoading()
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
                for (page in mCurrentPage + 1L..NUMBER_OF_PREVENTIVE_PAGES.toLong()) {
                    Logger.d(TAG, "Start loading page $page")
                    io.launch {
                        val remoteBook = getBooksListByPage(page)
                        Logger.d(TAG, "Done loading page $page")
                        remoteBook?.bookList?.let { bookList ->
                            if (!bookList.isEmpty()) {
                                mPreventiveData[page] = bookList
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
        mMainList.clear()
        for (entry in mPreventiveData.entries) {
            entry.value.clear()
        }
        mPreventiveData.clear()
        mCurrentNumOfPages = 0L
        mCurrentLimitPerPage = 0
        mCurrentPage = 1
        isLoadingPreventiveData = false
        isRefreshing.compareAndSet(true, false)
    }

    private suspend fun getBooksListByPage(pageNumber: Long): RemoteBook? {
        return if (isSearching) {
            mBookRepository.getBookByPage(mSearchData, pageNumber)
        } else {
            mBookRepository.getBookByPage(pageNumber)
        }
    }
}
