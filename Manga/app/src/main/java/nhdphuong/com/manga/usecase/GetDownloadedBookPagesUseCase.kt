package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetDownloadedBookPagesUseCase {
    fun execute(bookId: String): Single<List<String>>
}

class GetDownloadedBookPagesUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetDownloadedBookPagesUseCase {
    override fun execute(bookId: String): Single<List<String>> {
        return bookRepository.getDownloadedBookImagePaths(bookId)
    }
}