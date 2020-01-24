package nhdphuong.com.manga.features.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.IFileUtils
import nhdphuong.com.manga.supports.INetworkUtils
import nhdphuong.com.manga.supports.SupportUtils
import nhdphuong.com.manga.usecase.DownloadBookUseCase
import java.util.LinkedList
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.disposables.CompositeDisposable
import nhdphuong.com.manga.data.entity.DownloadingResult

/*
 * Created by nhdphuong on 4/14/18.
 */
class BookPreviewPresenter @Inject constructor(
    private val mView: BookPreviewContract.View,
    private val mBook: Book,
    private val mBookRepository: BookRepository,
    private val downloadBookUseCase: DownloadBookUseCase,
    private val networkUtils: INetworkUtils,
    private val fileUtils: IFileUtils,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : BookPreviewContract.Presenter, DownloadManager.BookDownloadCallback {

    companion object {
        private const val TAG = "BookPreviewPresenter"
        private const val MILLISECOND: Long = 1000

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

    private val uploadedTimeStamp: String = SupportUtils.getTimeElapsed(
        System.currentTimeMillis() - mBook.updateAt * MILLISECOND
    )

    private var isFavoriteBook: Boolean = false

    private val mBookDownloader = DownloadManager.Companion.BookDownloader

    private val compositeDisposable = CompositeDisposable()

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
        mView.showUploadedTime(uploadedTimeStamp)
        mView.showPageCount(mBook.numOfPages)
        mTagList = LinkedList()
        mCategoryList = LinkedList()
        mArtistList = LinkedList()
        mCharacterList = LinkedList()
        mLanguageList = LinkedList()
        mParodyList = LinkedList()
        mGroupList = LinkedList()
        if (mBookDownloader.isDownloading && mBookDownloader.bookId == mBook.bookId) {
            mBookDownloader.setDownloadCallback(this)
            mView.updateDownloadProgress(mBookDownloader.progress, mBookDownloader.total)
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
            io.launch {
                val coverUrl: String = getReachableBookCover()

                main.launch {
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
        mView.startReadingFromPage(startReadingPage, mBook)
    }

    override fun downloadBook() {
        if (!fileUtils.isStoragePermissionAccepted()) {
            mView.showRequestStoragePermission()
            return
        }

        if (!mBookDownloader.isDownloading) {
            downloadBookUseCase.execute(mBook)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    mBookDownloader.setDownloadCallback(this)
                    mBookDownloader.startDownloading(mBook.bookId, mBook.numOfPages)
                }
                .subscribeBy(onNext = { result ->
                    when (result) {
                        is DownloadingResult.DownloadingProgress -> {
                            mBookDownloader.updateProgress(mBook.bookId, result.progress)
                        }
                    }
                }, onError = {
                    Logger.e(TAG, "Failure in downloading book ${mBook.mediaId} with error: $it")
                    mBookDownloader.endDownloading(mBookDownloader.progress, mBookDownloader.total)
                }, onComplete = {
                    mBookDownloader.endDownloading(mBookDownloader.progress, mBookDownloader.total)
                    mView.showOpenFolderView()
                }).addTo(compositeDisposable)
        } else {
            if (mBookDownloader.bookId == mBook.bookId) {
                mView.showThisBookBeingDownloaded()
            } else {
                mView.showBookBeingDownloaded(mBookDownloader.bookId)
            }
        }
    }

    override fun restartBookPreview(bookId: String) {
        io.launch {
            val bookDetails = mBookRepository.getBookDetails(bookId)
            bookDetails?.let {
                main.launch {
                    BookPreviewActivity.restart(bookDetails)
                }
            }
        }
    }

    override fun changeBookFavorite() {
        io.launch {
            mBookRepository.saveFavoriteBook(mBook.bookId, !isFavoriteBook)
            refreshBookFavorite()
            main.launch {
                mView.showFavoriteBookSaved(isFavoriteBook)
            }
        }
    }

    override fun stop() {

    }

    override fun onDownloadingStarted(bookId: String, total: Int) {
        if (mView.isActive()) {
            main.launch {
                mView.initDownloading(total)
            }
        }
    }

    override fun onProgressUpdated(bookId: String, progress: Int, total: Int) {
        if (mView.isActive()) {
            main.launch {
                mView.updateDownloadProgress(progress, total)
            }
        }
    }

    override fun onDownloadingEnded(downloaded: Int, total: Int) {
        Logger.d(TAG, "$downloaded/$total pages are downloaded")
        if (mView.isActive()) {
            main.launch {
                if (downloaded == total) {
                    mView.finishDownloading()
                } else {
                    mView.finishDownloading(total - downloaded, total)
                }
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
        val mediaId = mBook.mediaId
        val coverUrl = ApiConstants.getBookCover(mediaId)
        if (networkUtils.isNetworkConnected()) {
            var isReachable = false
            val bookPages = mBook.bookImages.pages
            var currentPage = 0
            var url =
                ApiConstants.getPictureUrl(mediaId, currentPage, bookPages[currentPage].imageType)
            while (!isReachable && currentPage < bookPages.size) {
                isReachable = try {
                    networkUtils.isReachableUrl(url)
                } catch (e: Exception) {
                    false
                }
                if (isReachable) {
                    return url
                }
                currentPage++
                url = ApiConstants.getPictureUrl(
                    mediaId,
                    currentPage,
                    bookPages[currentPage].imageType
                )
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
        val maxPosition = min(bookPages.size, mCurrentThumbnailPosition + THUMBNAILS_LIMIT)
        for (pageId in mCurrentThumbnailPosition until maxPosition) {
            val page = bookPages[pageId]
            val url = ApiConstants.getThumbnailByPage(
                mediaId,
                pageId + 1,
                page.imageType
            )
            mBookThumbnailList.add(url)
        }
        mCurrentThumbnailPosition += THUMBNAILS_LIMIT
        isInfoLoaded = true
    }

    private fun loadRecommendBook() {
        io.launch {
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
                Logger.d(
                    TAG, "Number of recommend book of book ${mBook.bookId}:" +
                            " ${bookList.size}"
                )
                main.launch {
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
        runBlocking {
            isFavoriteBook = suspendCoroutine { continuation ->
                io.launch {
                    continuation.resume(mBookRepository.isFavoriteBook(mBook.bookId))
                }
            }
            Logger.d(TAG, "isFavoriteBook: $isFavoriteBook")
        }
    }
}
