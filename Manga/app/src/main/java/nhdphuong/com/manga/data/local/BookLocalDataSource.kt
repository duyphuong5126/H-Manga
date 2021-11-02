package nhdphuong.com.manga.data.local

import androidx.room.EmptyResultSetException
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.BookDataSource
import nhdphuong.com.manga.data.SerializationService
import nhdphuong.com.manga.data.entity.BlockedBook
import nhdphuong.com.manga.data.entity.FavoriteBook
import nhdphuong.com.manga.data.entity.PendingDownloadBookPreview
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.BookImages
import nhdphuong.com.manga.data.entity.book.BookTitle
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.ITag
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.local.model.DownloadedBookModel
import nhdphuong.com.manga.data.local.model.ImageUsageType
import nhdphuong.com.manga.data.local.model.BookImageModel
import nhdphuong.com.manga.data.local.model.BookTagModel
import nhdphuong.com.manga.data.local.model.LastVisitedPage
import nhdphuong.com.manga.data.local.model.PendingDownloadBook
import nhdphuong.com.manga.data.toArtist
import nhdphuong.com.manga.data.toCategory
import nhdphuong.com.manga.data.toCharacter
import nhdphuong.com.manga.data.toGroup
import nhdphuong.com.manga.data.toLanguage
import nhdphuong.com.manga.data.toParody
import nhdphuong.com.manga.data.toTag
import java.util.LinkedList
import javax.inject.Inject

/*
 * Created by nhdphuong on 6/9/18.
 */
class BookLocalDataSource @Inject constructor(
    private val bookDAO: BookDAO,
    private val tagDAO: TagDAO,
    private val serializationService: SerializationService
) : BookDataSource.Local {
    private val logger = Logger("BookLocalDataSource")

    override suspend fun saveRecentBook(book: Book) {
        val recentBook = try {
            bookDAO.getRecentBookSynchronously(book.bookId)!!
        } catch (throwable: Throwable) {
            if (throwable is EmptyResultSetException || throwable is NullPointerException) {
                RecentBook(book.bookId, 0, serializeBook(book), 0)
            } else throw throwable
        }
        recentBook.readingTimes++
        recentBook.createdAt = System.currentTimeMillis()
        bookDAO.insertRecentBooks(recentBook)
    }

    override suspend fun saveFavoriteBook(book: Book) {
        bookDAO.insertFavoriteBooks(
            FavoriteBook(
                book.bookId,
                System.currentTimeMillis(),
                serializeBook(book)
            )
        )
    }

    override suspend fun removeFavoriteBook(book: Book) {
        bookDAO.deleteFavoriteBook(book.bookId)
    }

    override suspend fun addBookToBlockList(bookId: String) {
        bookDAO.insertBlockedBooks(BlockedBook(bookId))
    }

    override fun getEmptyFavoriteBooks(): Single<List<FavoriteBook>> {
        return bookDAO.getEmptyFavoriteBooks()
    }

    override fun getEmptyRecentBooks(): Single<List<RecentBook>> {
        return bookDAO.getEmptyRecentBooks()
    }

    override fun getEmptyFavoriteBooksCount(): Int {
        return bookDAO.getEmptyFavoriteBooksCount()
    }

    override fun getEmptyRecentBooksCount(): Int {
        return bookDAO.getEmptyRecentBooksCount()
    }

    override suspend fun getRecentBooks(limit: Int, offset: Int): LinkedList<RecentBook> {
        val result = LinkedList<RecentBook>()
        result.addAll(bookDAO.getRecentBooks(limit, offset))
        return result
    }

    override suspend fun getFavoriteBooks(limit: Int, offset: Int): LinkedList<FavoriteBook> {
        val result = LinkedList<FavoriteBook>()
        result.addAll(bookDAO.getFavoriteBooks(limit, offset))
        return result
    }

    override suspend fun getAllFavoriteBookIds(): List<String> {
        return bookDAO.getFavoriteBookIds()
    }

    override suspend fun getAllRecentBookIds(): List<String> {
        return bookDAO.getRecentBookIds()
    }

    override fun updateRawFavoriteBook(bookId: String, rawBook: String): Boolean {
        return bookDAO.updateRawFavoriteBook(bookId, rawBook) > 0
    }

    override fun updateRawRecentBook(bookId: String, rawBook: String): Boolean {
        return bookDAO.updateRawRecentBook(bookId, rawBook) > 0
    }

    override suspend fun isFavoriteBook(bookId: String): Boolean {
        return bookDAO.getFavoriteBookCount(bookId) > 0
    }

    override fun checkIfFavoriteBook(bookId: String): Single<Boolean> {
        return Single.fromCallable {
            bookDAO.getFavoriteBookCount(bookId) > 0
        }
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

                val tagIds = tagList.map { it.tagId }
                val bookTags = ArrayList<Tag>(tagDAO.getTagsByIds(tagIds))
                bookTags.addAll(tagDAO.getArtistsByIds(tagIds).map(ITag::toTag))
                bookTags.addAll(tagDAO.getParodiesByIds(tagIds).map(ITag::toTag))
                bookTags.addAll(tagDAO.getCategoriesByIds(tagIds).map(ITag::toTag))
                bookTags.addAll(tagDAO.getLanguagesByIds(tagIds).map(ITag::toTag))
                bookTags.addAll(tagDAO.getCharactersByIds(tagIds).map(ITag::toTag))
                bookTags.addAll(tagDAO.getGroupsByIds(tagIds).map(ITag::toTag))

                val book = Book(
                    bookId = downloadedBookModel.bookId,
                    mediaId = downloadedBookModel.mediaId,
                    title = bookTitle,
                    bookImages = bookImages,
                    scanlator = downloadedBookModel.scanlator,
                    updateAt = downloadedBookModel.uploadDate,
                    tags = bookTags.distinctBy { it.name },
                    numOfFavorites = downloadedBookModel.numOfFavorites,
                    numOfPages = downloadedBookModel.numOfPages
                )
                Observable.just(book)
            }
    }

    override fun addToRecentList(book: Book): Completable {
        return bookDAO.getRecentBook(book.bookId)
            .onErrorResumeNext {
                if (it is EmptyResultSetException) {
                    val createdAt = System.currentTimeMillis()
                    val recentBook = RecentBook(book.bookId, createdAt, serializeBook(book), 1)
                    bookDAO.insertRecentBooks(recentBook)
                    Single.just(recentBook)
                } else {
                    Single.error(it)
                }
            }.ignoreElement()
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
            logger.d("Downloaded book saving result: $savingResult")
        }.andThen(extractAndSaveTagList(book))
    }

    override suspend fun updateDownloadedBook(book: Book): Boolean {
        return try {
            book.run {
                bookDAO.updateDownloadedBook(
                    bookId,
                    mediaId,
                    title.englishName,
                    title.japaneseName,
                    title.pretty,
                    scanlator,
                    updateAt,
                    numOfPages,
                    numOfFavorites
                ) > 0
            }
        } catch (error: Throwable) {
            logger.e("Could not update downloaded book with error $error")
            false
        }
    }

    override fun getDownloadedBookCoverPath(bookId: String): Single<String> {
        return bookDAO.getFirstImagePathOfBook(bookId, ImageUsageType.COVER)
            .flatMap { cover ->
                if (cover.isNotBlank()) {
                    Single.just(cover)
                } else {
                    getFirstNotBlankBookPage(bookId)
                }
            }.onErrorResumeNext { error ->
                if (error is EmptyResultSetException) {
                    Single.just("")
                } else {
                    Single.error(error)
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
                    }.onErrorResumeNext { error ->
                        if (error is EmptyResultSetException) {
                            Single.just(Pair(bookId, ""))
                        } else {
                            Single.error(error)
                        }
                    }
                    .toObservable()
            }.toList()
    }

    override fun clearDownloadedImagesOfBook(bookId: String): Completable {
        return Completable.fromCallable {
            val deletedRecords = bookDAO.clearDownloadedImages(bookId)
            logger.d("Deleted $deletedRecords image(s) of book $bookId")
        }
    }

    override fun deleteBook(bookId: String): Completable {
        return Completable.fromCallable {
            val deletedRecords = bookDAO.deleteBook(bookId)
            logger.d("Deleted $deletedRecords book(s) by id $bookId")
        }
    }

    override suspend fun unSeenBook(bookId: String): Boolean {
        val deletingResult = bookDAO.deleteRecentBook(bookId)
        logger.d("Deleting result of $bookId: $deletingResult")
        return deletingResult > 0
    }

    override suspend fun deleteLastVisitedPage(bookId: String): Boolean {
        return bookDAO.deleteLastVisitedPage(bookId) > 0
    }

    override fun saveLastVisitedPage(bookId: String, lastVisitedPage: Int): Completable {
        return Completable.fromCallable {
            val insertResult = bookDAO.addLastVisitedPage(LastVisitedPage(bookId, lastVisitedPage))
            logger.d("Inserted ${insertResult.size} row(s)")
        }
    }

    override fun getLastVisitedPage(bookId: String): Single<Int> {
        return bookDAO.getLastVisitedPage(bookId)
    }

    override fun getMostUsedTags(maximumEntries: Int): Single<List<String>> {
        return bookDAO.getMostUsedTags(maximumEntries)
            .map {
                val mostUsedTagNames = arrayListOf<String>()
                mostUsedTagNames.addAll(tagDAO.getArtistsByIds(it).map(Artist::name))
                mostUsedTagNames.addAll(tagDAO.getCharactersByIds(it).map(Character::name))
                mostUsedTagNames.addAll(tagDAO.getParodiesByIds(it).map(Parody::name))
                mostUsedTagNames.addAll(tagDAO.getTagsByIds(it).map(Tag::name))
                mostUsedTagNames.addAll(tagDAO.getCategoriesByIds(it).map(Category::name))
                mostUsedTagNames.addAll(tagDAO.getLanguagesByIds(it).map(Language::name))
                mostUsedTagNames.addAll(tagDAO.getGroupsByIds(it).map(Group::name))
                mostUsedTagNames
            }
    }

    override fun getRecentBookIdsForRecommendation(): Single<List<String>> {
        return bookDAO.getRecentBookIdsForRecommendation()
    }

    override suspend fun getBlockedBookIds(): List<String> {
        return bookDAO.getBlockedBookIds()
    }

    override fun putBookIntoPendingDownloadList(book: Book) {
        val pendingItem = PendingDownloadBook(
            book.bookId,
            book.title.pretty,
            serializationService.serialize(book)
        )
        val insertionResult = bookDAO.addPendingDownloadBook(pendingItem)
        logger.d("Pending item ${book.bookId}'s insertion result: $insertionResult")
    }

    override fun removeBookFromPendingDownloadList(bookId: String) {
        val removalResult = bookDAO.removeBookFromPendingDownloadList(bookId)
        logger.d("Pending item $bookId's removal result: $removalResult")
    }

    override fun getOldestPendingDownloadBook(): Maybe<Book> {
        return bookDAO.getOldestPendingDownloadBook().map {
            serializationService.deserialize(it, Book::class.java)
        }
    }

    override fun getPendingDownloadBooks(limit: Int): Single<List<PendingDownloadBookPreview>> {
        return bookDAO.getPendingDownloadBooks(limit).map {
            it.map { pendingBook ->
                PendingDownloadBookPreview(pendingBook.bookId, pendingBook.titlePretty)
            }
        }
    }

    override fun getPendingDownloadBookCount(): Single<Int> {
        return bookDAO.getPendingDownloadBookCount()
    }

    private fun extractAndSaveTagList(book: Book): Completable {
        return Completable.fromCallable {
            tagDAO.insertTags(book.tags.filter { it.type == Constants.TAG })
            book.tags.filter { it.type == Constants.ARTIST }.map { it.toArtist() }
                .let(tagDAO::insertArtists)
            book.tags.filter { it.type == Constants.CHARACTER }.map { it.toCharacter() }
                .let(tagDAO::insertCharacters)
            book.tags.filter { it.type == Constants.GROUP }.map { it.toGroup() }
                .let(tagDAO::insertGroups)
            book.tags.filter { it.type == Constants.CATEGORY }.map { it.toCategory() }
                .let(tagDAO::insertCategories)
            book.tags.filter { it.type == Constants.LANGUAGE }.map { it.toLanguage() }
                .let(tagDAO::insertLanguages)
            book.tags.filter { it.type == Constants.PARODY }.map { it.toParody() }
                .let(tagDAO::insertParodies)
        }.doOnComplete {
            val savingResult = bookDAO.addTagListOfBook(book.tags.map { tag ->
                BookTagModel(bookId = book.bookId, tagId = tag.id)
            })
            logger.d("Tags saving result of book ${book.bookId}: $savingResult")
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

    private fun serializeBook(book: Book): String {
        return serializationService.serialize(book)
    }
}
