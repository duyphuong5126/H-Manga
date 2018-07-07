package nhdphuong.com.manga.features.recent

import android.support.annotation.NonNull
import dagger.Module
import dagger.Provides

/*
 * Created by nhdphuong on 6/10/18.
 */
@Module
class RecentModule(private val mRecentView: RecentContract.View) {
    @NonNull
    @Provides
    fun providesRecentView(): RecentContract.View = mRecentView
}