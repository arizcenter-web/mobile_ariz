package com.tecsup.app_ariz.core.leads.di

import com.tecsup.app_ariz.core.leads.data.LeadsApi
import com.tecsup.app_ariz.core.leads.data.LeadsRepositoryImpl
import com.tecsup.app_ariz.core.leads.domain.LeadsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LeadsModule {

    @Binds
    abstract fun bindLeadsRepository(impl: LeadsRepositoryImpl): LeadsRepository

    companion object {
        @Provides
        @Singleton
        fun provideLeadsApi(retrofit: Retrofit): LeadsApi = retrofit.create(LeadsApi::class.java)
    }
}
