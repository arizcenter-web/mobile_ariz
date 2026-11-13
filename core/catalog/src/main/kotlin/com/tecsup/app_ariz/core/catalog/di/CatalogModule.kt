package com.tecsup.app_ariz.core.catalog.di

import com.tecsup.app_ariz.core.catalog.data.remote.CatalogApi
import com.tecsup.app_ariz.core.catalog.data.repository.CatalogRepositoryImpl
import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CatalogModule {

    @Binds
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    companion object {
        @Provides
        @Singleton
        fun provideCatalogApi(retrofit: Retrofit): CatalogApi = retrofit.create(CatalogApi::class.java)
    }
}
