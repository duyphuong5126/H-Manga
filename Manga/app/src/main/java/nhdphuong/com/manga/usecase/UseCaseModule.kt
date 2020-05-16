package nhdphuong.com.manga.usecase

import dagger.Binds
import dagger.Module

@Module
interface UseCaseModule {
    @Binds
    fun startBookDownloadingUseCase(startBookDownloadingUseCaseImpl: StartBookDownloadingUseCaseImpl): StartBookDownloadingUseCase

    @Binds
    fun downloadBookUseCase(downloadBookUseCaseImpl: DownloadBookUseCaseImpl): DownloadBookUseCase

    @Binds
    fun startBookDeletingUseCase(startBookDeletingUseCaseImpl: StartBookDeletingUseCaseImpl): StartBookDeletingUseCase

    @Binds
    fun deleteBookUseCase(deleteBookUseCaseImpl: DeleteBookUseCaseImpl): DeleteBookUseCase

    @Binds
    fun getAllDownloadedBooksUseCase(getAllDownloadedBooksUseCase: GetAllDownloadedBooksUseCaseImpl): GetAllDownloadedBooksUseCase

    @Binds
    fun getDownloadedBookCoverUseCase(getDownloadedBookCoverUseCaseImpl: GetDownloadedBookCoverUseCaseImpl): GetDownloadedBookCoverUseCase

    @Binds
    fun getDownloadedBookPagesUseCase(getDownloadedBookPagesUseCaseImpl: GetDownloadedBookPagesUseCaseImpl): GetDownloadedBookPagesUseCase

    @Binds
    fun getAvailableBookThumbnailsUseCase(getAvailableBookThumbnailsUseCaseImpl: GetAvailableBookThumbnailsUseCaseImpl): GetAvailableBookThumbnailsUseCase

    @Binds
    fun getVersionCodeUseCase(getVersionCodeUseCaseImpl: GetVersionCodeUseCaseImpl): GetVersionCodeUseCase
}
