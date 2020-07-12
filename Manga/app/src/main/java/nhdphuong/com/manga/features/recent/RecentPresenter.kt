package nhdphuong.com.manga.features.recent

import android.annotation.SuppressLint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.usecase.AnalyticsErrorLogUseCase
import java.util.LinkedList
import java.util.Collections
import java.util.Stack
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

/*
 * Created by nhdphuong on 6/10/18.
 */
class RecentPresenter @Inject constructor(
    private val view: RecentContract.View,
    private val bookRepository: BookRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val analyticsErrorLogUseCase: AnalyticsErrorLogUseCase,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : RecentContract.Presenter {
    companion object {
        private const val TAG = "RecentPresenter"
        private const val MAX_PER_PAGE = 25
        private const val NUMBER_OF_PREVENTIVE_PAGES = 5
    }

    init {
        view.setPresenter(this)
    }

    private var recentCount: Int = 0
    private var favoriteCount: Int = 0

    @RecentType
    private var type: String = Constants.RECENT
    private var currentPageCount: Int = 1
    private var currentPage: Int = 1
    private val recentBookList = LinkedList<Book>()

    @SuppressLint("UseSparseArrays")
    private var preventiveData = HashMap<Int, LinkedList<Book>>()
    private var isLoadingPreventiveData = AtomicBoolean(false)
    private val jobStack = Stack<Job>()

    private val compositeDisposable = CompositeDisposable()

    override fun start() {
        recentBookList.clear()
        view.showLoading()
        view.setUpRecentBookList(recentBookList)
        io.launch {
            recentBookList.addAll(getRecentBook(currentPage - 1))
            main.launch {
                if (view.isActive()) {
                    view.refreshRecentBookList()
                }
            }

            val job = launch {
                recentCount = bookRepository.getRecentCount()
                favoriteCount = bookRepository.getFavoriteCount()

                if (type == Constants.RECENT) {
                    currentPageCount = recentCount / MAX_PER_PAGE
                    if (recentCount % MAX_PER_PAGE > 0) {
                        currentPageCount++
                    }
                } else {
                    currentPageCount = favoriteCount / MAX_PER_PAGE
                    if (favoriteCount % MAX_PER_PAGE > 0) {
                        currentPageCount++
                    }
                }
                main.launch {
                    if (view.isActive()) {
                        if (currentPageCount == 0) {
                            view.showNothingView(type)
                        } else {
                            view.refreshRecentPagination(currentPageCount)
                        }
                        view.hideLoading()
                    }
                }

                loadPreventiveData()
            }
            jobStack.push(job)
        }
    }

    override fun setType(recentType: String) {
        type = recentType
    }

    override fun reloadRecentMarks() {
        val bookList = LinkedList<String>()
        io.launch {
            if (type == Constants.RECENT) {
                for (id in 0 until recentBookList.size) {
                    recentBookList[id].bookId.let { bookId ->
                        when {
                            bookRepository.isRecentBook(bookId) -> bookList.add(bookId)
                            else -> {
                            }
                        }
                    }
                }

                main.launch {
                    view.showRecentBooks(bookList)
                }
            } else {
                for (id in 0 until recentBookList.size) {
                    recentBookList[id].bookId.let { bookId ->
                        when {
                            bookRepository.isFavoriteBook(bookId) -> bookList.add(bookId)
                            else -> {
                            }
                        }
                    }
                }

                main.launch {
                    view.showFavoriteBooks(bookList)
                }
            }
        }
    }

    override fun jumpToPage(pageNumber: Int) {
        currentPage = pageNumber
        onPageChange()
    }

    override fun jumToFirstPage() {
        currentPage = 1
        onPageChange()
    }

    override fun jumToLastPage() {
        currentPage = currentPageCount
        onPageChange()
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

    override fun saveLastBookListRefreshTime() {
        sharedPreferencesManager.setLastBookListRefreshTime(System.currentTimeMillis())
    }

    override fun stop() {
        io.launch {
            while (jobStack.size > 0) {
                val job = jobStack.pop()
                job.cancel()
            }
        }
        compositeDisposable.clear()
        preventiveData.clear()
        recentBookList.clear()
        isLoadingPreventiveData.compareAndSet(isLoadingPreventiveData.get(), false)
        currentPage = 1
        currentPageCount = 0
    }

    private fun onPageChange() {
        io.launch {
            recentBookList.clear()
            var newPage = false
            val currentList: LinkedList<Book> = if (preventiveData.containsKey(currentPage)) {
                preventiveData[currentPage] as LinkedList<Book>
            } else {
                newPage = true
                main.launch {
                    view.showLoading()
                }
                val bookList = getRecentBook(currentPage - 1)
                preventiveData[currentPage] = bookList
                bookList
            }
            recentBookList.addAll(currentList)

            if (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                val pageList = sortListPage(currentPage, LinkedList(preventiveData.keys))
                var pageId = 0
                logListInt("Before deleted page list: ", pageList)
                while (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    val page = pageList[pageId++]
                    (preventiveData[page] as LinkedList).clear()
                    preventiveData.remove(page)
                }
            }
            logListInt("Final page list: ", LinkedList(preventiveData.keys))

            main.launch {
                view.refreshRecentBookList()
                if (newPage) {
                    view.hideLoading()
                }
            }
        }
    }

    private suspend fun loadPreventiveData() {
        isLoadingPreventiveData.compareAndSet(false, true)
        suspendCoroutine<Boolean> { continuation ->
            runBlocking {
                for (page in currentPage + 1..NUMBER_OF_PREVENTIVE_PAGES) {
                    Logger.d(TAG, "Start loading page $page")
                    preventiveData[page] = getRecentBook(page - 1)
                }
            }
            continuation.resume(true)
        }
        Logger.d(TAG, "Load preventive data successfully")
        isLoadingPreventiveData.compareAndSet(true, false)
    }

    private suspend fun getRecentBook(pageNumber: Int): LinkedList<Book> {
        val bookList = LinkedList<Book>()
        // Todo: Remove this try/catch
        try {
            val recentList = if (type == Constants.RECENT) {
                bookRepository.getRecentBooks(MAX_PER_PAGE, pageNumber * MAX_PER_PAGE)
            } else {
                bookRepository.getFavoriteBook(MAX_PER_PAGE, pageNumber * MAX_PER_PAGE)
            }
            for (recent in recentList) {
                val bookResponse = bookRepository.getBookDetails(recent.bookId)
                if (bookResponse is BookResponse.Success) {
                    bookList.add(bookResponse.book)
                }
            }
        } catch (throwable: Throwable) {
            analyticsErrorLogUseCase.execute(throwable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
                .addTo(compositeDisposable)
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
                if (abs(pageList[i] - anchor) < abs(pageList[j] - anchor)) {
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
