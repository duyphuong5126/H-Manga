package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

class CheckRecentFavoriteMigrationNeededUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : CheckRecentFavoriteMigrationNeededUseCase {
    override fun execute(): Single<Boolean> {
        return Single.create {
            val migrationNeeded = bookRepository.getEmptyFavoriteBooksCount() > 0 ||
                    bookRepository.getEmptyRecentBooksCount() > 0
            it.onSuccess(migrationNeeded)
        }
    }
}