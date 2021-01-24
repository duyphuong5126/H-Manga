package nhdphuong.com.manga.features.reader

import dagger.Subcomponent
import nhdphuong.com.manga.scope.ActivityScope

/*
 * Created by nhdphuong on 5/5/18.
 */
@ActivityScope
@Subcomponent(modules = [ReaderModule::class])
interface ReaderComponent {
    fun inject(readerActivity: ReaderActivity)
    fun inject(readerFragment: ReaderFragment)
}