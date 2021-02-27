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
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.disposables.CompositeDisposable
import nhdphuong.com.manga.Constants.Companion.EVENT_ADD_FAVORITE
import nhdphuong.com.manga.Constants.Companion.EVENT_FORGET_BOOK
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_ANALYTICS_BOOK_ID
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_ANALYTICS_BOOK_LANGUAGE
import nhdphuong.com.manga.Constants.Companion.EVENT_OPEN_BOOK
import nhdphuong.com.manga.Constants.Companion.EVENT_REMOVE_FAVORITE
import nhdphuong.com.manga.analytics.AnalyticsParam
import nhdphuong.com.manga.data.entity.BookResponse
import nhdphuong.com.manga.data.entity.CommentResponse
import nhdphuong.com.manga.data.entity.RecommendBookResponse
import nhdphuong.com.manga.data.entity.comment.Comment
import nhdphuong.com.manga.DownloadManager.Companion.BookDownloader as bookDownloader
import nhdphuong.com.manga.usecase.GetDownloadedBookCoverUseCase
import nhdphuong.com.manga.usecase.GetDownloadedBookPagesUseCase
import nhdphuong.com.manga.usecase.GetLastVisitedPageUseCase
import nhdphuong.com.manga.usecase.LogAnalyticsEventUseCase
import nhdphuong.com.manga.usecase.StartBookDeletingUseCase
import nhdphuong.com.manga.usecase.StartBookDownloadingUseCase

/*
 * Created by nhdphuong on 4/14/18.
 */
class BookPreviewPresenter @Inject constructor(
    private val view: BookPreviewContract.View,
    private val book: Book,
    private val getDownloadedBookCoverUseCase: GetDownloadedBookCoverUseCase,
    private val getDownloadedBookPagesUseCase: GetDownloadedBookPagesUseCase,
    private val startBookDownloadingUseCase: StartBookDownloadingUseCase,
    private val startBookDeletingUseCase: StartBookDeletingUseCase,
    private val getLastVisitedPageUseCase: GetLastVisitedPageUseCase,
    private val logAnalyticsEventUseCase: LogAnalyticsEventUseCase,
    private val bookRepository: BookRepository,
    private val networkUtils: INetworkUtils,
    private val fileUtils: IFileUtils,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : BookPreviewContract.Presenter {

    companion object {
        private const val MILLISECOND: Long = 1000
        private const val COMMENTS_PER_PAGE = 25
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
    private var cacheCoverUrl: String = ""

    private lateinit var tagList: ArrayList<Tag>
    private lateinit var artistList: ArrayList<Tag>
    private lateinit var categoryList: ArrayList<Tag>
    private lateinit var languageList: ArrayList<Tag>
    private lateinit var parodyList: ArrayList<Tag>
    private lateinit var characterList: ArrayList<Tag>
    private lateinit var groupList: ArrayList<Tag>

    private val bookThumbnailList = ArrayList<String>()

    private val commentSource = ArrayList<Comment>()

    private val uploadedTimeStamp: String = SupportUtils.getTimeElapsed(
        System.currentTimeMillis() - book.updateAt * MILLISECOND
    )

    private var isFavoriteBook: Boolean = false

    private var viewDownloadedData: Boolean = false

    private val compositeDisposable = CompositeDisposable()

    private val logger = Logger("BookPreviewPresenter")

    init {
        view.setPresenter(this)
    }

    override fun enableViewDownloadedDataMode() {
        viewDownloadedData = true
    }

    override fun start() {
        logBookInfo()
        view.showBookId(book.bookId)
        bookThumbnailList.clear()
        if (cacheCoverUrl.isBlank()) {
            if (viewDownloadedData) {
                getDownloadedBookCoverUseCase.execute(book.bookId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onSuccess = { coverImagePath ->
                        logger.d("Downloaded cover of book ${book.bookId}: $coverImagePath")
                        view.showBookCoverImage(coverImagePath)
                    }, onError = {
                        logger.e("Failed to get downloaded cover of book ${book.bookId}: $it")
                        view.showBookCoverImage(ApiConstants.getBookCover(book.mediaId))
                    }).addTo(compositeDisposable)
            } else {
                view.showBookCoverImage(ApiConstants.getBookCover(book.mediaId))
            }
        } else {
            view.showBookCoverImage(cacheCoverUrl)
        }
        refreshBookFavorite()
        io.launch {
            if (bookRepository.isRecentBook(book.bookId)) {
                main.launch { view.showUnSeenButton() }
            } else {
                main.launch { view.hideUnSeenButton() }
            }
        }
        view.showFavoriteBookSaved(isFavoriteBook)
        view.show1stTitle(book.title.englishName)
        view.show2ndTitle(book.title.japaneseName)
        view.showUploadedTime(uploadedTimeStamp)
        view.showPageCount(book.numOfPages)
        tagList = ArrayList()
        categoryList = ArrayList()
        artistList = ArrayList()
        characterList = ArrayList()
        languageList = ArrayList()
        parodyList = ArrayList()
        groupList = ArrayList()

        if (viewDownloadedData) {
            view.hideCommentList()
            return
        }
        io.launch {
            when (val commentResponse = bookRepository.getCommentList(book.bookId)) {
                is CommentResponse.Success -> {
                    logger.d("commentList=${commentResponse.commentList.size}")
                    val startPosition = 0
                    val desiredEndPosition = COMMENTS_PER_PAGE
                    val endPosition = minOf(commentResponse.commentList.size, desiredEndPosition)
                    val firstPage = if (endPosition > startPosition) {
                        commentResponse.commentList.subList(startPosition, endPosition)
                    } else emptyList()
                    main.launch {
                        commentSource.addAll(commentResponse.commentList)
                        if (view.isActive()) {
                            if (firstPage.isNotEmpty()) {
                                view.setUpCommentList(firstPage, COMMENTS_PER_PAGE)
                            } else {
                                view.hideCommentList()
                            }
                        }
                    }
                }

                is CommentResponse.Failure -> {
                    logger.d("failed to get comment list with error: ${commentResponse.error}")
                    main.launch {
                        view.hideCommentList()
                    }
                }
            }
        }
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
                    if (view.isActive()) {
                        view.showBookCoverImage(coverUrl)
                    }
                }
            }
        }
    }

    override fun saveCurrentAvailableCoverUrl(url: String) {
        logger.d("Current available url: $url")
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
                    logger.d("Started downloading book ${book.bookId}")
                }, onError = {
                    logger.e("Failed to start downloading book ${book.bookId}: $it")
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
            val bookResponse = bookRepository.getBookDetails(bookId)
            if (bookResponse is BookResponse.Success) {
                main.launch {
                    BookPreviewActivity.restart(bookResponse.book)
                }
            }
        }
    }

    override fun changeBookFavorite() {
        io.launch {
            var favoriteEvent = EVENT_ADD_FAVORITE
            if (isFavoriteBook) {
                favoriteEvent = EVENT_REMOVE_FAVORITE
                bookRepository.removeFavoriteBook(book)
            } else {
                bookRepository.saveFavoriteBook(book)
            }

            val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, book.bookId)
            logAnalyticsEventUseCase.execute(favoriteEvent, bookIdParam)
                .subscribe()
                .addTo(compositeDisposable)

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

    override fun refreshRecentStatus() {
        io.launch {
            if (bookRepository.isRecentBook(book.bookId)) {
                main.launch { view.showUnSeenButton() }
            } else {
                main.launch { view.hideUnSeenButton() }
            }
        }
    }

    override fun unSeenBook() {
        io.launch {
            val unSubscribingSuccess = bookRepository.unSeenBook(book.bookId)
            if (unSubscribingSuccess) {
                val deleteLastVisitedPageResult = bookRepository.deleteLastVisitedPage(book.bookId)
                logger.d("Result of deleting last visited page of ${book.bookId}: $deleteLastVisitedPageResult")
                main.launch {
                    view.hideUnSeenButton()
                }
                val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, book.bookId)
                logAnalyticsEventUseCase.execute(EVENT_FORGET_BOOK, bookIdParam)
                    .subscribe()
                    .addTo(compositeDisposable)
            } else {
                main.launch {
                    view.showUnSeenButton()
                }
            }
        }
    }

    override fun loadLastVisitedPage() {
        getLastVisitedPageUseCase.execute(book.bookId)
            .map {
                if (it < book.bookImages.pages.size) {
                    it
                } else {
                    throw RuntimeException("Last visited page is not available")
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ lastVisitedPage ->
                logger.d("Last visited page of book ${book.bookId}: $lastVisitedPage")
                view.showLastVisitedPage(lastVisitedPage + 1, bookThumbnailList[lastVisitedPage])
            }, {
                logger.e("Failed to get last visited page of book ${book.bookId}: $it")
                view.hideLastVisitedPage()
            })
            .addTo(compositeDisposable)
    }

    override fun refreshLastVisitedPage(lastVisitedPage: Int) {
        logger.d("refreshLastVisitedPage $lastVisitedPage")
        if (lastVisitedPage in bookThumbnailList.indices) {
            logger.d("showLastVisitedPage $lastVisitedPage")
            view.showLastVisitedPage(lastVisitedPage + 1, bookThumbnailList[lastVisitedPage])
        } else {
            view.hideLastVisitedPage()
        }
    }

    override fun syncNextPageOfCommentList(currentCommentCount: Int) {
        if (currentCommentCount < COMMENTS_PER_PAGE || currentCommentCount % COMMENTS_PER_PAGE > 0) {
            return
        }
        logger.d("currentCommentCount=$currentCommentCount")
        if (currentCommentCount >= (2 * COMMENTS_PER_PAGE)) {
            val notShownComments = commentSource.size - currentCommentCount
            view.enableShowFullCommentListButton(notShownComments, book.bookId)
            return
        }
        val page = currentCommentCount / COMMENTS_PER_PAGE
        val startPosition = page * COMMENTS_PER_PAGE
        val desiredEndPosition = (page + 1) * COMMENTS_PER_PAGE
        val endPosition = minOf(commentSource.size, desiredEndPosition)

        if (endPosition > startPosition) {
            view.showMoreCommentList(commentSource.subList(startPosition, endPosition))
        }
    }

    override fun stop() {
        compositeDisposable.clear()
    }

    private fun getReachableBookCover(): String {
        val mediaId = book.mediaId
        val coverUrl = ApiConstants.getBookCover(mediaId)
        if (networkUtils.isNetworkConnected() && book.bookImages.pages.isNotEmpty()) {
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
        if (viewDownloadedData) {
            getDownloadedBookPagesUseCase.execute(book.bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { downloadedImages ->
                    logger.d("Book ${book.bookId}: ${downloadedImages.size} page(s)")
                    bookThumbnailList.addAll(downloadedImages)
                    view.showBookThumbnailList(bookThumbnailList)
                }, onError = {
                    logger.d("Failed to get pages of book ${book.bookId}: $it")
                }).addTo(compositeDisposable)
        } else {
            val mediaId = book.mediaId
            val bookPages: List<ImageMeasurements> = book.bookImages.pages
            if (bookPages.isEmpty()) {
                return
            }
            for (pageId in bookPages.indices) {
                val page = bookPages[pageId]
                val url = ApiConstants.getThumbnailByPage(
                    mediaId,
                    pageId + 1,
                    page.imageType
                )
                bookThumbnailList.add(url)
            }
            view.showBookThumbnailList(bookThumbnailList)
        }
        isInfoLoaded = true
    }

    private fun loadRecommendBook() {
        io.launch {
            val recommendBookResponse = bookRepository.getRecommendBook(book.bookId)
            if (recommendBookResponse is RecommendBookResponse.Success) {
                recommendBookResponse.recommendBook.bookList.let { bookList ->
                    val recentList = LinkedList<String>()
                    val favoriteList = LinkedList<String>()
                    bookList.forEach {
                        val bookId = it.bookId
                        when {
                            bookRepository.isFavoriteBook(bookId) -> favoriteList.add(bookId)
                            bookRepository.isRecentBook(bookId) -> recentList.add(bookId)
                        }
                    }
                    logger.d("Number of recommend book of book ${book.bookId}: ${bookList.size}")
                    main.launch {
                        if (view.isActive()) {
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
            } else {
                main.launch {
                    view.showNoRecommendBook()
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
            logger.d("isFavoriteBook: $isFavoriteBook")
        }
    }

    private fun logBookInfo() {
        val bookIdParam = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_ID, book.bookId)
        val bookLanguage = AnalyticsParam(PARAM_NAME_ANALYTICS_BOOK_LANGUAGE, book.language)
        logAnalyticsEventUseCase.execute(EVENT_OPEN_BOOK, bookIdParam, bookLanguage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }
}
