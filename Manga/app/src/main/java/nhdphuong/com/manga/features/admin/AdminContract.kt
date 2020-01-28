package nhdphuong.com.manga.features.admin

import nhdphuong.com.manga.Base

interface AdminContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun showRequestStoragePermission()
        fun updateProgress()
        fun showNumberOfPages(numOfPages: Long)
        fun updateDownloadingStatistics(
            downloadedPages: Long = 0,
            artists: Int = 0,
            characters: Int = 0,
            categories: Int = 0,
            languages: Int = 0,
            parodies: Int = 0,
            groups: Int = 0,
            tags: Int = 0,
            unknownsTypes: Int = 0
        )

        fun restartApp()
    }

    interface Presenter : Base.Presenter {
        fun startDownloading()
        fun toggleCensored(censored: Boolean)
    }
}
