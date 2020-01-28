package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetAvailableBookThumbnailsUseCase {
    fun execute(bookIds: List<String>): Single<List<Pair<String, String>>>
}

class GetAvailableBookThumbnailsUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetAvailableBookThumbnailsUseCase {
    override fun execute(bookIds: List<String>): Single<List<Pair<String, String>>> {
        return bookRepository.getDownloadedBookThumbnailPaths(bookIds)
    }
}