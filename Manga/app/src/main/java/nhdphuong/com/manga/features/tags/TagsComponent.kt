package nhdphuong.com.manga.features.tags

import dagger.Subcomponent
import nhdphuong.com.manga.features.header.HeaderModule
import nhdphuong.com.manga.scope.ActivityScope

/*
 * Created by nhdphuong on 5/12/18.
 */
@ActivityScope
@Subcomponent(modules = [TagsModule::class, HeaderModule::class])
interface TagsComponent {
    fun inject(tagsActivity: TagsActivity)
}