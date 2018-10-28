package nhdphuong.com.manga.features.admin

import dagger.Module
import dagger.Provides

@Module
class AdminModule (private val mView: AdminContract.View) {
    @Provides
    fun providesView(): AdminContract.View = mView
}