package nhdphuong.com.manga.features.header

import dagger.Module
import dagger.Provides

/*
 * Created by nhdphuong on 4/10/18.
 */
@Module
class HeaderModule(private val mHeaderView: HeaderContract.View) {
    @Provides
    fun providesHeaderView(): HeaderContract.View = mHeaderView
}