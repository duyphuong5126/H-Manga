package nhdphuong.com.manga.features.reader

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.support.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.supports.SupportUtils
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import kotlin.collections.HashSet

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderPresenter @Inject constructor(private val mView: ReaderContract.View,
                                          private val mBook: Book,
                                          private val mStartReadingPage: Int,
                                          private val mContext: Context,
                                          private val mBookRepository: BookRepository,
                                          @IO private val io: CoroutineScope,
                                          @Main private val main: CoroutineScope) : ReaderContract.Presenter {
    companion object {
        private const val TAG = "ReaderPresenter"
        private const val PREFETCH_RADIUS = 5
    }

    private lateinit var mBookPages: LinkedList<String>
    private var mCurrentPage: Int = -1
    private val mDownloadQueue = LinkedBlockingQueue<Int>()
    private var isDownloading = false
    private val mPreFetchedPages = HashSet<Int>()
    private var mNotificationId: Int = -1

    private val mPrefixNumber: Int
        get() {
            var totalPages = mBook.numOfPages
            var prefixCount = 1
            while (totalPages / 10 > 0) {
                totalPages /= 10
                prefixCount++
            }
            return prefixCount
        }

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        Logger.d(TAG, "Start reading: ${mBook.previewTitle} from $mStartReadingPage")
        saveRecentBook()
        mPreFetchedPages.clear()

        mView.showBookTitle(mBook.previewTitle)
        mDownloadQueue.clear()

        mBookPages = LinkedList()
        for (pageId in 0 until mBook.bookImages.pages.size) {
            val page = mBook.bookImages.pages[pageId]
            mBookPages.add(ApiConstants.getPictureUrl(mBook.mediaId, pageId + 1, page.imageType))
        }
        if (!mBookPages.isEmpty()) {
            mCurrentPage = 0
            mView.showBookPages(mBookPages)
            mView.showPageIndicator(String.format(mContext.getString(R.string.bottom_reader), mCurrentPage + 1, mBookPages.size))
        }
        if (mStartReadingPage >= 0) {
            mView.jumpToPage(mStartReadingPage)
        }

        preloadPagesAround(mStartReadingPage)

        //mView.pushNowReadingNotification(mBook.previewTitle, mStartReadingPage + 1, mBookPages.size)
    }

    override fun updatePageIndicator(page: Int) {
        Logger.d(TAG, "Current page: $page")
        mCurrentPage = page
        mBookPages.size.let { pageCount ->
            val pageString = if (page == pageCount - 1) {
                mContext.getString(R.string.back_to_gallery)
            } else {
                String.format(mContext.getString(R.string.bottom_reader), page + 1, pageCount)
            }
            mView.showPageIndicator(pageString)
        }

        preloadPagesAround(page)
    }

    override fun backToGallery() {
        if (mCurrentPage == mBookPages.size - 1) {
            mView.navigateToGallery()
        }
    }

    override fun downloadCurrentPage() {
        NHentaiApp.instance.let { nHentaiApp ->
            if (!nHentaiApp.isStoragePermissionAccepted) {
                mView.showRequestStoragePermission()
                return@let
            }
            mDownloadQueue.add(mCurrentPage)
            if (!isDownloading) {
                isDownloading = true
                mView.showLoading()
                io.launch {
                    val resultList = LinkedList<String>()
                    while (!mDownloadQueue.isEmpty()) {
                        val downloadPage = mDownloadQueue.take()
                        mBook.bookImages.pages[downloadPage].let { page ->
                            val result = ImageUtils.downloadImage(mContext, mBookPages[downloadPage], page.width, page.height)

                            val resultFilePath = nHentaiApp.getImageDirectory(mBook.mediaId)

                            val format = if (page.imageType == Constants.PNG_TYPE) {
                                Bitmap.CompressFormat.PNG
                            } else {
                                Bitmap.CompressFormat.JPEG
                            }
                            val fileName = String.format("%0${mPrefixNumber}d", downloadPage + 1)
                            val resultPath = SupportUtils.compressBitmap(result, resultFilePath, fileName, format)
                            resultList.add(resultPath)
                            Logger.d(TAG, "$fileName is saved successfully")
                        }
                        main.launch {
                            mView.updateDownloadPopupTitle(String.format(mContext.getString(R.string.download_progress), downloadPage + 1))
                            mView.showDownloadPopup()
                        }
                        Logger.d(TAG, "Download page ${downloadPage + 1} completed")
                    }
                    nHentaiApp.refreshGallery(*resultList.toTypedArray())
                    isDownloading = false
                    main.launch {
                        mView.hideLoading()
                        val handler = Handler()
                        handler.postDelayed({
                            mView.hideDownloadPopup()
                        }, 3000)
                    }
                    Logger.d(TAG, "All pages downloaded")
                }
            }
        }
    }

    override fun reloadCurrentPage(onForceReload: (Int) -> Unit) {
        onForceReload(mCurrentPage)
    }

    override fun updateNotificationId(notificationId: Int) {
        mNotificationId = notificationId
    }

    override fun stop() {
        Logger.d(TAG, "End reading: ${mBook.previewTitle}")
        isDownloading = false
        /*if (mNotificationId != -1) {
            mView.removeNotification(mNotificationId)
            mNotificationId = -1
        }*/
    }

    private fun saveRecentBook() {
        io.launch {
            if (!mBookRepository.isFavoriteBook(mBook.bookId)) {
                mBookRepository.saveRecentBook(mBook.bookId)
            }
        }
    }

    @MainThread
    private fun preloadPagesAround(page: Int) {
        val startPrefetch = Math.max(0, page - PREFETCH_RADIUS)
        val endPrefetch = Math.min(mBookPages.size - 1, page + PREFETCH_RADIUS)
        Logger.d(TAG, "Prefetch from $startPrefetch to $endPrefetch")
        for (i in startPrefetch..endPrefetch) {
            if (!mPreFetchedPages.contains(i)) {
                Logger.d(TAG, "Pre-load page $i")
                mBook.bookImages.pages[i].run {
                    ImageUtils.downloadImage(mContext, mBookPages[i]) { bitmap ->
                        Logger.d(TAG, "Pre-fetched bitmap $i will be recycled")
                        bitmap?.recycle()
                        mPreFetchedPages.add(i)
                    }
                }
            }
        }
    }
}