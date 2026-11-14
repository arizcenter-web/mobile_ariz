package com.tecsup.app_ariz.feature.customer.di

import com.tecsup.app_ariz.feature.customer.data.CustomerApi
import com.tecsup.app_ariz.feature.customer.data.CustomerRepositoryImpl
import com.tecsup.app_ariz.feature.customer.domain.CustomerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CustomerModule {
    @Binds
    abstract fun bindRepository(impl: CustomerRepositoryImpl): CustomerRepository

    companion object {
        @Provides
        @Singleton
        fun provideApi(retrofit: Retrofit): CustomerApi = retrofit.create(CustomerApi::class.java)
    }
}
