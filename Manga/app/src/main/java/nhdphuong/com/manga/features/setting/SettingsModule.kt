package nhdphuong.com.manga.features.setting

import dagger.Module
import dagger.Provides

@Module
class SettingsModule(
    private val settingsView: SettingsContract.View
) {
    @Provides
    fun providesSettingsView(): SettingsContract.View = settingsView

    @Provides
    fun providesSettingsPresenter(
        settingsPresenter: SettingsPresenter
    ): SettingsContract.Presenter = settingsPresenter
}