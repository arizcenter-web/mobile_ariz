package com.tecsup.app_ariz.feature.professional.di

import com.tecsup.app_ariz.feature.professional.data.ProfessionalApi
import com.tecsup.app_ariz.feature.professional.data.ProfessionalRepositoryImpl
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProfessionalModule {

    @Binds
    abstract fun bindRepository(impl: ProfessionalRepositoryImpl): ProfessionalRepository

    companion object {
        @Provides
        @Singleton
        fun provideApi(retrofit: Retrofit): ProfessionalApi = retrofit.create(ProfessionalApi::class.java)
    }
}
