package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.DownloadingResult
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingProgress
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingFailure
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.ImageMeasurements
import nhdphuong.com.manga.data.local.model.ImageUsageType
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.supports.AppSupportUtils
import nhdphuong.com.manga.supports.IFileUtils
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlin.collections.ArrayList

interface DownloadBookUseCase {
    fun execute(book: Book): Observable<DownloadingResult>
}

class DownloadBookUseCaseImpl @Inject constructor(
    private val fileUtils: IFileUtils,
    private val appSupportUtils: AppSupportUtils,
    private val bookRepository: BookRepository
) : DownloadBookUseCase {
    private val logger: Logger by lazy {
        Logger("DownloadBookUseCaseImpl")
    }

    override fun execute(book: Book): Observable<DownloadingResult> {
        return bookRepository.addToRecentList(book)
            .andThen(bookRepository.saveDownloadedBook(book))
            .andThen(bookRepository.clearDownloadedImagesOfBook(book.bookId))
            .andThen(saveBookCover(book))
            .andThen(saveBookThumbnail(book))
            .andThen(saveBookPages(book))
    }

    private fun saveBookCover(book: Book): Completable {
        return Single.fromCallable {
            val resultDir = getBookDirectory(book)
            appSupportUtils.downloadAndSaveImage(
                ApiConstants.getBookCover(book.mediaId),
                resultDir,
                "cover",
                book.bookImages.cover.imageType
            )
        }.onErrorResumeNext {
            if (it is FileNotFoundException) {
                Single.just("")
            } else {
                Single.error(it)
            }
        }.flatMapCompletable { coverPath ->
            fileUtils.refreshGallery(false, coverPath)
            bookRepository.saveImageOfBook(
                book.bookId,
                book.bookImages.cover,
                ImageUsageType.COVER,
                coverPath
            )
        }
    }

    private fun saveBookThumbnail(book: Book): Completable {
        return Single.fromCallable {
            val resultDir = getBookDirectory(book)
            appSupportUtils.downloadAndSaveImage(
                ApiConstants.getBookCover(book.mediaId),
                resultDir,
                "thumbnail",
                book.bookImages.thumbnail.imageType
            )
        }.onErrorResumeNext {
            if (it is FileNotFoundException) {
                Single.just("")
            } else {
                Single.error(it)
            }
        }.flatMapCompletable { thumbnailPath ->
            fileUtils.refreshGallery(false, thumbnailPath)
            bookRepository.saveImageOfBook(
                book.bookId,
                book.bookImages.cover,
                ImageUsageType.THUMBNAIL,
                thumbnailPath
            )
        }
    }

    private fun saveBookPages(book: Book): Observable<DownloadingResult> {
        val resultList = ArrayList<String>()
        var progress = 0
        val total = book.numOfPages
        return generateBookPagesInfo(book)
            .flatMap { bookPages ->
                Observable.fromIterable(bookPages)
            }
            .flatMap { (index, pageUrl, imageInfo) ->
                try {
                    val resultPath =
                        saveImageAndGetOutputPath(book, index, pageUrl, imageInfo.imageType)
                    resultList.add(resultPath)
                    logger.d("Downloaded page $pageUrl")
                    Observable.just(
                        Triple(
                            resultPath,
                            imageInfo,
                            DownloadingProgress(++progress, total)
                        )
                    )
                } catch (exception: Exception) {
                    logger.e("Failed to download page $pageUrl with error $exception")
                    if (exception is FileNotFoundException) {
                        Observable.just(
                            Triple(
                                "",
                                imageInfo,
                                DownloadingFailure(pageUrl, exception)
                            )
                        )
                    } else {
                        Observable.error(exception)
                    }
                }
            }
            .flatMap { (resultPath, imageInfo, downloadingResult) ->
                bookRepository.saveImageOfBook(
                    book.bookId,
                    imageInfo,
                    ImageUsageType.BOOK_PAGE,
                    resultPath
                ).andThen(Observable.just(downloadingResult))
            }
            .doOnComplete {
                fileUtils.refreshGallery(true, *resultList.toTypedArray())
            }
    }

    private fun saveImageAndGetOutputPath(
        book: Book,
        index: Int,
        imageUrl: String,
        imageType: String
    ): String {
        val prefixNumber = getPrefixNumber(book.numOfPages)
        val fileName = String.format("%0${prefixNumber}d", index + 1)
        val resultDir = getBookDirectory(book)
        return appSupportUtils.downloadAndSaveImage(
            imageUrl,
            resultDir,
            fileName,
            imageType
        )
    }

    private fun generateBookPagesInfo(book: Book): Observable<List<Triple<Int, String, ImageMeasurements>>> {
        return Observable.fromCallable {
            val bookPages = ArrayList<Triple<Int, String, ImageMeasurements>>()
            for (pageIndex in book.bookImages.pages.indices) {
                val page = book.bookImages.pages[pageIndex]
                val pageUrl =
                    ApiConstants.getPictureUrl(book.mediaId, pageIndex + 1, page.imageType)
                bookPages.add(Triple(pageIndex, pageUrl, page))
            }
            bookPages
        }
    }

    private fun getPrefixNumber(total: Int): Int {
        var totalPages = total
        var prefixCount = 1
        while (totalPages / TEN_PAGES > 0) {
            totalPages /= TEN_PAGES
            prefixCount++
        }
        return prefixCount
    }

    private fun getBookDirectory(book: Book): String {
        return fileUtils.getImageDirectory(book.usefulName.replace(File.separator, "_"))
    }

    companion object {
        private const val TEN_PAGES = 10
    }
}
