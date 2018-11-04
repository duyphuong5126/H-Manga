package nhdphuong.com.manga.features.recent

import android.annotation.SuppressLint
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.supports.SupportUtils
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

/*
 * Created by nhdphuong on 6/10/18.
 */
class RecentPresenter @Inject constructor(private val mView: RecentContract.View,
                                          private val mBookRepository: BookRepository,
                                          private val mSharedPreferencesManager: SharedPreferencesManager) : RecentContract.Presenter {
    companion object {
        private const val TAG = "RecentPresenter"
        private const val MAX_PER_PAGE = 10
        private const val NUMBER_OF_PREVENTIVE_PAGES = 5
    }

    init {
        mView.setPresenter(this)
    }

    private var mRecentCount: Int = 0
    private var mFavoriteCount: Int = 0
    @RecentType
    private var mType: String? = null
    private var mCurrentPageCount: Int = 1
    private var mCurrentPage: Int = 1
    private val mRecentBookList = LinkedList<Book>()

    @SuppressLint("UseSparseArrays")
    private var mPreventiveData = HashMap<Int, LinkedList<Book>>()
    private var isLoadingPreventiveData = false
    private val mJobStack = Stack<Job>()

    override fun start() {
        mRecentBookList.clear()
        mView.setUpRecentBookList(mRecentBookList)

        launch {
            val job = launch {
                mRecentCount = mBookRepository.getRecentCount()
                mFavoriteCount = mBookRepository.getFavoriteCount()

                if (mType == Constants.RECENT) {
                    mCurrentPageCount = mRecentCount / MAX_PER_PAGE
                    if (mRecentCount % MAX_PER_PAGE > 0) {
                        mCurrentPageCount++
                    }
                } else {
                    mCurrentPageCount = mFavoriteCount / MAX_PER_PAGE
                    if (mFavoriteCount % MAX_PER_PAGE > 0) {
                        mCurrentPageCount++
                    }
                }
                launch(UI) {
                    if (mView.isActive()) {
                        mView.refreshRecentPagination(mCurrentPageCount)
                    }
                }
            }
            job.joinChildren()
            mJobStack.push(job)
        }
    }

    override fun setType(recentType: String) {
        mType = recentType

        mView.showLoading()
        launch {
            mRecentBookList.addAll(getRecentBook(mCurrentPage - 1))
            launch(UI) {
                if (mView.isActive()) {
                    mView.refreshRecentBookList()
                    mView.hideLoading()
                }
            }
            loadPreventiveData()
        }
    }

    override fun reloadRecentMarks() {
        val bookList = LinkedList<Int>()
        launch {
            if (mType == Constants.RECENT) {
                for (id in 0 until mRecentBookList.size) {
                    mRecentBookList[id].bookId.let { bookId ->
                        when {
                            mBookRepository.isRecentBook(bookId) -> bookList.add(id)
                            else -> {
                            }
                        }
                    }
                }

                launch(UI) {
                    if (!bookList.isEmpty()) {
                        mView.showRecentBooks(bookList)
                    }
                }
            } else {
                for (id in 0 until mRecentBookList.size) {
                    mRecentBookList[id].bookId.let { bookId ->
                        when {
                            mBookRepository.isFavoriteBook(bookId) -> bookList.add(id)
                            else -> {
                            }
                        }
                    }
                }

                launch(UI) {
                    if (!bookList.isEmpty()) {
                        mView.showFavoriteBooks(bookList)
                    }
                }
            }
        }
    }

    override fun jumpToPage(pageNumber: Int) {
        mCurrentPage = pageNumber
        onPageChange()
    }

    override fun jumToFirstPage() {
        mCurrentPage = 1
        onPageChange()
    }

    override fun jumToLastPage() {
        mCurrentPage = mCurrentPageCount
        onPageChange()
    }

    override fun reloadLastBookListRefreshTime() {
        mSharedPreferencesManager.getLastBookListRefreshTime().let { lastRefreshTime ->
            mView.showLastBookListRefreshTime(SupportUtils.getTimeElapsed(System.currentTimeMillis() - lastRefreshTime).toLowerCase())
        }
    }

    override fun saveLastBookListRefreshTime() {
        mSharedPreferencesManager.setLastBookListRefreshTime(System.currentTimeMillis())
    }

    override fun stop() {
        launch {
            while (mJobStack.size > 0) {
                val job = mJobStack.pop()
                job.cancel()
            }
        }
        mPreventiveData.clear()
        mRecentBookList.clear()
        isLoadingPreventiveData = false
        mCurrentPage = 1
        mCurrentPageCount = 0
        mType = null
    }

    private fun onPageChange() {
        launch {
            mRecentBookList.clear()
            var newPage = false
            val currentList: LinkedList<Book> = if (mPreventiveData.containsKey(mCurrentPage)) {
                mPreventiveData[mCurrentPage] as LinkedList<Book>
            } else {
                newPage = true
                launch(UI) {
                    mView.showLoading()
                }
                val bookList = getRecentBook(mCurrentPage - 1)
                mPreventiveData[mCurrentPage] = bookList
                bookList
            }
            mRecentBookList.addAll(currentList)

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
                mView.refreshRecentBookList()
                if (newPage) {
                    mView.hideLoading()
                }
            }
        }
    }

    private suspend fun loadPreventiveData() {
        isLoadingPreventiveData = true
        val countDownLatch = CountDownLatch(NUMBER_OF_PREVENTIVE_PAGES - mCurrentPage)
        for (page in mCurrentPage + 1..NUMBER_OF_PREVENTIVE_PAGES) {
            Logger.d(TAG, "Start loading page $page")
            mPreventiveData[page] = getRecentBook(page - 1)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        Logger.d(TAG, "Load preventive data successfully")
        isLoadingPreventiveData = false
    }

    private suspend fun getRecentBook(pageNumber: Int): LinkedList<Book> {
        val recentList = if (mType == Constants.RECENT) mBookRepository.getRecentBooks(MAX_PER_PAGE, pageNumber * MAX_PER_PAGE)
        else mBookRepository.getFavoriteBook(MAX_PER_PAGE, pageNumber * MAX_PER_PAGE)
        val bookList = LinkedList<Book>()
        for (recent in recentList) {
            bookList.add(mBookRepository.getBookDetails(recent.bookId)!!)
        }
        return bookList
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
}