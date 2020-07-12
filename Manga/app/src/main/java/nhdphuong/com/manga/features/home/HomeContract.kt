package nhdphuong.com.manga.features.home

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.enum.ErrorEnum

/*
 * Created by nhdphuong on 3/18/18.
 */
interface HomeContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun setUpHomeBookList(homeBookList: List<Book>)
        fun refreshHomeBookList()
        fun refreshHomePagination(pageCount: Long)
        fun showNothingView(isEmpty: Boolean)
        fun enableSortOption(sortOption: SortOption)
        fun showSortOptionList()
        fun hideSortOptionList()
        fun showLastBookListRefreshTime(lastRefreshTimeStamp: String)
        fun showRefreshingDialog()
        fun showRecentBooks(recentList: List<String>)
        fun showFavoriteBooks(favoriteList: List<String>)
        fun changeSearchResult(data: String)
        fun showBookPreview(book: Book)
        fun startUpdateTagsService()
        fun showUpgradeNotification()
        fun updateErrorMessage(errorEnum: ErrorEnum)
    }

    interface Presenter : Base.Presenter {
        fun jumpToPage(pageNumber: Long)
        fun jumToFirstPage()
        fun jumToLastPage()
        fun setNewerVersionAcknowledged()
        fun refreshAppVersion()
        fun reloadCurrentPage(onRefreshed: () -> Unit)
        fun reloadLastBookListRefreshTime()
        fun reloadRecentBooks()
        fun saveLastBookListRefreshTime()
        fun updateSearchData(data: String)
        fun pickBookRandomly()
        fun updateSortOption(sortOption: SortOption)
    }
}
