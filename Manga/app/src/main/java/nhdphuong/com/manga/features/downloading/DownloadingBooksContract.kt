package nhdphuong.com.manga.features.downloading

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel
import nhdphuong.com.manga.features.downloading.uimodel.PendingItemStatus

interface DownloadingBooksContract {
    interface View : Base.View<Presenter> {
        fun setUpPendingDownloadList(pendingDownloadList: List<PendingDownloadItemUiModel>)
        fun updatePendingList(newPendingDownloadList: List<PendingDownloadItemUiModel>)
        fun showNothingView()
        fun hideNothingView()
        fun updateStatus(itemStatus: PendingItemStatus)
        val isReady: Boolean
    }

    interface Presenter : Base.Presenter {
        fun removePendingItem(bookId: String)
        fun updateDownloadingStarted(bookId: String)
        fun updateDownloadingProgress(bookId: String, progress: Int, total: Int)
        fun updateDownloadingCompleted(bookId: String)
        fun updateDownloadFailure(bookId: String, failureCount: Int, total: Int)
        fun cleanUpPendingStatuses()
    }
}