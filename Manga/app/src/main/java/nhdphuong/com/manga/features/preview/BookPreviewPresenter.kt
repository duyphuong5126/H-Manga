package nhdphuong.com.manga.features.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.Constants
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
import nhdphuong.com.manga.DownloadManager.Companion.BookDownloader as bookDownloader
import nhdphuong.com.manga.usecase.GetDownloadedBookCoverUseCase
import nhdphuong.com.manga.usecase.StartBookDeletingUseCase
import nhdphuong.com.manga.usecase.StartBookDownloadingUseCase

/*
 * Created by nhdphuong on 4/14/18.
 */
class BookPreviewPresenter @Inject constructor(
    private val view: BookPreviewContract.View,
    private val book: Book,
    private val getDownloadedBookCoverUseCase: GetDownloadedBookCoverUseCase,
    private val startBookDownloadingUseCase: StartBookDownloadingUseCase,
    private val startBookDeletingUseCase: StartBookDeletingUseCase,
    private val bookRepository: BookRepository,
    private val networkUtils: INetworkUtils,
    private val fileUtils: IFileUtils,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : BookPreviewContract.Presenter {

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
    private lateinit var cacheCoverUrl: String

    private lateinit var tagList: LinkedList<Tag>
    private lateinit var artistList: LinkedList<Tag>
    private lateinit var categoryList: LinkedList<Tag>
    private lateinit var languageList: LinkedList<Tag>
    private lateinit var parodyList: LinkedList<Tag>
    private lateinit var characterList: LinkedList<Tag>
    private lateinit var groupList: LinkedList<Tag>

    private val bookThumbnailList = ArrayList<String>()

    private var currentThumbnailPosition = 0

    private val uploadedTimeStamp: String = SupportUtils.getTimeElapsed(
        System.currentTimeMillis() - book.updateAt * MILLISECOND
    )

    private var isFavoriteBook: Boolean = false

    private var viewDownloadedData: Boolean = false

    private val compositeDisposable = CompositeDisposable()

    init {
        view.setPresenter(this)
    }

    override fun enableViewDownloadedDataMode() {
        viewDownloadedData = true
    }

    override fun start() {
        bookThumbnailList.clear()
        currentThumbnailPosition = 0
        if (!this::cacheCoverUrl.isInitialized) {
            if (viewDownloadedData) {
                getDownloadedBookCoverUseCase.execute(book.bookId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = { coverImagePath ->
                        Logger.d(TAG, "Downloaded cover of book ${book.bookId}: $coverImagePath")
                        view.showBookCoverImage(coverImagePath)
                    }, onError = {
                        Logger.e(TAG, "Failed to get downloaded cover of book ${book.bookId}: $it")
                        view.showBookCoverImage(ApiConstants.getBookCover(book.mediaId))
                    }).addTo(compositeDisposable)
            } else {
                view.showBookCoverImage(ApiConstants.getBookCover(book.mediaId))
            }
        } else {
            view.showBookCoverImage(cacheCoverUrl)
        }
        refreshBookFavorite()
        view.showFavoriteBookSaved(isFavoriteBook)
        view.show1stTitle(book.title.englishName)
        view.show2ndTitle(book.title.japaneseName)
        view.showUploadedTime(uploadedTimeStamp)
        view.showPageCount(book.numOfPages)
        tagList = LinkedList()
        categoryList = LinkedList()
        artistList = LinkedList()
        characterList = LinkedList()
        languageList = LinkedList()
        parodyList = LinkedList()
        groupList = LinkedList()
    }

    override fun loadInfoLists() {
        if (!isInfoLoaded) {
            for (tag in book.tags) {
                when (tag.type) {
                    Constants.TAG -> tagList.add(tag)
                    Constants.CATEGORY -> categoryList.add(tag)
                    Constants.CHARACTER -> characterList.add(tag)
                    Constants.ARTIST -> artistList.add(tag)
                    Constants.LANGUAGE -> languageList.add(tag)
                    Constants.PARODY -> parodyList.add(tag)
                    Constants.GROUP -> groupList.add(tag)
                }
            }

            if (!isTagListInitialized) {
                if (tagList.isEmpty()) {
                    view.hideTagList()
                } else {
                    view.showTagList(tagList)
                }
                isTagListInitialized = true
            }
            if (!isArtistListInitialized) {
                if (artistList.isEmpty()) {
                    view.hideArtistList()
                } else {
                    view.showArtistList(artistList)
                }
                isArtistListInitialized = true
            }
            if (!isLanguageListInitialized) {
                if (languageList.isEmpty()) {
                    view.hideLanguageList()
                } else {
                    view.showLanguageList(languageList)
                }
                isLanguageListInitialized = true
            }
            if (!isCategoryListInitialized) {
                if (categoryList.isEmpty()) {
                    view.hideCategoryList()
                } else {
                    view.showCategoryList(categoryList)
                }
                isCategoryListInitialized = true
            }
            if (!isCharacterListInitialized) {
                if (characterList.isEmpty()) {
                    view.hideCharacterList()
                } else {
                    view.showCharacterList(characterList)
                }
                isCharacterListInitialized = true
            }
            if (!isGroupListInitialized) {
                if (groupList.isEmpty()) {
                    view.hideGroupList()
                } else {
                    view.showGroupList(groupList)
                }
                isGroupListInitialized = true
            }
            if (!isParodyListInitialized) {
                if (parodyList.isEmpty()) {
                    view.hideParodyList()
                } else {
                    view.showParodyList(parodyList)
                }
                isParodyListInitialized = true
            }

            loadBookThumbnails()
            view.showBookThumbnailList(bookThumbnailList)

            if (!viewDownloadedData) {
                loadRecommendBook()
            }
        }
    }

    override fun reloadCoverImage() {
        if (!isBookCoverReloaded) {
            isBookCoverReloaded = true
            io.launch {
                val coverUrl: String = getReachableBookCover()

                main.launch {
                    view.showBookCoverImage(coverUrl)
                }
            }
        }
    }

    override fun saveCurrentAvailableCoverUrl(url: String) {
        Logger.d(TAG, "Current available url: $url")
        cacheCoverUrl = url
    }

    override fun startReadingFrom(startReadingPage: Int) {
        view.startReadingFromPage(startReadingPage, book)
    }

    override fun downloadBook() {
        if (!fileUtils.isStoragePermissionAccepted()) {
            view.showRequestStoragePermission()
            return
        }

        if (!bookDownloader.isDownloading) {
            startBookDownloadingUseCase.execute(book)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onComplete = {
                    Logger.d(TAG, "Started downloading book ${book.bookId}")
                }, onError = {
                    Logger.d(TAG, "Failed to start downloading book ${book.bookId}: $it")
                }).addTo(compositeDisposable)
        } else {
            if (bookDownloader.bookId == book.bookId) {
                view.showThisBookBeingDownloaded()
            } else {
                view.showBookBeingDownloaded(bookDownloader.bookId)
            }
        }
    }

    override fun deleteBook() {
        startBookDeletingUseCase.execute(book.bookId)
    }

    override fun restartBookPreview(bookId: String) {
        io.launch {
            val bookDetails = bookRepository.getBookDetails(bookId)
            bookDetails?.let {
                main.launch {
                    BookPreviewActivity.restart(bookDetails)
                }
            }
        }
    }

    override fun changeBookFavorite() {
        io.launch {
            bookRepository.saveFavoriteBook(book.bookId, !isFavoriteBook)
            refreshBookFavorite()
            main.launch {
                view.showFavoriteBookSaved(isFavoriteBook)
            }
        }
    }

    override fun initDownloading(bookId: String, total: Int) {
        if (view.isActive() && book.bookId == bookId) {
            view.initDownloading(total)
        }
    }

    override fun updateDownloadingProgress(bookId: String, progress: Int, total: Int) {
        if (view.isActive() && book.bookId == bookId) {
            view.updateDownloadProgress(progress, total)
        }
    }

    override fun finishDownloading(bookId: String) {
        if (view.isActive() && book.bookId == bookId) {
            view.finishDownloading()
            view.showOpenFolderView()
        }
    }

    override fun finishDownloading(bookId: String, downloadingFailedCount: Int, total: Int) {
        if (view.isActive() && book.bookId == bookId) {
            view.finishDownloading(downloadingFailedCount, total)
        }
    }

    override fun initDeleting(bookId: String) {
        if (view.isActive() && book.bookId == bookId) {
            view.initDeleting()
        }
    }

    override fun updateDeletingProgress(bookId: String, progress: Int, total: Int) {
        if (view.isActive() && book.bookId == bookId) {
            view.updateDeletingProgress(progress, total)
        }
    }

    override fun finishDeleting(bookId: String) {
        if (view.isActive() && book.bookId == bookId) {
            view.finishDeleting(bookId)
        }
    }

    override fun finishDeleting(bookId: String, deletingFailedCount: Int) {
        if (view.isActive() && book.bookId == bookId) {
            view.finishDeleting(bookId, deletingFailedCount)
        }
    }

    override fun stop() {
        compositeDisposable.clear()
    }

    override fun loadMoreThumbnails() {
        if (bookThumbnailList.size < book.numOfPages) {
            loadBookThumbnails()
            view.updateBookThumbnailList()
        } else {
            Logger.d(TAG, "End of thumbnails list")
        }
    }

    private fun getReachableBookCover(): String {
        val mediaId = book.mediaId
        val coverUrl = ApiConstants.getBookCover(mediaId)
        if (networkUtils.isNetworkConnected()) {
            var isReachable = false
            val bookPages = book.bookImages.pages
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
                url = ApiConstants.getPictureUrl(
                    mediaId,
                    currentPage,
                    bookPages[currentPage].imageType
                )
                currentPage++
            }
        }
        return coverUrl
    }

    private fun loadBookThumbnails() {
        val mediaId = book.mediaId
        val bookPages: List<ImageMeasurements> = book.bookImages.pages
        if (bookPages.isEmpty()) {
            return
        }
        val maxPosition = min(bookPages.size, currentThumbnailPosition + THUMBNAILS_LIMIT)
        for (pageId in currentThumbnailPosition until maxPosition) {
            val page = bookPages[pageId]
            val url = ApiConstants.getThumbnailByPage(
                mediaId,
                pageId + 1,
                page.imageType
            )
            bookThumbnailList.add(url)
        }
        currentThumbnailPosition += THUMBNAILS_LIMIT
        isInfoLoaded = true
    }

    private fun loadRecommendBook() {
        io.launch {
            bookRepository.getRecommendBook(book.bookId)?.bookList?.let { bookList ->
                val recentList = LinkedList<String>()
                val favoriteList = LinkedList<String>()
                bookList.forEach {
                    val bookId = it.bookId
                    when {
                        bookRepository.isFavoriteBook(bookId) -> favoriteList.add(bookId)
                        bookRepository.isRecentBook(bookId) -> recentList.add(bookId)
                        else -> {
                        }
                    }
                }
                Logger.d(
                    TAG, "Number of recommend book of book ${book.bookId}:" +
                            " ${bookList.size}"
                )
                main.launch {
                    if (!bookList.isEmpty()) {
                        view.showRecommendBook(bookList)
                        if (!recentList.isEmpty()) {
                            view.showRecentBooks(recentList)
                        }
                        if (!favoriteList.isEmpty()) {
                            view.showFavoriteBooks(favoriteList)
                        }
                    } else {
                        view.showNoRecommendBook()
                    }
                }
            }
        }
    }

    private fun refreshBookFavorite() {
        runBlocking {
            isFavoriteBook = suspendCoroutine { continuation ->
                io.launch {
                    continuation.resume(bookRepository.isFavoriteBook(book.bookId))
                }
            }
            Logger.d(TAG, "isFavoriteBook: $isFavoriteBook")
        }
    }
}
