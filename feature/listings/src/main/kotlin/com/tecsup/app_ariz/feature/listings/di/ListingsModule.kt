package com.tecsup.app_ariz.feature.listings.di

import com.tecsup.app_ariz.feature.listings.data.remote.api.ListingsApi
import com.tecsup.app_ariz.feature.listings.data.repository.ListingsRepositoryImpl
import com.tecsup.app_ariz.feature.listings.domain.repository.ListingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ListingsModule {

    @Binds
    abstract fun bindListingsRepository(impl: ListingsRepositoryImpl): ListingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideListingsApi(retrofit: Retrofit): ListingsApi = retrofit.create(ListingsApi::class.java)
    }
}
