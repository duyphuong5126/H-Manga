package nhdphuong.com.manga.features.recent

import android.annotation.SuppressLint
import com.google.gson.JsonParseException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Constants.Companion.EVENT_BROWSE_FAVORITE
import nhdphuong.com.manga.Constants.Companion.EVENT_BROWSE_RECENT
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.FavoriteBook
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.enum.ErrorEnum
import nhdphuong.com.manga.extension.isNetworkError
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.usecase.LogAnalyticsErrorUseCase
import nhdphuong.com.manga.usecase.LogAnalyticsEventUseCase
import java.net.SocketTimeoutException
import java.util.Collections
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.min

/*
 * Created by nhdphuong on 6/10/18.
 */
class RecentPresenter @Inject constructor(
    private val view: RecentContract.View,
    private val bookRepository: BookRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val logAnalyticsErrorUseCase: LogAnalyticsErrorUseCase,
    private val logAnalyticsEventUseCase: LogAnalyticsEventUseCase,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : RecentContract.Presenter {
    companion object {
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
    private val recentBookList = ArrayList<Book>()

    @SuppressLint("UseSparseArrays")
    private var preventiveData = HashMap<Int, ArrayList<Book>>()
    private var isLoadingPreventiveData = AtomicBoolean(false)
    private val jobList = CopyOnWriteArrayList<Job>()


    private val remoteBookErrorSubject = PublishSubject.create<Throwable>()

    private val compositeDisposable = CompositeDisposable()

    private val logger = Logger("RecentPresenter")

    init {
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
        recentBookList.clear()
        view.showLoading()
        view.setUpRecentBookList(recentBookList)
        io.launch {
            preventiveData[currentPage] = getRecentBook(currentPage - 1)
            recentBookList.addAll(preventiveData[currentPage].orEmpty())
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
                        if (currentPageCount == 0 || recentBookList.isEmpty()) {
                            view.showNothingView(type)
                        } else {
                            view.refreshRecentPagination(currentPageCount)
                        }
                        view.hideLoading()
                    }
                }

                loadPreventiveData()
            }
            jobList.add(job)
        }
        val eventName = if (type == Constants.RECENT) EVENT_BROWSE_RECENT else EVENT_BROWSE_FAVORITE
        logAnalyticsEventUseCase.execute(eventName)
            .subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(compositeDisposable)
    }

    override fun setType(recentType: String) {
        type = recentType
    }

    override fun reloadRecentMarks() {
        val bookList = ArrayList<String>()
        io.launch {
            if (type == Constants.RECENT) {
                recentBookList.filter { bookRepository.isRecentBook(it.bookId) }
                    .map(Book::bookId)
                    .let(bookList::addAll)

                main.launch {
                    view.showRecentBooks(bookList)
                }
            } else {
                recentBookList.filter { bookRepository.isFavoriteBook(it.bookId) }
                    .map(Book::bookId)
                    .let(bookList::addAll)

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
            while (jobList.size > 0) {
                val job = jobList.removeAt(0)
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
            val currentList: List<Book> = if (preventiveData.containsKey(currentPage)) {
                preventiveData[currentPage].orEmpty()
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
                val pageList = sortListPage(currentPage, ArrayList(preventiveData.keys))
                var pageId = 0
                logListInt("Before deleted page list: ", pageList)
                while (preventiveData.size > NUMBER_OF_PREVENTIVE_PAGES) {
                    val page = pageList[pageId++]
                    (preventiveData[page] as ArrayList).clear()
                    preventiveData.remove(page)
                }
            }
            logListInt("Final page list: ", ArrayList(preventiveData.keys))

            main.launch {
                view.refreshRecentBookList()
                if (newPage) {
                    view.hideLoading()
                }
            }
        }
    }

    private suspend fun loadPreventiveData() {
        if (currentPageCount <= currentPage) {
            logger.d("No more preventive page left")
            return
        }
        val toLoadPages = min(currentPageCount, NUMBER_OF_PREVENTIVE_PAGES)
        isLoadingPreventiveData.compareAndSet(false, true)
        suspendCoroutine<Boolean> { continuation ->
            runBlocking {
                for (page in currentPage + 1..toLoadPages) {
                    logger.d("Start loading page $page")
                    preventiveData[page] = getRecentBook(page - 1)
                }
            }
            continuation.resume(true)
        }
        logger.d("Load preventive data successfully")
        isLoadingPreventiveData.compareAndSet(true, false)
    }

    private suspend fun getRecentBook(pageNumber: Int): ArrayList<Book> {
        // Todo: Remove this try/catch
        try {
            val resultList = ArrayList<Book>()
            if (type == Constants.RECENT) {
                bookRepository.getRecentBooks(MAX_PER_PAGE, pageNumber * MAX_PER_PAGE)
                    .mapNotNull(RecentBook::rawBook)
            } else {
                bookRepository.getFavoriteBooks(MAX_PER_PAGE, pageNumber * MAX_PER_PAGE)
                    .mapNotNull(FavoriteBook::rawBook)
            }.let(resultList::addAll)
            return resultList
        } catch (throwable: Throwable) {
            remoteBookErrorSubject.onNext(throwable)
            logAnalyticsErrorUseCase.execute(throwable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
                .addTo(compositeDisposable)
        }
        return ArrayList()
    }

    private fun sortListPage(anchor: Int, pageList: ArrayList<Int>): ArrayList<Int> {
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
        logger.d(message)
    }
}
