package nhdphuong.com.manga.usecase

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import nhdphuong.com.manga.data.SerializationService
import nhdphuong.com.manga.data.entity.BookResponse.Success
import nhdphuong.com.manga.data.entity.BookResponse.Failure
import nhdphuong.com.manga.data.entity.FavoriteBook
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.RecentFavoriteMigrationResult
import nhdphuong.com.manga.data.entity.RecentFavoriteMigrationResult.MigratedBook
import nhdphuong.com.manga.data.entity.RecentFavoriteMigrationResult.BookMigrationError
import nhdphuong.com.manga.data.repository.BookRepository
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class RecentFavoriteMigrationUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository,
    private val serializationService: SerializationService
) : RecentFavoriteMigrationUseCase {
    override fun execute(): Observable<RecentFavoriteMigrationResult> {
        val progress = AtomicInteger(0)
        val failedItems = AtomicInteger(0)
        val total = AtomicInteger(0)
        return getFavoriteBookMigrationData()
            .zipWith(getRecentBookMigrationData())
            .flatMapObservable { (favoriteIds, recentIds) ->
                val result = arrayListOf<MigrationData>()
                arrayListOf<String>().apply {
                    addAll(recentIds)
                    addAll(favoriteIds)
                }.distinct().map { bookId ->
                    val isRecent = recentIds.firstOrNull { it == bookId } != null
                    val isFavorite = favoriteIds.firstOrNull { it == bookId } != null
                    MigrationData(bookId, isRecent, isFavorite)
                }.let(result::addAll)

                total.compareAndSet(0, result.size)
                Observable.fromIterable(result)
            }.map {
                val bookId = it.bookId
                when (val bookDetailsResponse =
                    bookRepository.getBookDetailsSynchronously(it.bookId)) {
                    is Success -> {
                        val serializedBook = serializationService.serialize(
                            bookDetailsResponse.book
                        )
                        if (it.isInFavoriteList) {
                            bookRepository.updateRawFavoriteBook(bookId, serializedBook)
                        }
                        if (it.isInRecentList) {
                            bookRepository.updateRawRecentBook(bookId, serializedBook)
                        }
                        MigratedBook(progress.incrementAndGet(), total.get())
                    }

                    is Failure -> BookMigrationError(failedItems.incrementAndGet(), total.get())
                }
            }
    }

    private fun getRecentBookMigrationData(): Single<List<String>> {
        return bookRepository.getEmptyRecentBooks().map {
            it.map(RecentBook::bookId)
        }
    }

    private fun getFavoriteBookMigrationData(): Single<List<String>> {
        return bookRepository.getEmptyFavoriteBooks().map {
            it.map(FavoriteBook::bookId)
        }
    }

    private class MigrationData(
        val bookId: String,
        val isInRecentList: Boolean,
        val isInFavoriteList: Boolean
    )
}