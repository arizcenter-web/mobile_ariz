package com.tecsup.app_ariz.feature.auth.di

import com.tecsup.app_ariz.feature.auth.data.google.FirebaseGoogleIdTokenProvider
import com.tecsup.app_ariz.feature.auth.data.remote.api.AuthApi
import com.tecsup.app_ariz.feature.auth.data.repository.AuthRepositoryImpl
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import com.tecsup.app_ariz.feature.auth.domain.repository.GoogleIdTokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindGoogleIdTokenProvider(impl: FirebaseGoogleIdTokenProvider): GoogleIdTokenProvider

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    }
}
