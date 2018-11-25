package nhdphuong.com.manga.features.home

import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.data.repository.TagRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.SupportUtils
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import kotlin.collections.HashMap

/*
 * Created by nhdphuong on 3/18/18.
 */

class HomePresenter @Inject constructor(private val mView: HomeContract.View,
                                        private val mBookRepository: BookRepository,
                                        private val mTagRepository: TagRepository,
                                        private val mSharedPreferencesManager: SharedPreferencesManager,
                                        @IO private val io: CoroutineScope,
                                        @Main private val main: CoroutineScope) : HomeContract.Presenter {
    companion object {
        private const val TAG = "HomePresenter"
        private const val NUMBER_OF_PREVENTIVE_PAGES = 10
    }

    private var mMainList = LinkedList<Book>()
    private var mCurrentNumOfPages = 0L
    private var mCurrentLimitPerPage = 0
    private var mCurrentPage = 1
    private var mPreventiveData = HashMap<Int, LinkedList<Book>>()
    private var isLoadingPreventiveData = false
    private val mJobStack = Stack<Job>()
    private var isRefreshing: Boolean = false

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
                        NHentaiApp.instance.startUpdateTagsService()
                    }
                }
                mTagRepository.getCurrentVersion(onSuccess = { newVersion ->
                    if (mSharedPreferencesManager.currentTagVersion != newVersion) {
                        Logger.d(TAG, "New version is available, new version: $newVersion, current version: ${mSharedPreferencesManager.currentTagVersion}")
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
            NHentaiApp.instance.startUpdateTagsService()
        }
    }

    override fun jumpToPage(pageNumber: Int) {
        Logger.d(TAG, "Navigate to page $pageNumber")
        onPageChange(pageNumber)
    }

    override fun jumToFirstPage() {
        Logger.d(TAG, "Navigate to first page")
        onPageChange(1)
    }

    override fun jumToLastPage() {
        mCurrentNumOfPages.toInt().let { currentNumberOfPages ->
            Logger.d(TAG, "Navigate to last page: $currentNumberOfPages")
            onPageChange(currentNumberOfPages)
        }
    }

    override fun reloadCurrentPage(onRefreshed: () -> Unit) {
        if (!isRefreshing) {
            isRefreshing = true
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
                isRefreshing = false

                main.launch {
                    if (!isCurrentPageEmpty) {
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
            mView.showLastBookListRefreshTime(SupportUtils.getTimeElapsed(System.currentTimeMillis() - lastRefreshTime).toLowerCase())
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

    private fun onPageChange(pageNumber: Int) {
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

            val toLoadList: List<Int> = when (mCurrentPage) {
                1 -> listOf(2)
                mCurrentNumOfPages.toInt() -> listOf((mCurrentNumOfPages - 1).toInt())
                else -> listOf(mCurrentPage - 1, mCurrentPage + 1)
            }
            logListInt("To load list: ", toLoadList)

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
                logListInt("Before deleted page list: ", pageList)
                while (mPreventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    Logger.d(TAG, "Remove page: $pageId")
                    val page = pageList[pageId++]
                    (mPreventiveData[page] as LinkedList).clear()
                    mPreventiveData.remove(page)
                }
            }
            logListInt("Final page list: ", LinkedList(mPreventiveData.keys))

            main.launch {
                mView.refreshHomeBookList()
                if (newPage) {
                    mView.hideLoading()
                }
            }
        }
    }

    private fun sortListPage(anchor: Int, pageList: LinkedList<Int>): LinkedList<Int> {
        if (pageList.isEmpty()) {
            return pageList
        }
        val size = pageList.size
        for (i in 0 until size - 1) {
            for (j in i + 1 until size) {
                if (Math.abs(pageList[i] - anchor) < Math.abs(pageList[j] - anchor)) {
                    Collections.swap(pageList, i, j)
                }
            }
        }
        return pageList
    }

    private fun logListInt(messageString: String, listInt: List<Int>) {
        var message = "$messageString["
        for (i in 0 until listInt.size - 1) {
            message += "${listInt[i]}, "
        }
        message += "${listInt[listInt.size - 1]}]"
        Logger.d(TAG, message)
    }

    private suspend fun loadPreventiveData() {
        isLoadingPreventiveData = true
        val countDownLatch = CountDownLatch(NUMBER_OF_PREVENTIVE_PAGES - mCurrentPage)
        for (page in mCurrentPage + 1..NUMBER_OF_PREVENTIVE_PAGES) {
            Logger.d(TAG, "Start loading page $page")
            io.launch {
                val remoteBook = getBooksListByPage(page)
                Logger.d(TAG, "Done loading page $page")
                remoteBook?.bookList?.let { bookList ->
                    if (!bookList.isEmpty()) {
                        mPreventiveData[page] = bookList
                    }
                }
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
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
        isRefreshing = false
    }

    private suspend fun getBooksListByPage(pageNumber: Int): RemoteBook? = if (isSearching) mBookRepository.getBookByPage(mSearchData, pageNumber) else mBookRepository.getBookByPage(pageNumber)
}