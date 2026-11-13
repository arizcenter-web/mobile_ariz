package com.tecsup.app_ariz.feature.rewards.di

import com.tecsup.app_ariz.feature.rewards.data.remote.api.RewardsApi
import com.tecsup.app_ariz.feature.rewards.data.repository.RewardsRepositoryImpl
import com.tecsup.app_ariz.feature.rewards.domain.repository.RewardsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RewardsModule {

    @Binds
    abstract fun bindRewardsRepository(impl: RewardsRepositoryImpl): RewardsRepository

    companion object {
        @Provides
        @Singleton
        fun provideRewardsApi(retrofit: Retrofit): RewardsApi = retrofit.create(RewardsApi::class.java)
    }
}
