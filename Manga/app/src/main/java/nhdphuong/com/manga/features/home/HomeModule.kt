package nhdphuong.com.manga.features.home

import dagger.Module
import dagger.Provides

/*
 * Created by nhdphuong on 3/21/18.
 */
@Module
class HomeModule(private val mHomeView: HomeContract.View) {
    @Provides
    fun provideHomeView(): HomeContract.View = mHomeView
}