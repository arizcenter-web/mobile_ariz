package com.tecsup.app_ariz.core.media.di

import com.tecsup.app_ariz.core.media.MediaRepository
import com.tecsup.app_ariz.core.media.data.CloudinaryApi
import com.tecsup.app_ariz.core.media.data.MediaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MediaModule {

    @Binds
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    companion object {
        @Provides
        @Singleton
        fun provideCloudinaryApi(retrofit: Retrofit): CloudinaryApi = retrofit.create(CloudinaryApi::class.java)
    }
}
