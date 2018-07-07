package nhdphuong.com.manga.features.recent

import dagger.Subcomponent

/*
 * Created by nhdphuong on 6/16/18.
 */
@Subcomponent(modules = [RecentModule::class])
interface RecentComponent {
    fun inject(recentActivity: RecentActivity)
}