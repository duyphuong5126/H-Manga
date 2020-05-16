package nhdphuong.com.manga.usecase

import io.reactivex.Observable
import io.reactivex.rxkotlin.zipWith
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.entity.DeletingResult
import nhdphuong.com.manga.data.entity.DeletingResult.DeletingProgress
import nhdphuong.com.manga.data.entity.DeletingResult.DeletingFailure
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.supports.IFileUtils
import java.lang.RuntimeException
import javax.inject.Inject

interface DeleteBookUseCase {
    fun execute(bookId: String): Observable<DeletingResult>
}

class DeleteBookUseCaseImpl @Inject constructor(
    private val fileUtils: IFileUtils,
    private val bookRepository: BookRepository
) : DeleteBookUseCase {
    override fun execute(bookId: String): Observable<DeletingResult> {
        return bookRepository.getDownloadedBookCoverPath(bookId)
            .zipWith(bookRepository.getDownloadedBookImagePaths(bookId))
            .flatMap { (cover, imagePaths) ->
                bookRepository.getDownloadedBookThumbnailPaths(listOf(bookId))
                    .map { thumbnails ->
                        Triple(cover, imagePaths, thumbnails.map { it.second })
                    }
            }.flatMapObservable { (cover, imagePaths, thumbnails) ->
                bookRepository.deleteBook(bookId)
                    .andThen(deleteBookImages(bookId, cover, thumbnails, imagePaths))
            }
    }

    private fun deleteBookImages(
        bookId: String, cover: String, thumbnails: List<String>, imagePaths: List<String>
    ): Observable<DeletingResult> {
        return Observable.fromCallable {
            val total = imagePaths.size + 1
            imagePaths.toMutableList().apply {
                add(0, cover)
                addAll(thumbnails)
            }.mapIndexed { index, path ->
                Triple(index, total, path)
            }
        }.flatMap {
            Observable.fromIterable(it)
        }.map { (index, total, imagePath) ->
            Logger.d(TAG, "Deleting $index/$total")
            try {
                if (fileUtils.deleteFile(imagePath)) {
                    DeletingProgress(bookId, index, total)
                } else {
                    val error = RuntimeException("Cannot delete file $imagePath")
                    DeletingFailure(bookId, imagePath, error)
                }
            } catch (exception: Exception) {
                DeletingFailure(bookId, imagePath, exception)
            }
        }.doOnComplete {
            fileUtils.deleteParentDirectory(cover)
            val imageList = imagePaths.toMutableList().apply {
                add(0, cover)
                addAll(thumbnails)
            }
            fileUtils.refreshGallery(true, *imageList.toTypedArray())
        }
    }

    companion object {
        private const val TAG = "DeleteBookUseCase"
    }
}
