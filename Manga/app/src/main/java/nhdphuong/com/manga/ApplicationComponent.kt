package nhdphuong.com.manga

import dagger.Component
import nhdphuong.com.manga.data.RepositoryModule
import nhdphuong.com.manga.features.about.AboutUsComponent
import nhdphuong.com.manga.features.about.AboutUsModule
import nhdphuong.com.manga.features.admin.AdminComponent
import nhdphuong.com.manga.features.admin.AdminModule
import nhdphuong.com.manga.features.comment.CommentThreadComponent
import nhdphuong.com.manga.features.comment.CommentThreadModule
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
import nhdphuong.com.manga.features.setting.SettingsComponent
import nhdphuong.com.manga.features.setting.SettingsModule
import nhdphuong.com.manga.features.tags.TagsComponent
import nhdphuong.com.manga.features.tags.TagsModule
import nhdphuong.com.manga.service.BookDeletingService
import nhdphuong.com.manga.service.BookDownloadingService
import nhdphuong.com.manga.service.RecentFavoriteMigrationService
import nhdphuong.com.manga.service.TagsDownloadingService
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
    fun plus(commentThreadModule: CommentThreadModule): CommentThreadComponent
    fun plus(settingsModule: SettingsModule): SettingsComponent
    fun plus(aboutUsModule: AboutUsModule): AboutUsComponent

    fun inject(service: TagsUpdateService)
    fun inject(bookDownloadingService: BookDownloadingService)
    fun inject(recentFavoriteMigrationService: RecentFavoriteMigrationService)
    fun inject(bookDeletingService: BookDeletingService)
    fun inject(tagsDownloadingService: TagsDownloadingService)
    fun inject(nHentaiApp: NHentaiApp)
}
