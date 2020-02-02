package nhdphuong.com.manga.features.recent

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book

/*
 * Created by nhdphuong on 6/10/18.
 */
interface RecentContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun setUpRecentBookList(recentBookList: List<Book>)
        fun refreshRecentBookList()
        fun refreshRecentPagination(pageCount: Int)
        fun showRecentBooks(recentList: List<String>)
        fun showFavoriteBooks(favoriteList: List<String>)
        fun showLastBookListRefreshTime(lastRefreshTimeStamp: String)
        fun showNothingView(@RecentType recentType: String)
    }

    interface Presenter : Base.Presenter {
        fun setType(@RecentType recentType: String)
        fun reloadRecentMarks()
        fun jumpToPage(pageNumber: Int)
        fun jumToFirstPage()
        fun jumToLastPage()
        fun reloadLastBookListRefreshTime()
        fun saveLastBookListRefreshTime()
    }
}