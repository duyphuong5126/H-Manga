package nhdphuong.com.manga.data.entity

sealed class DownloadingResult {
    data class DownloadingProgress(val progress: Int, val total: Int) : DownloadingResult()
    data class DownloadingFailure(val fileUrl: String, val error: Throwable) : DownloadingResult()
    object DownloadingCompleted : DownloadingResult()
}