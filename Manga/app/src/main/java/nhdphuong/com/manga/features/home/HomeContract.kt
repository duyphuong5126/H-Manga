package nhdphuong.com.manga.features.home

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.entity.notification.NotificationContent
import nhdphuong.com.manga.enum.ErrorEnum

/*
 * Created by nhdphuong on 3/18/18.
 */
interface HomeContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun setUpHomeBookList(homeBookList: List<Book>)
        fun showRecommendBooks(bookList: List<Book>)
        fun hideRecommendBooks()
        fun refreshRecommendBooks()
        fun refreshHomeBookList()
        fun refreshHomePagination(pageCount: Long, currentFocusedIndex: Int)
        fun showNothingView()
        fun hideNothingView()
        fun enableSortOption(sortOption: SortOption)
        fun showSortOptionList()
        fun hideSortOptionList()
        fun showLastBookListRefreshTime(lastRefreshTimeStamp: String)
        fun showRefreshingDialog()
        fun showRecentBooks(recentList: List<String>)
        fun showFavoriteBooks(favoriteList: List<String>)
        fun showFavoriteRecommendedBooks(favoriteList: List<String>)
        fun changeSearchInfo(data: String)
        fun showBookPreview(book: Book)
        fun startUpdateTagsService()
        fun showUpgradeNotification(latestVersionCode: String)
        fun updateErrorMessage(errorEnum: ErrorEnum)
        fun finishRefreshing()
        fun showAlternativeDomainsQuestion()
        fun startRecentFavoriteMigration()
        fun showNotification(notificationContent: NotificationContent)
        fun invalidated()
    }

    interface Presenter : Base.Presenter {
        fun jumpToPage(pageNumber: Long)
        fun jumToFirstPage()
        fun jumToLastPage()
        fun setNewerVersionAcknowledged()
        fun reloadIfEmpty()
        fun refreshAppVersion()
        fun reloadCurrentPage()
        fun reloadLastBookListRefreshTime()
        fun reloadRecentBooks()
        fun saveLastBookListRefreshTime()
        fun updateSearchData(data: String)
        fun pickBookRandomly()
        fun updateSortOption(sortOption: SortOption)
        fun checkedOutAlternativeDomains()
        fun doNoRecommendBook(bookId: String)
        fun checkOutRecommendedBook(bookId: String)
        fun checkAndResumeBookDownloading()
        fun addFavoriteRecommendedBook(book: Book)
        fun removeFavoriteRecommendedBook(book: Book)
        fun openBook(bookId: String)
    }
}
