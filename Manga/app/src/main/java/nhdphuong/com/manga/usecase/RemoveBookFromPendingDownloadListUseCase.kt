package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface RemoveBookFromPendingDownloadListUseCase {
    fun execute(bookId: String): Completable
}

class RemoveBookFromPendingDownloadListUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : RemoveBookFromPendingDownloadListUseCase {
    override fun execute(bookId: String): Completable {
        return Completable.fromCallable {
            bookRepository.removeBookFromPendingDownloadList(bookId)
        }
    }
}
