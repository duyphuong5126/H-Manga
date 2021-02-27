package nhdphuong.com.manga.usecase

import io.reactivex.Maybe
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.data.entity.RecommendBookResponse.Success
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetRecommendedBooksFromFavoriteUseCase {
    fun execute(): Maybe<List<Book>>
}

class GetRecommendedBooksFromFavoriteUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetRecommendedBooksFromFavoriteUseCase {
    override fun execute(): Maybe<List<Book>> {
        return Maybe.create { emitter ->
            try {
                val result = arrayListOf<Book>()
                runBlocking {
                    val notRecommendedIds = arrayListOf<String>().apply {
                        addAll(bookRepository.getAllRecentBookIds())
                        addAll(bookRepository.getBlockedBookIds())
                    }.distinct()
                    val favoriteIds = bookRepository.getAllFavoriteBookIds().shuffled()
                    if (favoriteIds.isNotEmpty()) {
                        favoriteIds.take(MAX_FAVORITES).forEach { favoriteId ->
                            val bookResponse = bookRepository.getRecommendBook(favoriteId)
                            if (bookResponse is Success) {
                                result.addAll(bookResponse.recommendBook.bookList.filterNot {
                                    notRecommendedIds.contains(it.bookId)
                                })
                            }
                        }
                    }
                }
                if (result.isEmpty()) {
                    emitter.onComplete()
                } else {
                    result.sortByDescending { it.numOfFavorites }
                    emitter.onSuccess(result.take(MAX_RECOMMENDED_BOOKS))
                }
            } catch (throwable: Throwable) {
                emitter.onError(throwable)
            }
        }
    }

    companion object {
        private const val MAX_FAVORITES = 50
        private const val MAX_RECOMMENDED_BOOKS = 5
    }
}