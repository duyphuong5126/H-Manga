package nhdphuong.com.manga.features.downloaded

import dagger.Subcomponent

@Subcomponent(modules = [DownloadedBooksModule::class])
interface DownloadedBooksComponent {
    fun inject(downloadedBooksActivity: DownloadedBooksActivity)
}