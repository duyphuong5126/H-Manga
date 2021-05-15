package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface PutBookIntoPendingDownloadListUseCase {
    fun execute(book: Book): Completable
}

class PutBookIntoPendingDownloadListUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : PutBookIntoPendingDownloadListUseCase {
    override fun execute(book: Book): Completable {
        return Completable.fromCallable {
            bookRepository.putBookIntoPendingDownloadList(book)
        }
    }
}