package nhdphuong.com.manga.features.reader

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.supports.GlideUtils
import nhdphuong.com.manga.supports.SupportUtils
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderPresenter @Inject constructor(private val mView: ReaderContract.View,
                                          private val mBook: Book,
                                          private val mStartReadingPage: Int,
                                          private val mContext: Context,
                                          private val mBookRepository: BookRepository) : ReaderContract.Presenter {
    companion object {
        private val TAG = ReaderPresenter::class.java.simpleName
    }

    private lateinit var mBookPages: LinkedList<String>
    private var mCurrentPage: Int = -1
    private val mDownloadQueue = LinkedBlockingQueue<Int>()
    private var isDownloading = false

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
        Log.d(TAG, "Start reading: ${mBook.previewTitle}")
        saveRecentBook()

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

        if (mStartReadingPage != 0) {
            launch {
                delay(1000)
                launch(UI) {
                    mView.jumpToPage(mStartReadingPage)
                }
            }
        }
    }

    override fun updatePageIndicator(page: Int) {
        mCurrentPage = page
        mBookPages.size.let { pageCount ->
            val pageString = if (page == pageCount - 1) {
                mContext.getString(R.string.back_to_gallery)
            } else {
                String.format(mContext.getString(R.string.bottom_reader), page + 1, pageCount)
            }
            mView.showPageIndicator(pageString)
        }
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
                launch {
                    val resultList = LinkedList<String>()
                    while (!mDownloadQueue.isEmpty()) {
                        val downloadPage = mDownloadQueue.take()
                        mBook.bookImages.pages[downloadPage].let { page ->
                            val result = GlideUtils.downloadImage(mContext, mBookPages[downloadPage], page.width, page.height)

                            val resultFilePath = nHentaiApp.getImageDirectory(mBook.mediaId)

                            val format = if (page.imageType == Constants.PNG_TYPE) {
                                Bitmap.CompressFormat.PNG
                            } else {
                                Bitmap.CompressFormat.JPEG
                            }
                            val fileName = String.format("%0${mPrefixNumber}d", downloadPage + 1)
                            val resultPath = SupportUtils.compressBitmap(result, resultFilePath, fileName, format)
                            resultList.add(resultPath)
                            Log.d(TAG, "$fileName is saved successfully")
                        }
                        launch(UI) {
                            mView.updateDownloadPopupTitle(String.format(mContext.getString(R.string.download_progress), downloadPage + 1))
                            mView.showDownloadPopup()
                        }
                        Log.d(TAG, "Download page ${downloadPage + 1} completed")
                    }
                    nHentaiApp.refreshGallery(*resultList.toTypedArray())
                    isDownloading = false
                    launch(UI) {
                        mView.hideLoading()
                        val handler = Handler()
                        handler.postDelayed({
                            mView.hideDownloadPopup()
                        }, 3000)
                    }
                    Log.d(TAG, "All pages downloaded")
                }
            }
        }
    }

    override fun reloadCurrentPage(onForceReload: (Int) -> Unit) {
        onForceReload(mCurrentPage)
    }

    override fun stop() {
        isDownloading = false
    }

    private fun saveRecentBook() {
        launch {
            if (!mBookRepository.isFavoriteBook(mBook.bookId)) {
                mBookRepository.saveRecentBook(mBook.bookId)
            }
        }
    }
}