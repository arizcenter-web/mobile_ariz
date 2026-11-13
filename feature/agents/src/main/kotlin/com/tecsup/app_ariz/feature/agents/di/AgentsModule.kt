package com.tecsup.app_ariz.feature.agents.di

import com.tecsup.app_ariz.feature.agents.data.remote.api.AgentsApi
import com.tecsup.app_ariz.feature.agents.data.repository.AgentsRepositoryImpl
import com.tecsup.app_ariz.feature.agents.domain.repository.AgentsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AgentsModule {

    @Binds
    abstract fun bindAgentsRepository(impl: AgentsRepositoryImpl): AgentsRepository

    companion object {
        @Provides
        @Singleton
        fun provideAgentsApi(retrofit: Retrofit): AgentsApi = retrofit.create(AgentsApi::class.java)
    }
}
