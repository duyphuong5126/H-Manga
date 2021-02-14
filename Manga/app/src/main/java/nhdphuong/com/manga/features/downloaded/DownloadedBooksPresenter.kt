package nhdphuong.com.manga.features.downloaded

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.usecase.GetAllDownloadedBooksUseCase
import nhdphuong.com.manga.usecase.GetAvailableBookThumbnailsUseCase
import java.util.ArrayList
import java.util.Locale
import javax.inject.Inject

class DownloadedBooksPresenter @Inject constructor(
    private val view: DownloadedBooksContract.View,
    private val mBookRepository: BookRepository,
    private val getAllDownloadedBooksUseCase: GetAllDownloadedBooksUseCase,
    private val getAvailableBookThumbnailsUseCase: GetAvailableBookThumbnailsUseCase,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : DownloadedBooksContract.Presenter {
    private val compositeDisposable = CompositeDisposable()

    private val totalBookList = mutableListOf<Book>()
    private val currentBookList = mutableListOf<Book>()
    private val bookIdList = mutableListOf<String>()
    private val downloadedThumbnails = ArrayList<Pair<String, String>>()
    private var totalPages = 1
    private var currentPage: Int = 0

    private val logger: Logger by lazy {
        Logger("DownloadedBooksPresenter")
    }

    override fun start() {
        totalBookList.clear()
        currentBookList.clear()
        downloadedThumbnails.clear()
        view.setUpBookList(currentBookList)

        getAllDownloadedBooksUseCase.execute()
            .flatMap { downloadedBooks ->
                getAvailableBookThumbnailsUseCase.execute(downloadedBooks.map { it.bookId })
                    .doOnSuccess { availableThumbnails ->
                        downloadedThumbnails.addAll(availableThumbnails)
                    }
                    .map { downloadedBooks }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                view.showLoading()
            }
            .doAfterTerminate {
                view.hideLoading()
            }
            .subscribeBy(onSuccess = { downloadedBooks ->
                logger.d("Downloaded books: ${downloadedBooks.size}")
                val bookCount = downloadedBooks.size
                totalPages = bookCount / MAX_PER_PAGE
                if (bookCount % MAX_PER_PAGE > 0) {
                    totalPages++
                }
                currentPage = 0
                if (bookCount == 0) {
                    view.showNothingView()
                }
                totalBookList.addAll(downloadedBooks)
                updateCurrentPage()
                view.refreshRecentPagination(totalPages)
                view.showLastBookListRefreshTime(
                    SupportUtils.getTimeElapsed(0).toLowerCase(Locale.US)
                )
            }, onError = { error ->
                logger.e("Failed to get downloaded books with error: $error")
            }).addTo(compositeDisposable)
    }

    override fun reloadLastBookListRefreshTime() {
        view.showLastBookListRefreshTime(
            SupportUtils.getTimeElapsed(0).toLowerCase(Locale.US)
        )
    }

    override fun reloadBookMarkers() {
        io.launch {
            val recentList = mutableListOf<String>()
            val favoriteList = mutableListOf<String>()
            bookIdList.forEach { bookId ->
                when {
                    mBookRepository.isFavoriteBook(bookId) -> favoriteList.add(bookId)
                    mBookRepository.isRecentBook(bookId) -> recentList.add(bookId)
                }
            }
            main.launch {
                view.showRecentBooks(recentList)
                view.showFavoriteBooks(favoriteList)
            }
        }
    }

    override fun reloadBookThumbnails() {
        val fromIndex = currentPage * MAX_PER_PAGE
        val toIndex = if ((currentPage + 1) * MAX_PER_PAGE < totalBookList.size) {
            (currentPage + 1) * MAX_PER_PAGE
        } else totalBookList.size - 1
        if (fromIndex == toIndex) {
            view.refreshThumbnailList(listOf(downloadedThumbnails[toIndex]))
        } else {
            view.refreshThumbnailList(downloadedThumbnails.subList(fromIndex, toIndex))
        }
    }

    override fun jumToFirstPage() {
        currentPage = 0
        updateCurrentPage()
    }

    override fun jumpToPage(pageNumber: Int) {
        currentPage = pageNumber - 1
        updateCurrentPage()
    }

    override fun jumToLastPage() {
        currentPage = totalPages - 1
        updateCurrentPage()
    }

    override fun notifyBookRemoved(bookId: String) {
        totalBookList.removeAll { it.bookId == bookId }
        updateCurrentPage()
    }

    override fun stop() {
        compositeDisposable.clear()
    }

    private fun updateCurrentPage() {
        io.launch {
            currentBookList.clear()
            if (totalBookList.isEmpty()) {
                main.launch {
                    view.refreshBookList()
                }
                return@launch
            }
            val fromIndex = currentPage * MAX_PER_PAGE
            val toIndex = if ((currentPage + 1) * MAX_PER_PAGE < totalBookList.size) {
                (currentPage + 1) * MAX_PER_PAGE
            } else totalBookList.size
            if (fromIndex == toIndex) {
                currentBookList.add(totalBookList[toIndex])
            } else {
                currentBookList.addAll(totalBookList.subList(fromIndex, toIndex))
            }
            bookIdList.clear()
            bookIdList.addAll(currentBookList.map { it.bookId })
            main.launch {
                view.refreshBookList()
            }
        }
    }

    companion object {
        private const val MAX_PER_PAGE = 25
    }
}
