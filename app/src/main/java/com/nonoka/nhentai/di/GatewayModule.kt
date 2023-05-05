package com.nonoka.nhentai.di

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.gateway.DoujinshiRepositoryImpl
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSource
import com.nonoka.nhentai.gateway.remote.DoujinshiRemoteSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    @Singleton
    fun providesDoujinshiRepository(remoteSource: DoujinshiRemoteSource): DoujinshiRepository {
        return DoujinshiRepositoryImpl(remoteSource)
    }
}