package nhdphuong.com.manga

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import nhdphuong.com.manga.api.ApiConstants
import nhdphuong.com.manga.api.BookApiService
import nhdphuong.com.manga.api.TagApiService
import nhdphuong.com.manga.data.local.Database
import nhdphuong.com.manga.data.local.BookDAO
import nhdphuong.com.manga.data.local.TagDAO
import nhdphuong.com.manga.scope.corountine.Default
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.supports.ServiceGenerator
import nhdphuong.com.manga.supports.INetworkUtils
import nhdphuong.com.manga.supports.NetworkUtils
import nhdphuong.com.manga.supports.IFileUtils
import nhdphuong.com.manga.supports.FileUtils
import nhdphuong.com.manga.supports.AppSupportUtils
import nhdphuong.com.manga.supports.SupportUtils
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
    fun provideSharedPreferencesManager(): SharedPreferencesManager {
        return SharedPreferencesManager.instance
    }

    @Singleton
    @Provides
    fun providesRecentBookDAO(): BookDAO = Database.instance.getRecentBookDAO()

    @Singleton
    @Provides
    fun providesTagDAO(): TagDAO = Database.instance.getTagDAO()

    @Provides
    fun providesNetworkUtils(): INetworkUtils = NetworkUtils()

    @Provides
    fun providesFileUtils(): IFileUtils = FileUtils()

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
    fun providesAppSupportUtils(): AppSupportUtils = SupportUtils()
}
