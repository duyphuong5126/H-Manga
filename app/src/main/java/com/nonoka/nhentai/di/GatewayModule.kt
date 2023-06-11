package com.nonoka.nhentai.di

import android.content.Context
import androidx.room.Room
import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.gateway.DoujinshiRepositoryImpl
import com.nonoka.nhentai.gateway.local.DoujinshiDao
import com.nonoka.nhentai.gateway.local.DoujinshiLocalDataSource
import com.nonoka.nhentai.gateway.local.DoujinshiLocalDataSourceImpl
import com.nonoka.nhentai.gateway.local.NHentaiDatabase
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GatewayModule {
    @Provides
    @Singleton
    fun providesDoujinshiRemoteDataSource(): DoujinshiRemoteSource {
        return DoujinshiRemoteSourceImpl()
    }

    @Provides
    fun providesDoujinshiLocalDataSource(doujinshiDao: DoujinshiDao): DoujinshiLocalDataSource {
        return DoujinshiLocalDataSourceImpl(doujinshiDao)
    }

    @Provides
    @Singleton
    fun providesDoujinshiRepository(
        remoteSource: DoujinshiRemoteSource,
        localDataSource: DoujinshiLocalDataSource
    ): DoujinshiRepository {
        return DoujinshiRepositoryImpl(remoteSource, localDataSource)
    }

    @Provides
    fun providesNHentaiDatabase(@ApplicationContext applicationContext: Context): NHentaiDatabase {
        return Room.databaseBuilder(
            applicationContext,
            NHentaiDatabase::class.java, "nhentai_database"
        ).build()
    }

    @Provides
    fun providesDoujinshiDao(database: NHentaiDatabase): DoujinshiDao {
        return database.doujinshiDao()
    }
}