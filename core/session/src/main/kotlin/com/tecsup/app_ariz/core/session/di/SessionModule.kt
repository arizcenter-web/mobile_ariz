package com.tecsup.app_ariz.core.session.di

import com.tecsup.app_ariz.core.session.DefaultSessionManager
import com.tecsup.app_ariz.core.session.SessionApi
import com.tecsup.app_ariz.core.session.SessionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    abstract fun bindSessionManager(impl: DefaultSessionManager): SessionManager

    companion object {
        @Provides
        @Singleton
        fun provideSessionApi(retrofit: Retrofit): SessionApi = retrofit.create(SessionApi::class.java)
    }
}
