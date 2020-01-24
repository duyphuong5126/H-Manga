package nhdphuong.com.manga.usecase

import dagger.Binds
import dagger.Module

@Module
interface UseCaseModule {
    @Binds
    fun downloadBookUseCase(downloadBookUseCaseImpl: DownloadBookUseCaseImpl): DownloadBookUseCase
}