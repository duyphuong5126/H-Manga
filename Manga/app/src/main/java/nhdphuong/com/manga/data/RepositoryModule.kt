package nhdphuong.com.manga.data

import androidx.annotation.NonNull
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import nhdphuong.com.manga.api.BookApiService
import nhdphuong.com.manga.api.MasterDataApiService
import nhdphuong.com.manga.data.local.BookLocalDataSource
import nhdphuong.com.manga.data.local.BookDAO
import nhdphuong.com.manga.data.local.TagDAO
import nhdphuong.com.manga.data.local.MasterDataLocalDataSource
import nhdphuong.com.manga.data.remote.BookRemoteDataSource
import nhdphuong.com.manga.data.remote.MasterDataRemoteDataSource
import nhdphuong.com.manga.scope.Local
import nhdphuong.com.manga.scope.Remote
import javax.inject.Singleton

/*
 * Created by nhdphuong on 3/24/18.
 */
@Module
class RepositoryModule {
    @Provides
    @NonNull
    @Singleton
    @Remote
    fun provideBookRemoteDataSource(bookApiService: BookApiService): BookDataSource.Remote {
        return BookRemoteDataSource(bookApiService)
    }

    @Provides
    @NonNull
    @Singleton
    @Local
    fun providesBookLocalDataSource(bookDAO: BookDAO, tagDAO: TagDAO): BookDataSource.Local {
        return BookLocalDataSource(bookDAO, tagDAO)
    }

    @Provides
    @NonNull
    @Singleton
    @Remote
    fun provideMasterDataRemoteDataSource(masterDataApiService: MasterDataApiService): MasterDataSource.Remote {
        return MasterDataRemoteDataSource(masterDataApiService)
    }

    @Provides
    @NonNull
    @Singleton
    @Local
    fun providesMasterDataLocalDataSource(tagDAO: TagDAO): MasterDataSource.Local {
        return MasterDataLocalDataSource(tagDAO, CoroutineScope(Dispatchers.IO))
    }
}
