package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetDownloadedBookCoverUseCase {
    fun execute(bookId: String): Single<String>
}

class GetDownloadedBookCoverUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetDownloadedBookCoverUseCase {
    override fun execute(bookId: String): Single<String> {
        return bookRepository.getDownloadedBookCoverPath(bookId)
    }
}