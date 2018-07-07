package nhdphuong.com.manga.features.preview

import dagger.Subcomponent
import nhdphuong.com.manga.scope.ActivityScope

/*
 * Created by nhdphuong on 4/14/18.
 */
@ActivityScope
@Subcomponent(modules = [BookPreviewModule::class])
interface BookPreviewComponent {
    fun inject(bookPreviewActivity: BookPreviewActivity)
}