package nhdphuong.com.manga.features.recent

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.enum.ErrorEnum

/*
 * Created by nhdphuong on 6/10/18.
 */
interface RecentContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun setUpRecentBookList(recentBookList: List<Book>)
        fun setUpRecommendedBookList(recommendedBookList: List<Book>)
        fun refreshRecentBookList()
        fun refreshRecentPagination(pageCount: Int)
        fun showRecentBooks(recentList: List<String>)
        fun showFavoriteBooks(favoriteList: List<String>)
        fun showLastBookListRefreshTime(lastRefreshTimeStamp: String)
        fun showNothingView(@RecentType recentType: String)
        fun updateErrorMessage(errorEnum: ErrorEnum)
        fun showRecentRecommendedBooks(recentList: List<String>)
        fun showFavoriteRecommendedBooks(favoriteList: List<String>)
        fun hideRecommendedList()
        fun showRecommendedList()
    }

    interface Presenter : Base.Presenter {
        fun setType(@RecentType recentType: String)
        fun reloadRecentMarks()
        fun jumpToPage(pageNumber: Int)
        fun jumToFirstPage()
        fun jumToLastPage()
        fun reloadLastBookListRefreshTime()
        fun saveLastBookListRefreshTime()
        fun syncRecommendedList()
        fun doNoRecommendBook(bookId: String)
        fun checkOutRecommendedBook(bookId: String)
        fun addFavoriteRecommendedBook(book: Book)
        fun removeFavoriteRecommendedBook(book: Book)
    }
}
