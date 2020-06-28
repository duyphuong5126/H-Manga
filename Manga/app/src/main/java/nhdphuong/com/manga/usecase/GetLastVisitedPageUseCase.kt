package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetLastVisitedPageUseCase {
    fun execute(bookId: String): Single<Int>
}

class GetLastVisitedPageUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetLastVisitedPageUseCase {
    override fun execute(bookId: String): Single<Int> {
        return bookRepository.getLastVisitedPage(bookId)
    }
}
