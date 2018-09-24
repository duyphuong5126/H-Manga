package nhdphuong.com.manga.features.reader

import nhdphuong.com.manga.Base

/*
 * Created by nhdphuong on 5/5/18.
 */
interface ReaderContract {
    interface View : Base.View<Presenter> {
        fun showBookTitle(bookTitle: String)
        fun showBookPages(pageList: List<String>)
        fun jumpToPage(pageNumber: Int)
        fun showPageIndicator(pageString: String)
        fun navigateToGallery()
        fun showRequestStoragePermission()
        fun showDownloadPopup()
        fun hideDownloadPopup()
        fun updateDownloadPopupTitle(downloadTitle: String)
        fun removeNotification(notificationId: Int)
        fun pushNowReadingNotification(readingTitle: String, page: Int, total: Int)
    }

    interface Presenter : Base.Presenter {
        fun updatePageIndicator(page: Int)
        fun backToGallery()
        fun downloadCurrentPage()
        fun reloadCurrentPage(onForceReload: (Int) -> Unit)
        fun updateNotificationId(notificationId: Int)
    }
}