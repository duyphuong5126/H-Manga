package nhdphuong.com.manga

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.api.BookApiService
import nhdphuong.com.manga.api.TagApiService
import nhdphuong.com.manga.data.local.Database
import nhdphuong.com.manga.data.local.RecentBookDAO
import nhdphuong.com.manga.data.local.TagDAO
import nhdphuong.com.manga.scope.corountine.Default
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.scope.corountine.Unconfined
import nhdphuong.com.manga.supports.ServiceGenerator
import javax.inject.Singleton

/*
 * Created by nhdphuong on 3/21/18.
 */
@Module
class ApplicationModule(private val mApplication: NHentaiApp) {
    @Provides
    fun provideContext() = mApplication.applicationContext!!

    @Provides
    fun provideApplication() = mApplication

    @Singleton
    @Provides
    fun provideBookApiService(): BookApiService {
        ServiceGenerator.setBaseUrl(ApiConstants.NHENTAI_HOME)
        ServiceGenerator.setInterceptor(null)
        return ServiceGenerator.createService(BookApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideTagApiService(): TagApiService {
        ServiceGenerator.setInterceptor(null)
        return ServiceGenerator.createService(TagApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideSharedPreferencesManager(): SharedPreferencesManager = SharedPreferencesManager.instance

    @Singleton
    @Provides
    fun providesRecentBookDAO(): RecentBookDAO = Database.instance.getRecentBookDAO()

    @Singleton
    @Provides
    fun providesTagDAO(): TagDAO = Database.instance.getTagDAO()

    @Provides
    @Main
    fun providesMainDispatcher(): CoroutineScope = CoroutineScope(Dispatchers.Main)

    @Provides
    @IO
    fun providesIODispatcher(): CoroutineScope = CoroutineScope(Dispatchers.IO)

    @Provides
    @Default
    fun providesDefaultDispatcher(): CoroutineScope = CoroutineScope(Dispatchers.Default)

    @Provides
    @Unconfined
    fun providesUnconfinedDispatcher(): CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
}