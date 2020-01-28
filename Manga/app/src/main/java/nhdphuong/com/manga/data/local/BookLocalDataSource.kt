package nhdphuong.com.manga.data.local

import androidx.room.EmptyResultSetException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.BookImages
import nhdphuong.com.manga.data.entity.book.BookTitle
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.local.model.BookImageModel
import nhdphuong.com.manga.data.local.model.BookTagModel
import nhdphuong.com.manga.data.local.model.DownloadedBookModel
import nhdphuong.com.manga.data.local.model.ImageUsageType
import java.util.LinkedList
import javax.inject.Inject

/*
 * Created by nhdphuong on 6/9/18.
 */
class BookLocalDataSource @Inject constructor(
    private val bookDAO: BookDAO,
    private val tagDAO: TagDAO
) : BookDataSource.Local {
    override suspend fun saveRecentBook(bookId: String) {
        bookDAO.insertRecentBooks(RecentBook(bookId, false, System.currentTimeMillis()))
    }

    override suspend fun saveFavoriteBook(bookId: String, isFavorite: Boolean) {
        bookDAO.insertRecentBooks(RecentBook(bookId, isFavorite, System.currentTimeMillis()))
    }

    override suspend fun getRecentBooks(limit: Int, offset: Int): LinkedList<RecentBook> {
        val result = LinkedList<RecentBook>()
        result.addAll(bookDAO.getRecentBooks(limit, offset))
        return result
    }

    override suspend fun getFavoriteBook(limit: Int, offset: Int): LinkedList<RecentBook> {
        val result = LinkedList<RecentBook>()
        result.addAll(bookDAO.getFavoriteBooks(limit, offset))
        return result
    }

    override suspend fun isFavoriteBook(bookId: String): Boolean {
        return bookDAO.isFavoriteBook(bookId) == 1
    }

    override suspend fun isRecentBook(bookId: String): Boolean {
        return bookDAO.getRecentBookId(bookId) == bookId
    }

    override suspend fun getRecentCount(): Int = bookDAO.getRecentBookCount()

    override suspend fun getFavoriteCount(): Int = bookDAO.getFavoriteBookCount()

    override fun getDownloadedBookList(): Single<List<Book>> {
        return bookDAO.getAllDownloadedBooks()
            .toObservable()
            .flatMap {
                Observable.fromIterable(it)
            }.flatMap { downloadedBookModel ->
                collectDownloadedDataOfBook(downloadedBookModel)
            }.toList()
    }

    private fun collectDownloadedDataOfBook(downloadedBookModel: DownloadedBookModel): Observable<Book> {
        return bookDAO.getAllTagsOfBook(downloadedBookModel.bookId)
            .zipWith(bookDAO.getAllImagesOfBook(downloadedBookModel.bookId))
            .flatMapObservable { (tagList, imageList) ->
                val bookTitle = BookTitle(
                    downloadedBookModel.titleEng,
                    downloadedBookModel.titleJapanese,
                    downloadedBookModel.titlePretty
                )

                val cover = imageList.firstOrNull {
                    it.usageType == ImageUsageType.COVER
                }?.let {
                    ImageMeasurements(it.imageType, it.width, it.height)
                } ?: ImageMeasurements(Constants.JPG, 0, 0)
                val thumbnail = imageList.firstOrNull {
                    it.usageType == ImageUsageType.THUMBNAIL
                }?.let {
                    ImageMeasurements(it.imageType, it.width, it.height)
                } ?: ImageMeasurements(Constants.JPG, 0, 0)
                val bookPages = imageList.filter { it.usageType == ImageUsageType.BOOK_PAGE }
                    .map {
                        ImageMeasurements(it.imageType, it.width, it.height)
                    }
                val bookImages = BookImages(bookPages, cover, thumbnail)

                val bookTags = tagList.map {
                    tagDAO.getTagById(it.tagId)
                }
                val book = Book(
                    bookId = downloadedBookModel.bookId,
                    mediaId = downloadedBookModel.mediaId,
                    title = bookTitle,
                    bookImages = bookImages,
                    scanlator = downloadedBookModel.scanlator,
                    updateAt = downloadedBookModel.uploadDate,
                    tags = bookTags,
                    numOfFavorites = downloadedBookModel.numOfFavorites,
                    numOfPages = downloadedBookModel.numOfPages
                )
                Observable.just(book)
            }
    }

    override fun addToRecentList(bookId: String): Completable {
        return Completable.fromCallable {
            bookDAO.insertRecentBooks(RecentBook(bookId, false, System.currentTimeMillis()))
        }
    }

    override fun saveImageOfBook(
        bookId: String,
        imageMeasurements: ImageMeasurements,
        usageType: String,
        localPath: String
    ): Completable {
        return Completable.fromCallable {
            bookDAO.addImageOfBook(
                BookImageModel(
                    bookId = bookId,
                    imageType = imageMeasurements.imageType,
                    usageType = usageType,
                    width = imageMeasurements.width,
                    height = imageMeasurements.height,
                    localPath = localPath
                )
            )
        }
    }

    override fun saveDownloadedBook(book: Book): Completable {
        return Completable.fromCallable {
            val savingResult = bookDAO.addDownloadedBook(listOf(book).map {
                DownloadedBookModel(
                    bookId = book.bookId,
                    mediaId = book.mediaId,
                    titleEng = book.title.englishName,
                    titleJapanese = book.title.japaneseName,
                    titlePretty = book.title.pretty,
                    scanlator = book.scanlator,
                    uploadDate = book.updateAt,
                    numOfFavorites = book.numOfFavorites,
                    numOfPages = book.numOfPages
                )
            })
            Logger.d(TAG, "Downloaded book saving result: $savingResult")
        }.andThen(extractAndSaveTagList(book))
    }

    override fun getDownloadedBookCoverPath(bookId: String): Single<String> {
        return bookDAO.getFirstImagePathOfBook(bookId, ImageUsageType.COVER)
            .flatMap { cover ->
                if (cover.isNotBlank()) {
                    Single.just(cover)
                } else {
                    getFirstNotBlankBookPage(bookId)
                }
            }
    }

    override fun getDownloadedBookImagePaths(bookId: String): Single<List<String>> {
        return bookDAO.getImagePathsOfBook(bookId, ImageUsageType.BOOK_PAGE)
    }

    override fun getDownloadedBookThumbnailPaths(bookIds: List<String>): Single<List<Pair<String, String>>> {
        return Observable.fromIterable(bookIds)
            .flatMap { bookId ->
                bookDAO.getFirstImagePathOfBook(bookId, ImageUsageType.THUMBNAIL)
                    .flatMap { thumbnail ->
                        if (thumbnail.isNotBlank()) {
                            Single.just(Pair(bookId, thumbnail))
                        } else {
                            getFirstNotBlankBookPage(bookId).map { Pair(bookId, it) }
                        }
                    }
                    .toObservable()
            }.toList()
    }

    override fun clearDownloadedImagesOfBook(bookId: String): Completable {
        return Completable.fromCallable {
            val deletedRecords = bookDAO.clearDownloadedImages(bookId)
            Logger.d(TAG, "Deleted $deletedRecords image(s) of book $bookId")
        }
    }

    private fun extractAndSaveTagList(book: Book): Completable {
        return Completable.fromCallable {
            tagDAO.insertTags(book.tags)
        }.doOnComplete {
            val savingResult = bookDAO.addTagListOfBook(book.tags.map { tag ->
                BookTagModel(bookId = book.bookId, tagId = tag.tagId)
            })
            Logger.d(TAG, "Tags saving result of book ${book.bookId}: $savingResult")
        }
    }

    private fun getFirstNotBlankBookPage(bookId: String): Single<String> {
        return bookDAO.getFirstNotBlankImagePathOfBook(bookId, ImageUsageType.BOOK_PAGE)
            .onErrorResumeNext { error ->
                if (error is EmptyResultSetException) {
                    Single.just("")
                } else {
                    Single.error(error)
                }
            }
    }

    companion object {
        private const val TAG = "BookLocalDataSource"
    }
}
