package nhdphuong.com.manga.features.downloaded

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book

interface DownloadedBooksContract {
    interface View : Base.View<Presenter> {
        fun setUpBookList(bookList: List<Book>)
        fun refreshBookList()
        fun refreshRecentPagination(pageCount: Int)
        fun moveToPage(pageNumber: Int)
        fun showLastBookListRefreshTime(lastRefreshTimeStamp: String)
        fun showRecentBooks(recentList: List<String>)
        fun showFavoriteBooks(favoriteList: List<String>)
        fun refreshThumbnailList(thumbnailList: List<Pair<String, String>>)
        fun showNothingView()
    }

    interface Presenter : Base.Presenter {
        fun reloadLastBookListRefreshTime()
        fun reloadBookMarkers()
        fun reloadBookThumbnails()
        fun jumpToPage(pageNumber: Int)
        fun jumToFirstPage()
        fun jumToLastPage()
        fun notifyBookRemoved(bookId: String)
    }
}
