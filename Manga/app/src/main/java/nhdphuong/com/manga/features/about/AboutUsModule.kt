package nhdphuong.com.manga.features.about

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.api.InstallationApiService
import nhdphuong.com.manga.data.repository.MasterDataRepository
import nhdphuong.com.manga.data.repository.installation.InstallationRemoteDataSource
import nhdphuong.com.manga.data.repository.installation.InstallationRemoteDataSourceImpl
import nhdphuong.com.manga.data.repository.installation.InstallationRepository
import nhdphuong.com.manga.data.repository.installation.InstallationRepositoryImpl
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.usecase.GetVersionCodeUseCase

@Module
class AboutUsModule(private val aboutView: AboutUsContract.View) {

    @Provides
    fun providesView(): AboutUsContract.View = aboutView

    @Provides
    fun providesInstallationRemoteDataSource(installationApiService: InstallationApiService): InstallationRemoteDataSource {
        return InstallationRemoteDataSourceImpl(installationApiService)
    }

    @Provides
    fun providesInstallationRepository(installationRemoteDataSource: InstallationRemoteDataSource): InstallationRepository {
        return InstallationRepositoryImpl(installationRemoteDataSource)
    }

    @Provides
    fun providesPresenter(
        getVersionCodeUseCase: GetVersionCodeUseCase,
        aboutView: AboutUsContract.View,
        masterDataRepository: MasterDataRepository,
        installationRepository: InstallationRepository,
        sharedPreferencesManager: SharedPreferencesManager,
        @IO io: CoroutineScope,
        @Main main: CoroutineScope
    ): AboutUsContract.Presenter {
        return AboutUsPresenter(
            aboutView,
            getVersionCodeUseCase,
            masterDataRepository,
            installationRepository,
            sharedPreferencesManager,
            io,
            main
        )
    }
}
