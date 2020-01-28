package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetAllDownloadedBooksUseCase {
    fun execute(): Single<List<Book>>
}

class GetAllDownloadedBooksUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetAllDownloadedBooksUseCase {
    override fun execute(): Single<List<Book>> {
        return bookRepository.getDownloadedBookList()
    }
}