package nhdphuong.com.manga.features.about

import dagger.Subcomponent

@Subcomponent(modules = [AboutUsModule::class])
interface AboutUsComponent {
    fun inject(aboutUsActivity: AboutUsActivity)
}
