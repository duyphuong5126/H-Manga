package nhdphuong.com.manga

/*
 * Created by nhdphuong on 6/2/18.
 */
class DownloadManager {
    companion object {
        private val logger: Logger by lazy {
            Logger("DownloadManager")
        }

        object BookDownloader {
            private var mBookId: String = ""
                set(value) {
                    if (!isDownloading) {
                        field = value
                    }
                }
            val bookId: String
                get() = mBookId


            private var mTotal: Int = 0
                set(value) {
                    if (value >= 0) {
                        field = value
                    }
                }
            val total: Int
                get() = mTotal

            private var mProgress: Int = 0
                set(value) {
                    if (value >= 0) {
                        field = value
                    }
                }
            val progress: Int
                get() = mProgress

            val isDownloading: Boolean
                get() = mTotal > 0 && ((mProgress * 1f) / (mTotal * 1f)) < 1.0

            private var mDownloadCallback: BookDownloadCallback? = null

            fun startDownloading(bookId: String, total: Int) {
                if (mBookId != bookId && !isDownloading) {
                    mBookId = bookId
                    mTotal = total
                    mDownloadCallback?.onDownloadingStarted(bookId, total)
                }
            }

            fun updateProgress(bookId: String, progress: Int) {
                if (mBookId == bookId && isDownloading) {
                    mProgress = progress
                    mDownloadCallback?.onProgressUpdated(bookId, progress, total)
                }
            }

            fun endDownloading(bookId: String, downloaded: Int, total: Int) {
                mTotal = 0
                mProgress = 0
                mBookId = ""
                mDownloadCallback?.onDownloadingEnded(bookId, downloaded, total)
                mDownloadCallback = null
            }

            fun endDownloadingWithError(bookId: String, downloaded: Int, total: Int) {
                mTotal = 0
                mProgress = 0
                mBookId = ""
                mDownloadCallback?.onDownloadingEndedWithError(bookId, total - downloaded, total)
                mDownloadCallback = null
            }

            fun setDownloadCallback(downloadCallback: BookDownloadCallback) {
                mDownloadCallback = downloadCallback
            }
        }

        object TagsDownloadManager {
            @Volatile
            private var mIsTagsDownloading: Boolean = false
            val isTagDownloading
                get() = mIsTagsDownloading

            fun startDownloading() {
                if (!isTagDownloading) {
                    mIsTagsDownloading = true
                } else {
                    logger.d("A downloading process right now, cannot start another one")
                }
            }

            fun stopDownloading() {
                if (isTagDownloading) {
                    mIsTagsDownloading = false
                } else {
                    logger.d("No downloading process right now, nothing can be stopped")
                }
            }
        }
    }

    interface BookDownloadCallback {
        fun onDownloadingStarted(bookId: String, total: Int)
        fun onProgressUpdated(bookId: String, progress: Int, total: Int)
        fun onDownloadingEnded(bookId: String, downloaded: Int, total: Int)
        fun onDownloadingEndedWithError(bookId: String, downloadingFailedCount: Int, total: Int)
    }
}
