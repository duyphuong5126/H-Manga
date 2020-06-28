package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface SaveLastVisitedPageUseCase {
    fun execute(bookId: String, lastVisitedPage: Int): Completable
}

class SaveLastVisitedPageUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : SaveLastVisitedPageUseCase {
    override fun execute(bookId: String, lastVisitedPage: Int): Completable {
        return bookRepository.saveLastVisitedPage(bookId, lastVisitedPage)
    }
}
