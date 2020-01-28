package nhdphuong.com.manga

import dagger.Component
import nhdphuong.com.manga.data.RepositoryModule
import nhdphuong.com.manga.features.admin.AdminComponent
import nhdphuong.com.manga.features.admin.AdminModule
import nhdphuong.com.manga.features.downloaded.DownloadedBooksComponent
import nhdphuong.com.manga.features.downloaded.DownloadedBooksModule
import nhdphuong.com.manga.features.header.HeaderModule
import nhdphuong.com.manga.features.home.HomeComponent
import nhdphuong.com.manga.features.home.HomeModule
import nhdphuong.com.manga.features.preview.BookPreviewComponent
import nhdphuong.com.manga.features.preview.BookPreviewModule
import nhdphuong.com.manga.features.reader.ReaderComponent
import nhdphuong.com.manga.features.reader.ReaderModule
import nhdphuong.com.manga.features.recent.RecentComponent
import nhdphuong.com.manga.features.recent.RecentModule
import nhdphuong.com.manga.features.tags.TagsComponent
import nhdphuong.com.manga.features.tags.TagsModule
import nhdphuong.com.manga.service.BookDownloadingServices
import nhdphuong.com.manga.service.TagsUpdateService
import nhdphuong.com.manga.usecase.UseCaseModule
import javax.inject.Singleton

/*
 * Created by nhdphuong on 3/21/18.
 */
@Singleton
@Component(modules = [ApplicationModule::class, RepositoryModule::class, UseCaseModule::class])
interface ApplicationComponent {
    fun plus(homeModule: HomeModule, headerModule: HeaderModule): HomeComponent
    fun plus(bookPreviewModule: BookPreviewModule): BookPreviewComponent
    fun plus(readerModule: ReaderModule): ReaderComponent
    fun plus(tagsModule: TagsModule, headerModule: HeaderModule): TagsComponent
    fun plus(recentModule: RecentModule): RecentComponent
    fun plus(adminModule: AdminModule): AdminComponent
    fun plus(downloadedBooksModule: DownloadedBooksModule): DownloadedBooksComponent

    fun inject(service: TagsUpdateService)
    fun inject(bookDownloadingServices: BookDownloadingServices)
    fun inject(nHentaiApp: NHentaiApp)
}