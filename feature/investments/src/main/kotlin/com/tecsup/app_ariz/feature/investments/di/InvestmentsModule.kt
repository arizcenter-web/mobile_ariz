package com.tecsup.app_ariz.feature.investments.di

import com.tecsup.app_ariz.feature.investments.data.remote.InvestmentsApi
import com.tecsup.app_ariz.feature.investments.data.repository.InvestmentsRepositoryImpl
import com.tecsup.app_ariz.feature.investments.domain.repository.InvestmentsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class InvestmentsModule {

    @Binds
    abstract fun bindInvestmentsRepository(impl: InvestmentsRepositoryImpl): InvestmentsRepository

    companion object {
        @Provides
        @Singleton
        fun provideInvestmentsApi(retrofit: Retrofit): InvestmentsApi = retrofit.create(InvestmentsApi::class.java)
    }
}
