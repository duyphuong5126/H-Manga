package nhdphuong.com.manga.features.downloading

import dagger.Subcomponent

@Subcomponent(modules = [DownloadingBooksModule::class])
interface DownloadingBooksComponent {
    fun inject(downloadingBooksActivity: DownloadingBooksActivity)
}