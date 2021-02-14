package nhdphuong.com.manga.usecase

import io.reactivex.Maybe
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.entity.RemoteBookResponse.Success
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.SortOption.Recent
import nhdphuong.com.manga.data.entity.book.SortOption.PopularWeek
import nhdphuong.com.manga.data.entity.book.SortOption.PopularToday
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.data.repository.SearchRepository
import javax.inject.Inject
import kotlin.random.Random

interface GetRecommendedBooksUseCase {
    fun execute(dayOfWeek: Int, excludedSearchInfo: String): Maybe<List<Book>>
}

class GetRecommendedBooksUseCaseImpl @Inject constructor(
    private val searchRepository: SearchRepository,
    private val bookRepository: BookRepository
) : GetRecommendedBooksUseCase {
    private val logger = Logger("GetTodayRecommendedBooksUseCase")

    override fun execute(dayOfWeek: Int, excludedSearchInfo: String): Maybe<List<Book>> {
        return searchRepository.getMostUsedSearchEntries(MAX_SEARCH_ENTRIES)
            .flatMap { mostUsedSearchList ->
                bookRepository.getMostUsedTags(MAX_USED_TAGS).map {
                    val result = arrayListOf<String>()
                    result.addAll(mostUsedSearchList)
                    result.addAll(it)
                    if (excludedSearchInfo.isNotBlank()) {
                        result.removeAll { entry -> entry == excludedSearchInfo }
                    }
                    if (result.isNotEmpty()) {
                        result.shuffle()
                    }
                    ArrayList(result.distinct())
                }
            }.map { searchQueries ->
                logger.d("searchQueries=$searchQueries")
                if (searchQueries.isNotEmpty()) {
                    val recommendedBooks = arrayListOf<Book>()
                    runBlocking {
                        val recommendSortOption = if (dayOfWeek >= 6 || dayOfWeek == 1) {
                            if (Random.nextInt() / 2 == 0) PopularToday else PopularWeek
                        } else {
                            if (Random.nextInt() / 2 == 0) Recent else PopularToday
                        }

                        val recentFavoriteIds = arrayListOf<String>().apply {
                            addAll(bookRepository.getAllRecentBookIds())
                            addAll(bookRepository.getAllFavoriteBookIds())
                        }.distinct()

                        var remainSlotCount = MAX_RECOMMENDED_BOOKS

                        while (remainSlotCount > 0 && searchQueries.isNotEmpty()) {
                            val searchQuery = searchQueries.removeAt(0)
                            var currentPage = 1L
                            while (currentPage <= MAX_PAGES_PER_SEARCH_ENTRY && remainSlotCount > 0) {
                                val response = bookRepository.getBookByPage(
                                    searchQuery,
                                    currentPage,
                                    recommendSortOption
                                )
                                if (response is Success) {
                                    response.remoteBook.bookList.filterNot {
                                        recentFavoriteIds.contains(it.bookId)
                                    }.let(recommendedBooks::addAll)
                                    recommendedBooks.sortByDescending { it.numOfFavorites }

                                    remainSlotCount -= recommendedBooks.size
                                }
                                currentPage++
                            }
                        }
                    }
                    recommendedBooks.take(MAX_RECOMMENDED_BOOKS)
                } else emptyList()
            }.flatMapMaybe {
                if (it.isEmpty()) {
                    Maybe.empty()
                } else {
                    Maybe.just(it)
                }
            }
    }

    private companion object {
        private const val MAX_SEARCH_ENTRIES = 50
        private const val MAX_USED_TAGS = 50
        private const val MAX_RECOMMENDED_BOOKS = 5
        private const val MAX_PAGES_PER_SEARCH_ENTRY = 10
    }
}