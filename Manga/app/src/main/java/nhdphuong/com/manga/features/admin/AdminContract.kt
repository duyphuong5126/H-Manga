package nhdphuong.com.manga.features.admin

import nhdphuong.com.manga.Base

interface AdminContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
        fun showRequestStoragePermission()
        fun showNumberOfPages(numOfPages: Long)
        fun startDownloadingTagData(numberOfPage: Long)

        fun restartApp()
    }

    interface Presenter : Base.Presenter {
        fun startDownloading()
        fun toggleCensored(censored: Boolean)
    }
}
