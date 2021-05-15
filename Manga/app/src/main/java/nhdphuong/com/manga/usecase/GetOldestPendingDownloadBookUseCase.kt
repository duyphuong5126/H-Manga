package nhdphuong.com.manga.usecase

import io.reactivex.Maybe
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetOldestPendingDownloadBookUseCase {
    fun execute(): Maybe<Book>
}

class GetOldestPendingDownloadBookUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetOldestPendingDownloadBookUseCase {
    override fun execute(): Maybe<Book> {
        return bookRepository.getOldestPendingDownloadBook()
    }
}
