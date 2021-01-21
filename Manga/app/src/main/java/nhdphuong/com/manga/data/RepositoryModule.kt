package nhdphuong.com.manga.data

import androidx.annotation.NonNull
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.api.MasterDataApiService
import nhdphuong.com.manga.data.local.BookLocalDataSource
import nhdphuong.com.manga.data.local.BookDAO
import nhdphuong.com.manga.data.local.TagDAO
import nhdphuong.com.manga.data.local.MasterDataLocalDataSource
import nhdphuong.com.manga.data.local.SearchLocalDataSource
import nhdphuong.com.manga.data.local.SearchLocalDataSourceImpl
import nhdphuong.com.manga.data.remote.BookRemoteDataSource
import nhdphuong.com.manga.data.remote.MasterDataRemoteDataSource
import nhdphuong.com.manga.data.repository.SearchRepository
import nhdphuong.com.manga.data.repository.SearchRepositoryImpl
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
    fun provideBookRemoteDataSource(service: SerializationService): BookDataSource.Remote {
        return BookRemoteDataSource(service)
    }

    @Provides
    @NonNull
    @Singleton
    @Local
    fun providesBookLocalDataSource(
        bookDAO: BookDAO,
        bookSerializationService: SerializationService,
        tagDAO: TagDAO
    ): BookDataSource.Local {
        return BookLocalDataSource(bookDAO, tagDAO, bookSerializationService)
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
    fun providesMasterDataLocalDataSource(
        tagDAO: TagDAO,
        sharedPreferencesManager: SharedPreferencesManager,
        service: SerializationService
    ): MasterDataSource.Local {
        return MasterDataLocalDataSource(
            tagDAO,
            sharedPreferencesManager,
            service,
            CoroutineScope(Dispatchers.IO),
        )
    }

    @Provides
    fun providesSearchLocalDataSource(dataSourceImpl: SearchLocalDataSourceImpl): SearchLocalDataSource {
        return dataSourceImpl
    }

    @Provides
    fun providesSearchRepository(repositoryImpl: SearchRepositoryImpl): SearchRepository {
        return repositoryImpl
    }

}
