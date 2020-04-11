package nhdphuong.com.manga.features.downloaded

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.usecase.GetAllDownloadedBooksUseCase
import nhdphuong.com.manga.usecase.GetAvailableBookThumbnailsUseCase

@Module
class DownloadedBooksModule(private val downloadedBooksView: DownloadedBooksContract.View) {
    @Provides
    fun providesDownloadedBooksView(): DownloadedBooksContract.View = downloadedBooksView

    @Provides
    fun providesDownloadedBooksPresenter(
        view: DownloadedBooksContract.View,
        mBookRepository: BookRepository,
        getAllDownloadedBooksUseCase: GetAllDownloadedBooksUseCase,
        getAvailableBookThumbnailsUseCase: GetAvailableBookThumbnailsUseCase,
        @IO io: CoroutineScope,
        @Main main: CoroutineScope
    ): DownloadedBooksContract.Presenter {
        return DownloadedBooksPresenter(
            view,
            mBookRepository,
            getAllDownloadedBooksUseCase,
            getAvailableBookThumbnailsUseCase,
            io,
            main
        )
    }
}
