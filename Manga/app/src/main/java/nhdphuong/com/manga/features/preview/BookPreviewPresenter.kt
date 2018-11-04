package nhdphuong.com.manga.features.preview

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import nhdphuong.com.manga.*
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.features.reader.ReaderActivity
import nhdphuong.com.manga.supports.SupportUtils
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import kotlin.collections.ArrayList

/*
 * Created by nhdphuong on 4/14/18.
 */
class BookPreviewPresenter @Inject constructor(private val mView: BookPreviewContract.View,
                                               private val mBook: Book,
                                               private val mContext: Context,
                                               private val mBookRepository: BookRepository) : BookPreviewContract.Presenter, DownloadManager.DownloadCallback {

    companion object {
        private const val TAG = "BookPreviewPresenter"
        private const val MILLISECOND: Long = 1000

        private const val BATCH_COUNT = 5
        private const val THUMBNAILS_LIMIT = 30
    }

    private var isTagListInitialized = false
    private var isLanguageListInitialized = false
    private var isArtistListInitialized = false
    private var isCategoryListInitialized = false
    private var isCharacterListInitialized = false
    private var isParodyListInitialized = false
    private var isGroupListInitialized = false
    private var isInfoLoaded = false
    private var isBookCoverReloaded = false
    private lateinit var mCacheCoverUrl: String

    private lateinit var mTagList: LinkedList<Tag>
    private lateinit var mArtistList: LinkedList<Tag>
    private lateinit var mCategoryList: LinkedList<Tag>
    private lateinit var mLanguageList: LinkedList<Tag>
    private lateinit var mParodyList: LinkedList<Tag>
    private lateinit var mCharacterList: LinkedList<Tag>
    private lateinit var mGroupList: LinkedList<Tag>

    private val mBookThumbnailList = ArrayList<String>()

    private var mCurrentThumbnailPosition = 0

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

    private val uploadedTimeStamp: String = SupportUtils.getTimeElapsed(System.currentTimeMillis() - mBook.updateAt * MILLISECOND)

    private var isFavoriteBook: Boolean = false

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        mBookThumbnailList.clear()
        mCurrentThumbnailPosition = 0
        if (!this::mCacheCoverUrl.isInitialized) {
            mView.showBookCoverImage(ApiConstants.getBookCover(mBook.mediaId))
        } else {
            mView.showBookCoverImage(mCacheCoverUrl)
        }
        refreshBookFavorite()
        mView.showFavoriteBookSaved(isFavoriteBook)
        mView.show1stTitle(mBook.title.englishName)
        mView.show2ndTitle(mBook.title.japaneseName)
        mView.showUploadedTime(String.format(mContext.getString(R.string.uploaded), uploadedTimeStamp))
        mView.showPageCount(String.format(mContext.getString(R.string.page_count), mBook.numOfPages))
        mTagList = LinkedList()
        mCategoryList = LinkedList()
        mArtistList = LinkedList()
        mCharacterList = LinkedList()
        mLanguageList = LinkedList()
        mParodyList = LinkedList()
        mGroupList = LinkedList()
        if (DownloadManager.isDownloading && DownloadManager.bookId == mBook.bookId) {
            DownloadManager.setDownloadCallback(this)
            mView.updateDownloadProgress(DownloadManager.progress, DownloadManager.total)
        }
    }

    override fun loadInfoLists() {
        if (!isInfoLoaded) {
            for (tag in mBook.tags) {
                when (tag.type) {
                    Constants.TAG -> mTagList.add(tag)
                    Constants.CATEGORY -> mCategoryList.add(tag)
                    Constants.CHARACTER -> mCharacterList.add(tag)
                    Constants.ARTIST -> mArtistList.add(tag)
                    Constants.LANGUAGE -> mLanguageList.add(tag)
                    Constants.PARODY -> mParodyList.add(tag)
                    Constants.GROUP -> mGroupList.add(tag)
                }
            }

            if (!isTagListInitialized) {
                if (mTagList.isEmpty()) {
                    mView.hideTagList()
                } else {
                    mView.showTagList(mTagList)
                }
                isTagListInitialized = true
            }
            if (!isArtistListInitialized) {
                if (mArtistList.isEmpty()) {
                    mView.hideArtistList()
                } else {
                    mView.showArtistList(mArtistList)
                }
                isArtistListInitialized = true
            }
            if (!isLanguageListInitialized) {
                if (mLanguageList.isEmpty()) {
                    mView.hideLanguageList()
                } else {
                    mView.showLanguageList(mLanguageList)
                }
                isLanguageListInitialized = true
            }
            if (!isCategoryListInitialized) {
                if (mCategoryList.isEmpty()) {
                    mView.hideCategoryList()
                } else {
                    mView.showCategoryList(mCategoryList)
                }
                isCategoryListInitialized = true
            }
            if (!isCharacterListInitialized) {
                if (mCharacterList.isEmpty()) {
                    mView.hideCharacterList()
                } else {
                    mView.showCharacterList(mCharacterList)
                }
                isCharacterListInitialized = true
            }
            if (!isGroupListInitialized) {
                if (mGroupList.isEmpty()) {
                    mView.hideGroupList()
                } else {
                    mView.showGroupList(mGroupList)
                }
                isGroupListInitialized = true
            }
            if (!isParodyListInitialized) {
                if (mParodyList.isEmpty()) {
                    mView.hideParodyList()
                } else {
                    mView.showParodyList(mParodyList)
                }
                isParodyListInitialized = true
            }

            loadBookThumbnails()
            mView.showBookThumbnailList(mBookThumbnailList)

            loadRecommendBook()
        }
    }

    override fun reloadCoverImage() {
        if (!isBookCoverReloaded) {
            isBookCoverReloaded = true
            launch {
                val coverUrl = withContext(DefaultDispatcher) {
                    getReachableBookCover()
                }
                launch(UI) {
                    mView.showBookCoverImage(coverUrl)
                }
            }
        }
    }

    override fun saveCurrentAvailableCoverUrl(url: String) {
        Logger.d(TAG, "Current available url: $url")
        mCacheCoverUrl = url
    }

    override fun startReadingFrom(startReadingPage: Int) {
        ReaderActivity.start(mContext, startReadingPage, mBook)
    }

    override fun downloadBook() {
        NHentaiApp.instance.let { nHentaiApp ->
            if (!nHentaiApp.isStoragePermissionAccepted) {
                mView.showRequestStoragePermission()
                return@let
            }

            if (!DownloadManager.isDownloading) {
                val bookPages = LinkedList<String>()
                for (pageId in 0 until mBook.bookImages.pages.size) {
                    val page = mBook.bookImages.pages[pageId]
                    bookPages.add(ApiConstants.getPictureUrl(mBook.mediaId, pageId + 1, page.imageType))
                }
                bookPages.size.let { total ->
                    if (total > 0) {
                        DownloadManager.setDownloadCallback(this)
                        DownloadManager.startDownloading(mBook.bookId, total)
                        launch {
                            var progress = 0
                            val resultList = LinkedList<String>()
                            var currentPage = 0
                            while (currentPage < total) {
                                val lastPage = if (currentPage + BATCH_COUNT <= total) currentPage + BATCH_COUNT else total
                                runBlocking {
                                    val countDownLatch = CountDownLatch(lastPage - currentPage)
                                    for (downloadPage in currentPage until lastPage) {
                                        launch {
                                            try {
                                                mBook.bookImages.pages[downloadPage].let { page ->
                                                    val result = SupportUtils.downloadImageBitmap(bookPages[downloadPage], false)!!

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
                                                    countDownLatch.countDown()
                                                }
                                                launch(UI) {
                                                    progress++
                                                    DownloadManager.updateProgress(mBook.bookId, progress)
                                                }
                                                Logger.d(TAG, "Download page ${downloadPage + 1} completed")
                                            } catch (exception: Exception) {
                                                countDownLatch.countDown()
                                            }
                                        }
                                    }
                                    countDownLatch.await()
                                }
                                currentPage += BATCH_COUNT
                            }
                            delay(1000)
                            launch(UI) {
                                nHentaiApp.refreshGallery(*resultList.toTypedArray())
                                DownloadManager.endDownloading(progress, total)
                            }
                        }
                    }
                }
            } else {
                if (DownloadManager.bookId == mBook.bookId) {
                    mView.showThisBookBeingDownloaded()
                } else {
                    mView.showBookBeingDownloaded(DownloadManager.bookId)
                }
            }
        }
    }

    override fun restartBookPreview(bookId: String) {
        launch {
            val bookDetails = mBookRepository.getBookDetails(bookId)
            bookDetails?.let {
                launch(UI) {
                    BookPreviewActivity.restart(bookDetails)
                }
            }
        }
    }

    override fun changeBookFavorite() {
        launch {
            mBookRepository.saveFavoriteBook(mBook.bookId, !isFavoriteBook)
            refreshBookFavorite()
            launch(UI) {
                mView.showFavoriteBookSaved(isFavoriteBook)
            }
        }
    }

    override fun stop() {

    }

    override fun onDownloadingStarted(bookId: String, total: Int) {
        if (mView.isActive()) {
            mView.initDownloading(total)
        }
    }

    override fun onProgressUpdated(bookId: String, progress: Int, total: Int) {
        if (mView.isActive()) {
            mView.updateDownloadProgress(progress, total)
        }
    }

    override fun onDownloadingEnded(downloaded: Int, total: Int) {
        if (mView.isActive()) {
            if (downloaded == total) {
                mView.finishDownloading()
            } else {
                mView.finishDownloading(total - downloaded, total)
            }
        }
    }

    override fun loadMoreThumbnails() {
        if (mBookThumbnailList.size < mBook.numOfPages) {
            loadBookThumbnails()
            mView.updateBookThumbnailList()
        } else {
            Logger.d(TAG, "End of thumbnails list")
        }
    }

    private fun getReachableBookCover(): String {
        val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val mediaId = mBook.mediaId
        val coverUrl = ApiConstants.getBookCover(mediaId)
        if (networkInfo != null && networkInfo.isConnected) {
            var isReachable = false
            val bookPages = mBook.bookImages.pages
            var currentPage = 0
            var url = ApiConstants.getPictureUrl(mediaId, currentPage, bookPages[currentPage].imageType)
            while (!isReachable && currentPage < bookPages.size) {
                isReachable = try {
                    val urlServer = URL(url)
                    val urlConn = urlServer.openConnection() as HttpURLConnection
                    urlConn.connectTimeout = 3000
                    urlConn.connect()
                    urlConn.responseCode == 200
                } catch (e: Exception) {
                    false
                }
                if (isReachable) {
                    return url
                }
                currentPage++
                url = ApiConstants.getPictureUrl(mediaId, currentPage, bookPages[currentPage].imageType)
            }
        }
        return coverUrl
    }

    private fun loadBookThumbnails() {
        val mediaId = mBook.mediaId
        val bookPages: List<ImageMeasurements> = mBook.bookImages.pages
        if (bookPages.isEmpty()) {
            return
        }
        val maxPosition = Math.min(bookPages.size, mCurrentThumbnailPosition + THUMBNAILS_LIMIT)
        for (pageId in mCurrentThumbnailPosition until maxPosition) {
            val page = bookPages[pageId]
            val url = ApiConstants.getThumbnailByPage(mediaId, pageId + 1, page.imageType)
            mBookThumbnailList.add(url)
        }
        mCurrentThumbnailPosition += THUMBNAILS_LIMIT
        isInfoLoaded = true
    }

    private fun loadRecommendBook() {
        launch {
            mBookRepository.getRecommendBook(mBook.bookId)?.bookList?.let { bookList ->
                val recentList = LinkedList<Int>()
                val favoriteList = LinkedList<Int>()
                for (id in 0 until bookList.size) {
                    bookList[id].bookId.let { bookId ->
                        when {
                            mBookRepository.isFavoriteBook(bookId) -> favoriteList.add(id)
                            mBookRepository.isRecentBook(bookId) -> recentList.add(id)
                            else -> {
                            }
                        }
                    }
                }
                Logger.d(TAG, "Number of recommend book of book ${mBook.bookId}: ${bookList.size}")
                launch(UI) {
                    if (!bookList.isEmpty()) {
                        mView.showRecommendBook(bookList)
                        if (!recentList.isEmpty()) {
                            mView.showRecentBooks(recentList)
                        }
                        if (!favoriteList.isEmpty()) {
                            mView.showFavoriteBooks(favoriteList)
                        }
                    } else {
                        mView.showNoRecommendBook()
                    }
                }
            }
        }
    }

    private fun refreshBookFavorite() {
        val countDownLatch = CountDownLatch(1)
        launch {
            isFavoriteBook = mBookRepository.isFavoriteBook(mBook.bookId)
            Logger.d(TAG, "isFavoriteBook: $isFavoriteBook")
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }
}