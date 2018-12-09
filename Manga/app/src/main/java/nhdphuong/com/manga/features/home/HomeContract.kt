package nhdphuong.com.manga.features.home

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book

/*
 * Created by nhdphuong on 3/18/18.
 */
interface HomeContract {
    interface View : Base.View<Presenter> {
        fun setUpHomeBookList(homeBookList: List<Book>)
        fun refreshHomeBookList()
        fun refreshHomePagination(pageCount: Long)
        fun showNothingView(isEmpty: Boolean)
        fun showLastBookListRefreshTime(lastRefreshTimeStamp: String)
        fun showRefreshingDialog()
        fun showRecentBooks(recentList: List<Int>)
        fun showFavoriteBooks(favoriteList: List<Int>)
        fun changeSearchResult(data: String)
        fun showRandomBook(randomBook: Book)
    }

    interface Presenter : Base.Presenter {
        fun jumpToPage(pageNumber: Long)
        fun jumToFirstPage()
        fun jumToLastPage()
        fun reloadCurrentPage(onRefreshed: () -> Unit)
        fun reloadLastBookListRefreshTime()
        fun reloadRecentBooks()
        fun saveLastBookListRefreshTime()
        fun updateSearchData(data: String)
        fun pickBookRandomly()
    }
}