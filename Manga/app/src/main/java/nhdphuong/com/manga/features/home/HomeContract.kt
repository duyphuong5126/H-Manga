package nhdphuong.com.manga.features.home

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book

/*
 * Created by nhdphuong on 3/18/18.
 */
interface HomeContract {
    fun onSearchInputted(data: String)

    interface View : Base.View<Presenter> {
        fun setUpHomeBookList(homeBookList: List<Book>)
        fun refreshHomeBookList()
        fun refreshHomePagination(pageCount: Long)
        fun showNothingView(isEmpty: Boolean)
        fun showLastBookListRefreshTime(lastRefreshTimeStamp: String)
        fun showRefreshingDialog()
        fun showRecentBooks(recentList: List<Int>)
        fun showFavoriteBooks(favoriteList: List<Int>)
        fun changeSearchInputted(data: String)
    }

    interface Presenter : Base.Presenter {
        fun jumpToPage(pageNumber: Int)
        fun jumToFirstPage()
        fun jumToLastPage()
        fun reloadCurrentPage(onRefreshed: () -> Unit)
        fun reloadLastBookListRefreshTime()
        fun reloadRecentBooks()
        fun saveLastBookListRefreshTime()
        fun updateSearchData(data: String)
    }
}