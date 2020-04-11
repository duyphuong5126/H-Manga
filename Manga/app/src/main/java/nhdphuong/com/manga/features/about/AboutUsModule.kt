package nhdphuong.com.manga.features.about

import dagger.Module
import dagger.Provides
import nhdphuong.com.manga.usecase.GetVersionCodeUseCase

@Module
class AboutUsModule(private val aboutView: AboutUsContract.View) {

    @Provides
    fun providesView(): AboutUsContract.View = aboutView

    @Provides
    fun providesPresenter(
        getVersionCodeUseCase: GetVersionCodeUseCase,
        aboutView: AboutUsContract.View
    ): AboutUsContract.Presenter {
        return AboutUsPresenter(aboutView, getVersionCodeUseCase)
    }
}
