package nhdphuong.com.manga.features.downloading

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel

interface DownloadingBooksContract {
    interface View : Base.View<Presenter> {
        fun setUpPendingDownloadList(pendingDownloadList: List<PendingDownloadItemUiModel>)
        fun updatePendingList(newPendingDownloadList: List<PendingDownloadItemUiModel>)
        fun showNothingView()
        fun hideNothingView()
        fun updateDownloadingStarted(position: Int)
        fun updateProgress(position: Int, progress: Int, total: Int)
        fun updateCompletion(position: Int)
        fun updateFailure(position: Int, failureCount: Int, total: Int)
    }

    interface Presenter : Base.Presenter {
        fun removePendingItem(bookId: String)
        fun updateDownloadingStarted(bookId: String)
        fun updateDownloadingProgress(bookId: String, progress: Int, total: Int)
        fun updateDownloadingCompleted(bookId: String)
        fun updateDownloadFailure(bookId: String, failureCount: Int, total: Int)
    }
}