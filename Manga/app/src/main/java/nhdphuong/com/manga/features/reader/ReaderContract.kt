package nhdphuong.com.manga.features.reader

import nhdphuong.com.manga.Base

/*
 * Created by nhdphuong on 5/5/18.
 */
interface ReaderContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun showBookTitle(bookTitle: String)
        fun showBookPages(pageList: List<String>)
        fun jumpToPage(pageNumber: Int)
        fun showPageIndicator(currentPage: Int, total: Int)
        fun showBackToGallery()
        fun navigateToGallery(lastVisitedPage: Int)
        fun showRequestStoragePermission()
        fun showDownloadPopup()
        fun hideDownloadPopup()
        fun updateDownloadPopupTitle(downloadPage: Int)
        fun removeNotification(notificationId: Int)
        fun pushNowReadingNotification(readingTitle: String, page: Int, total: Int)
    }

    interface Presenter : Base.Presenter {
        fun enableViewDownloadedDataMode()
        fun updatePageIndicator(page: Int)
        fun forceBackToGallery()
        fun backToGallery()
        fun downloadCurrentPage()
        fun reloadCurrentPage(onForceReload: (Int) -> Unit)
        fun updateNotificationId(notificationId: Int)
        fun endReading()
    }
}
