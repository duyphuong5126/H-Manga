package nhdphuong.com.manga.features.downloading.uimodel

sealed class PendingItemStatus {
    abstract val position: Int

    data class DownloadingStarted(override val position: Int) : PendingItemStatus()

    data class DownloadingProgress(override val position: Int, val progress: Int, val total: Int) :
        PendingItemStatus()

    data class DownloadingFailed(
        override val position: Int,
        val failureCount: Int,
        val total: Int
    ) : PendingItemStatus()

    data class DownloadingCompleted(override val position: Int) : PendingItemStatus()
}
