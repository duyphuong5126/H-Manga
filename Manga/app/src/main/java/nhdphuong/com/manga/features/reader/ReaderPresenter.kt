package nhdphuong.com.manga.features.reader

import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.IFileUtils
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.usecase.GetDownloadedBookPagesUseCase
import java.util.LinkedList
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Constants.Companion.EVENT_READ_BOOK_HORIZONTALLY
import nhdphuong.com.manga.Constants.Companion.EVENT_READ_BOOK_VERTICALLY
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_ANALYTICS_BOOK_ID
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.analytics.AnalyticsParam
import nhdphuong.com.manga.supports.doInIOContext
import nhdphuong.com.manga.usecase.LogAnalyticsEventUseCase
import nhdphuong.com.manga.usecase.SaveLastVisitedPageUseCase
import nhdphuong.com.manga.views.uimodel.ReaderType
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.io.File

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderPresenter @Inject constructor(
    private val view: ReaderContract.View,
    private val book: Book,
    private val startReadingPage: Int,
    private val getDownloadedBookPagesUseCase: GetDownloadedBookPagesUseCase,
    private val saveLastVisitedPageUseCase: SaveLastVisitedPageUseCase,
    private val logAnalyticsEventUseCase: LogAnalyticsEventUseCase,
    private val bookRepository: BookRepository,
    private val preferencesManager: SharedPreferencesManager,
    private val fileUtils: IFileUtils,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : ReaderContract.Presenter {
    private val logger = Logger("ReaderPresenter")

    private lateinit var bookPages: ArrayList<String>
    private var currentPage: Int = -1
    private val downloadQueue = LinkedBlockingQueue<Int>()
    private var isDownloading = false
    private var notificationId: Int = -1

    private var viewMode: ReaderType = preferencesManager.currentReaderType
        set(value) {
            field = value
            preferencesManager.currentReaderType = value
        }

    private val prefixNumber: Int
        get() {
            var totalPages = book.numOfPages
            var prefixCount = 1
            while (totalPages / 10 > 0) {
                totalPages /= 10
                prefixCount++
            }
            return prefixCount
        }

    private var viewDownloadedData: Boolean = false

    private val lastVisitedPageQueue = CircularFifoQueue<Int>(LAST_VISITED_PAGE_LIMIT)

    private val lastVisitedPage: Int
        get() = if (lastVisitedPageQueue.isNotEmpty()) {
            lastVisitedPageQueue.get(lastVisitedPageQueue.size - 1)
        } else throw RuntimeException("Last visited page is not available")

    private val unBoundCompositeDisposable = CompositeDisposable()

    private val compositeDisposable = CompositeDisposable()

    init {
        view.setPresenter(this)
    }

    override fun enableViewDownloadedDataMode() {
        viewDownloadedData = true
    }

    override fun start() {
        logger.d("Start reading: ${book.previewTitle} from $startReadingPage")
        view.showBookTitle(book.previewTitle)
        logCurrentReaderMode()
        bookPages = ArrayList()
        if (viewDownloadedData) {
            getDownloadedBookPagesUseCase.execute(book.bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { downloadedImages ->
                    logger.d("Book ${book.bookId}: ${downloadedImages.size} page(s)")
                    bookPages.addAll(downloadedImages)
                    setUpReader()
                }, onError = {
                    logger.e("Failed to get pages of book ${book.bookId}: $it")
                }).addTo(compositeDisposable)
        } else {
            saveRecentBook()
            for (pageId in book.bookImages.pages.indices) {
                val page = book.bookImages.pages[pageId]
                bookPages.add(
                    ApiConstants.getPictureUrl(book.mediaId, pageId + 1, page.imageType)
                )
            }
            setUpReader()
        }
    }

    override fun toggleViewMode() {
        viewMode = if (viewMode == ReaderType.VerticalScroll) {
            ReaderType.HorizontalPage
        } else {
            ReaderType.VerticalScroll
        }
        logCurrentReaderMode()
        view.showBookPages(bookPages, viewMode, currentPage)
        view.showPageIndicator(currentPage + 1, bookPages.size)
    }

    override fun updatePageIndicator(page: Int) {
        logger.d("Current page: $page")
        currentPage = page
        bookPages.size.let { pageCount ->
            if (page == pageCount - 1) {
                view.showBackToGallery()
            } else {
                view.showPageIndicator(page + 1, pageCount)
            }
        }
        lastVisitedPageQueue.add(page)
        logger.d("Last $LAST_VISITED_PAGE_LIMIT visited pages: $lastVisitedPageQueue")
    }

    override fun forceBackToGallery() {
        view.navigateToGallery(lastVisitedPage)
    }

    override fun backToGallery() {
        if (currentPage == bookPages.size - 1) {
            view.navigateToGallery(lastVisitedPage)
        }
    }

    override fun downloadCurrentPage() {
        downloadQueue.add(currentPage)
        if (!isDownloading) {
            isDownloading = true
            view.showLoading()
            io.doInIOContext {
                val resultList = LinkedList<String>()
                while (!downloadQueue.isEmpty()) {
                    val downloadPage = downloadQueue.take()
                    book.bookImages.pages[downloadPage].let { page ->
                        val result = ImageUtils.downloadImage(bookPages[downloadPage])

                        val resultFilePath = fileUtils.getImageDirectory(
                            book.usefulName.replace(
                                File.separator, "_"
                            )
                        )

                        val fileName = String.format("%0${prefixNumber}d", downloadPage + 1)
                        val resultPath =
                            SupportUtils.saveImage(
                                result,
                                resultFilePath,
                                fileName,
                                page.imageType
                            )
                        resultList.add(resultPath)
                        logger.d("$fileName is saved successfully")
                    }
                    main.launch {
                        if (view.isActive()) {
                            view.updateDownloadPopupTitle(downloadPage + 1)
                            view.showDownloadPopup()
                        }
                    }
                    logger.d("Download page ${downloadPage + 1} completed")
                }
                fileUtils.refreshGallery(false, *resultList.toTypedArray())
                isDownloading = false
                main.launch {
                    if (view.isActive()) {
                        view.hideLoading()
                        view.hideDownloadPopup()
                    }
                }
                logger.d("All pages downloaded")
            }
        }
    }

    override fun reloadCurrentPage(onForceReload: (Int) -> Unit) {
        onForceReload(currentPage)
    }

    override fun updateNotificationId(notificationId: Int) {
        this.notificationId = notificationId
    }

    override fun endReading() {
        if (notificationId != -1) {
            view.removeNotification(notificationId)
            notificationId = -1
        }
    }

    override fun generateSharableLink() {
        val pageId = currentPage
        if (pageId in book.bookImages.pages.indices) {
            view.processSharingCurrentPage(
                book.bookId,
                book.title.pretty,
                ApiConstants.getSharablePageUrl(book.bookId, pageId + 1)
            )
        }
    }

    override fun stop() {
        logger.d("End reading: ${book.previewTitle}")
        isDownloading = false
        compositeDisposable.clear()
        if (lastVisitedPageQueue.isNotEmpty()) {
            val lastVisitedPage = lastVisitedPage
            logger.d("Saving last visited page $lastVisitedPage")
            saveLastVisitedPageUseCase.execute(book.bookId, lastVisitedPage)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    logger.d("Done saving last page $lastVisitedPage")
                }, {
                    logger.e("Failed to save last page $lastVisitedPage with error: $it")
                }).addTo(unBoundCompositeDisposable)
        }
    }

    private fun setUpReader() {
        currentPage = if (startReadingPage >= 0) startReadingPage else 0

        if (bookPages.isNotEmpty()) {
            view.showBookPages(bookPages, viewMode, currentPage)
        }
        lastVisitedPageQueue.add(currentPage)

        view.pushNowReadingNotification(
            book.previewTitle,
            startReadingPage + 1,
            bookPages.size
        )
    }

    private fun saveRecentBook() {
        io.launch {
            bookRepository.saveRecentBook(book)
        }
    }

    private fun logCurrentReaderMode() {
        val eventName = if (viewMode == ReaderType.HorizontalPage) {
            EVENT_READ_BOOK_HORIZONTALLY
        } else {
            EVENT_READ_BOOK_VERTICALLY
        }

        val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, book.bookId)
        logAnalyticsEventUseCase.execute(eventName, bookIdParam)
            .subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(compositeDisposable)
    }

    companion object {
        private const val LAST_VISITED_PAGE_LIMIT = 5
    }
}
