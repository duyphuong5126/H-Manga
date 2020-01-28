package nhdphuong.com.manga.features.downloaded

import dagger.Module
import dagger.Provides

@Module
class DownloadedBooksModule(private val downloadedBooksView: DownloadedBooksContract.View) {
    @Provides
    fun providesDownloadedBooksView(): DownloadedBooksContract.View = downloadedBooksView
}