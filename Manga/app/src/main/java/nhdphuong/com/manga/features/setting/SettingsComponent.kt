package nhdphuong.com.manga.features.setting

import dagger.Subcomponent

@Subcomponent(modules = [SettingsModule::class])
interface SettingsComponent {
    fun inject(settingsActivity: SettingsActivity)
}