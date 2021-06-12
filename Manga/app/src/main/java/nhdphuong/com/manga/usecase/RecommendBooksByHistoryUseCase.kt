package nhdphuong.com.manga.usecase

import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.TagType
import nhdphuong.com.manga.data.entity.TagType.OtherTag
import nhdphuong.com.manga.data.entity.TagType.Artist
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.SortOption.Recent
import nhdphuong.com.manga.data.entity.book.SortOption.PopularToday
import nhdphuong.com.manga.data.entity.book.SortOption.PopularWeek
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.repository.BookRepository
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.random.Random

interface RecommendBooksByHistoryUseCase {
    fun execute(currentBookList: List<Book>, dayOfWeek: Int): Single<List<Book>>
}

class RecommendBooksByHistoryUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : RecommendBooksByHistoryUseCase {
    override fun execute(currentBookList: List<Book>, dayOfWeek: Int): Single<List<Book>> {
        return Single.fromCallable {
            val randomInt = Random.nextInt(1, 11)
            val tagType = if (randomInt % 2 == 0) Artist else OtherTag
            val leftTag = if (randomInt % 2 == 0) OtherTag else Artist
            val isLeftTagUsed = AtomicBoolean(false)

            val recommendedBooks = arrayListOf<Book>()

            val notRecommendedIds = getNotRecommendedBookIds()
            val recommendSortOption = if (dayOfWeek >= 6 || dayOfWeek == 1) {
                if (Random.nextInt() / 2 == 0) PopularToday else PopularWeek
            } else {
                if (Random.nextInt() / 2 == 0) Recent else PopularToday
            }
            var remainSlotCount = MAX_RECOMMENDED_BOOKS
            val searchQueries = arrayListOf<String>()

            searchQueries.addAll(getSearchQueries(currentBookList, tagType))
            searchQueries.shuffle()

            runBlocking {
                while (remainSlotCount > 0 && searchQueries.isNotEmpty()) {
                    val searchQuery = searchQueries.removeAt(0)
                    var currentPage = 1L
                    while (currentPage <= MAX_PAGES_PER_SEARCH_ENTRY && remainSlotCount > 0) {
                        val response = bookRepository.getBookByPage(
                            searchQuery,
                            currentPage,
                            recommendSortOption
                        )
                        if (response is RemoteBookResponse.Success) {
                            val remoteBooks = response.remoteBook.bookList.filterNot {
                                notRecommendedIds.contains(it.bookId)
                            }.sortedByDescending { it.numOfFavorites }.take(remainSlotCount)

                            recommendedBooks.addAll(remoteBooks)
                            remainSlotCount -= remoteBooks.size
                        }
                        currentPage++
                    }

                    val leftTagNotUsed = isLeftTagUsed.compareAndSet(false, true)
                    if (searchQueries.isEmpty() && remainSlotCount > 0 && leftTagNotUsed) {
                        searchQueries.addAll(getSearchQueries(currentBookList, leftTag))
                    }
                }
            }
            recommendedBooks
        }
    }

    private fun getSearchQueries(currentBookList: List<Book>, tagType: TagType): List<String> {
        return currentBookList.map(Book::tags).flatten()
            .filter { it.type == tagType.value }
            .map(Tag::name)
    }

    private fun getNotRecommendedBookIds(): List<String> {
        return runBlocking {
            arrayListOf<String>().apply {
                addAll(bookRepository.getAllRecentBookIds())
                addAll(bookRepository.getAllFavoriteBookIds())
                addAll(bookRepository.getBlockedBookIds())
            }.distinct()
        }
    }

    companion object {
        private const val MAX_RECOMMENDED_BOOKS = 5
        private const val MAX_PAGES_PER_SEARCH_ENTRY = 50
    }
}