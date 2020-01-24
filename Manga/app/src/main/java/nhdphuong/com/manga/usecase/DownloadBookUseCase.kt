package nhdphuong.com.manga.usecase

import io.reactivex.Observable
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.data.entity.DownloadingResult
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingProgress
import nhdphuong.com.manga.data.entity.DownloadingResult.DownloadingFailure
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.supports.AppSupportUtils
import nhdphuong.com.manga.supports.IFileUtils
import javax.inject.Inject
import kotlin.collections.ArrayList

interface DownloadBookUseCase {
    fun execute(book: Book): Observable<DownloadingResult>
}

class DownloadBookUseCaseImpl @Inject constructor(
    private val fileUtils: IFileUtils,
    private val appSupportUtils: AppSupportUtils
) : DownloadBookUseCase {
    override fun execute(book: Book): Observable<DownloadingResult> {
        val resultList = ArrayList<String>()
        var progress = 0
        val total = book.numOfPages
        return Observable.fromCallable {
            val bookPages = ArrayList<Triple<Int, String, String>>()
            for (pageIndex in book.bookImages.pages.indices) {
                val page = book.bookImages.pages[pageIndex]
                val pageUrl =
                    ApiConstants.getPictureUrl(book.mediaId, pageIndex + 1, page.imageType)
                bookPages.add(Triple(pageIndex, pageUrl, page.imageType))
            }
            bookPages
        }.flatMap { bookPages ->
            Observable.fromIterable(bookPages)
        }.map { (index, pageUrl, imageType) ->
            try {
                val prefixNumber = getPrefixNumber(book.numOfPages)
                val fileName = String.format("%0${prefixNumber}d", index + 1)
                val resultDir = fileUtils.getImageDirectory(book.usefulName)

                val resultPath =
                    appSupportUtils.downloadAndSaveImage(pageUrl, resultDir, fileName, imageType)
                resultList.add(resultPath)
                Logger.d(TAG, "Downloaded page $pageUrl")
                DownloadingProgress(++progress, total)
            } catch (exception: Exception) {
                Logger.d(TAG, "Failed to download page $pageUrl")
                DownloadingFailure(pageUrl, exception)
            }
        }.doOnComplete {
            fileUtils.refreshGallery(*resultList.toTypedArray())
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

    companion object {
        private const val TAG = "DownloadBookUseCase"
        private const val TEN_PAGES = 10
    }
}