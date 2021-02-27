package nhdphuong.com.manga.usecase

import io.reactivex.Maybe
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.entity.RecommendBookResponse
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetRecommendedBooksFromReadingHistoryUseCase {
    fun execute(): Maybe<List<Book>>
}

class GetRecommendedBooksFromReadingHistoryUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetRecommendedBooksFromReadingHistoryUseCase {
    private val logger: Logger by lazy {
        Logger("GetRecommendedBooksFromReadingHistoryUseCase")
    }

    override fun execute(): Maybe<List<Book>> {
        return bookRepository.getRecentBookIdsForRecommendation()
            .flatMapMaybe {
                if (it.isEmpty()) Maybe.empty() else getRecommendation(it)
            }
    }

    private fun getRecommendation(recentIds: List<String>): Maybe<List<Book>> {
        logger.d("Found ${recentIds.size} recent IDS")
        return Maybe.create { emitter ->
            val result = arrayListOf<Book>()
            try {
                runBlocking {
                    val notRecommendedIds = arrayListOf<String>().apply {
                        addAll(recentIds)
                        addAll(bookRepository.getAllFavoriteBookIds())
                        addAll(bookRepository.getBlockedBookIds())
                    }.distinct()
                    recentIds.take(MAX_RECENT_IDS).shuffled().forEach { valuableRecentId ->
                        logger.d("Load recommendation of book $valuableRecentId")
                        val bookResponse = bookRepository.getRecommendBook(valuableRecentId)
                        if (bookResponse is RecommendBookResponse.Success) {
                            result.addAll(bookResponse.recommendBook.bookList.filterNot {
                                notRecommendedIds.contains(it.bookId)
                            })
                        }
                    }
                }
                logger.d("Result: ${result.size} books")
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
        private const val MAX_RECENT_IDS = 50
        private const val MAX_RECOMMENDED_BOOKS = 5
    }
}