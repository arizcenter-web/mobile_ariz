package com.tecsup.app_ariz.feature.admin.di

import com.tecsup.app_ariz.feature.admin.data.AdminApi
import com.tecsup.app_ariz.feature.admin.data.AdminRepositoryImpl
import com.tecsup.app_ariz.feature.admin.domain.AdminRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AdminModule {
    @Binds
    abstract fun bindRepository(impl: AdminRepositoryImpl): AdminRepository

    companion object {
        @Provides
        @Singleton
        fun provideApi(retrofit: Retrofit): AdminApi = retrofit.create(AdminApi::class.java)
    }
}
