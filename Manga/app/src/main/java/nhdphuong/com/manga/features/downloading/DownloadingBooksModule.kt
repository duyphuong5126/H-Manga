package nhdphuong.com.manga.features.downloading

import dagger.Module
import dagger.Provides

@Module
class DownloadingBooksModule(private val view: DownloadingBooksContract.View) {

    @Provides
    fun providesDownloadingBooksView(): DownloadingBooksContract.View = view

    @Provides
    fun provides(presenter: DownloadingBooksPresenter): DownloadingBooksContract.Presenter =
        presenter
}