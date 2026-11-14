package com.tecsup.app_ariz.feature.info.di

import com.tecsup.app_ariz.feature.info.data.ClaimApi
import com.tecsup.app_ariz.feature.info.data.ClaimRepositoryImpl
import com.tecsup.app_ariz.feature.info.data.ContactApi
import com.tecsup.app_ariz.feature.info.data.ContactRepositoryImpl
import com.tecsup.app_ariz.feature.info.domain.ClaimRepository
import com.tecsup.app_ariz.feature.info.domain.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class InfoModule {

    @Binds
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    abstract fun bindClaimRepository(impl: ClaimRepositoryImpl): ClaimRepository

    companion object {
        @Provides
        @Singleton
        fun provideContactApi(retrofit: Retrofit): ContactApi = retrofit.create(ContactApi::class.java)

        @Provides
        @Singleton
        fun provideClaimApi(retrofit: Retrofit): ClaimApi = retrofit.create(ClaimApi::class.java)
    }
}
