package nhdphuong.com.manga.features.admin

import dagger.Subcomponent
import nhdphuong.com.manga.scope.ActivityScope

@ActivityScope
@Subcomponent(modules = [AdminModule::class])
interface AdminComponent {
    fun inject(adminActivity: AdminActivity)
}