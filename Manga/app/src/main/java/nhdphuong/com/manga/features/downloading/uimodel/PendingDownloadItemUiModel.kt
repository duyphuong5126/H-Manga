package nhdphuong.com.manga.features.downloading.uimodel

sealed class PendingDownloadItemUiModel {
    data class PendingItemUiModel(
        val bookId: String,
        val bookTitle: String
    ) : PendingDownloadItemUiModel()

    data class TotalCountItem(var total: Int) : PendingDownloadItemUiModel()
}
