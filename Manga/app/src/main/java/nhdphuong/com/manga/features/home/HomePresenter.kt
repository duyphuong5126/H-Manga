package nhdphuong.com.manga.features.home

import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.RemoteBook
import nhdphuong.com.manga.data.repository.BookRepository
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
                                        private val mSharedPreferencesManager: SharedPreferencesManager) : HomeContract.Presenter {
    companion object {
        private val TAG = HomePresenter::class.java.simpleName
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

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        Log.d(TAG, "start")
        reload()
    }

    override fun jumpToPage(pageNumber: Int) {
        Log.d(TAG, "Current page: $pageNumber")
        mCurrentPage = pageNumber
        onPageChange()
    }

    override fun jumToFirstPage() {
        mCurrentPage = 1
        Log.d(TAG, "Current page: $mCurrentPage")
        onPageChange()
    }

    override fun jumToLastPage() {
        mCurrentPage = mCurrentNumOfPages.toInt()
        Log.d(TAG, "Current page: $mCurrentPage")
        onPageChange()
    }

    override fun reloadCurrentPage(onRefreshed: () -> Unit) {
        if (!isRefreshing) {
            isRefreshing = true
            launch {
                val remoteBooks = getBookByPage(mCurrentPage)
                mCurrentNumOfPages = remoteBooks?.numOfPages ?: 0L
                if (remoteBooks != null) {
                    remoteBooks.bookList.let { bookList ->
                        mPreventiveData[mCurrentPage]?.let { page ->
                            page.clear()
                            page.addAll(bookList)
                        }
                        mMainList.clear()
                        mMainList.addAll(bookList)

                        launch(UI) {
                            mView.refreshHomeBookList()
                            onRefreshed()
                            mView.showNothingView(false)
                            mView.refreshHomePagination(mCurrentNumOfPages)
                            isRefreshing = false
                        }
                    }
                } else {
                    launch(UI) {
                        onRefreshed()
                        mView.showNothingView(true)
                        isRefreshing = false
                    }
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
        launch {
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

            launch(UI) {
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
            reload()
        } else {
            Log.d(TAG, "Search data is not changed")
        }
    }

    override fun stop() {
        Log.d(TAG, "stop")
        launch {
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
        val job = launch {
            val startTime = System.currentTimeMillis()
            getBookByPage(mCurrentPage).let { remoteBook ->
                Log.d(TAG, "Time spent=${System.currentTimeMillis() - startTime}")
                mCurrentNumOfPages = remoteBook?.numOfPages ?: 0L
                mCurrentLimitPerPage = remoteBook?.numOfBooksPerPage ?: 0
                Log.d(TAG, "Remote books: $mCurrentNumOfPages")
                val bookList = remoteBook?.bookList ?: LinkedList()
                mMainList.addAll(bookList)
                mPreventiveData[mCurrentPage] = bookList
                for (book in bookList) {
                    Log.d(TAG, book.logString)
                }

                loadPreventiveData()

                launch(UI) {
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
        mJobStack.push(job)
    }

    private fun onPageChange() {
        launch {
            mMainList.clear()
            var newPage = false
            val currentList: LinkedList<Book> = if (mPreventiveData.containsKey(mCurrentPage)) {
                mPreventiveData[mCurrentPage] as LinkedList<Book>
            } else {
                newPage = true
                launch(UI) {
                    mView.showLoading()
                }
                val bookList = getBookByPage(mCurrentPage)?.bookList ?: LinkedList()
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
                    getBookByPage(page)?.bookList?.let { bookList ->
                        mPreventiveData[page] = bookList
                    }
                    Log.d(TAG, "Page $page loaded")
                }
            }

            if (mPreventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                val pageList = sortListPage(mCurrentPage, LinkedList(mPreventiveData.keys))
                var pageId = 0
                logListInt("Before deleted page list: ", pageList)
                while (mPreventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    val page = pageList[pageId++]
                    (mPreventiveData[page] as LinkedList).clear()
                    mPreventiveData.remove(page)
                }
            }
            logListInt("Final page list: ", LinkedList(mPreventiveData.keys))

            launch(UI) {
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
        Log.d(TAG, message)
    }

    private suspend fun loadPreventiveData() {
        isLoadingPreventiveData = true
        val countDownLatch = CountDownLatch(NUMBER_OF_PREVENTIVE_PAGES - mCurrentPage)
        for (page in mCurrentPage + 1..NUMBER_OF_PREVENTIVE_PAGES) {
            Log.d(TAG, "Start loading page $page")
            async {
                val remoteBook = getBookByPage(page)
                Log.d(TAG, "Done loading page $page")
                remoteBook?.bookList?.let { bookList ->
                    if (!bookList.isEmpty()) {
                        mPreventiveData[page] = bookList
                    }
                }
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
        Log.d(TAG, "Load preventive data successfully")
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

    private suspend fun getBookByPage(pageNumber: Int): RemoteBook? = if (isSearching) mBookRepository.getBookByPage(mSearchData, pageNumber) else mBookRepository.getBookByPage(pageNumber)
}