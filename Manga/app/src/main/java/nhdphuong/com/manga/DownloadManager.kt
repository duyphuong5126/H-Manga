package nhdphuong.com.manga

import java.util.concurrent.atomic.AtomicBoolean

/*
 * Created by nhdphuong on 6/2/18.
 */
class DownloadManager {
    companion object {
        private val logger: Logger by lazy {
            Logger("DownloadManager")
        }

        data class BookDownloadingProgress(var progress: Int, val total: Int) {
            val percentageLabel: String
                get() {
                    val percentage = if (total <= 0) 0.0 else (progress * 1.0) / total
                    return (percentage * 100).toInt().toString()
                }
        }

        object BookDownloader {
            private val progressMap = HashMap<String, BookDownloadingProgress>()

            private val hasDownloadingBook = AtomicBoolean(false)
            val isDownloading: Boolean
                get() = hasDownloadingBook.get()

            private val callbacks = arrayListOf<BookDownloadCallback>()

            private var beingDownloadedBookId: String? = null
            val downloadingBookId: String? get() = beingDownloadedBookId

            val currentProgressStatus: BookDownloadingProgress?
                get() = if (beingDownloadedBookId != null) {
                    progressMap[beingDownloadedBookId]?.let {
                        BookDownloadingProgress(it.progress, it.total)
                    }
                } else null


            fun startDownloading(bookId: String, total: Int) {
                val isDownloadingBook = progressMap.containsKey(bookId)
                val isNotDownloading = hasDownloadingBook.compareAndSet(false, true)
                if (!isDownloadingBook && isNotDownloading) {
                    beingDownloadedBookId = bookId
                    progressMap[bookId] = BookDownloadingProgress(0, total)
                    callbacks.forEach {
                        it.onDownloadingStarted(bookId, total)
                    }
                }
            }

            fun updateProgress(bookId: String, progress: Int) {
                beingDownloadedBookId = bookId
                progressMap[bookId]?.apply {
                    this.progress = progress
                    callbacks.forEach {
                        it.onProgressUpdated(bookId, progress, this.total)
                    }
                }
            }

            fun endDownloading(bookId: String) {
                val lastProgress = progressMap.remove(bookId)
                lastProgress?.run {
                    callbacks.forEach {
                        it.onDownloadingEnded(bookId, progress, total)
                    }
                }
                beingDownloadedBookId = null
                hasDownloadingBook.compareAndSet(true, false)
            }

            fun endDownloadingWithError(bookId: String) {
                val lastProgress = progressMap.remove(bookId)
                lastProgress?.run {
                    callbacks.forEach {
                        it.onDownloadingEndedWithError(bookId, total - progress, total)
                    }
                }
                beingDownloadedBookId = null
                hasDownloadingBook.compareAndSet(true, false)
            }

            fun addDownloadCallback(downloadCallback: BookDownloadCallback) {
                callbacks.add(downloadCallback)
            }

            fun removeDownloadCallback(downloadCallback: BookDownloadCallback) {
                callbacks.remove(downloadCallback)
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
