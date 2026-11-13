package com.tecsup.app_ariz.core.favorites.di

import com.tecsup.app_ariz.core.favorites.data.remote.FavoritesApi
import com.tecsup.app_ariz.core.favorites.data.repository.DefaultFavoritesRepository
import com.tecsup.app_ariz.core.favorites.domain.FavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FavoritesModule {

    @Binds
    abstract fun bindFavoritesRepository(impl: DefaultFavoritesRepository): FavoritesRepository

    companion object {
        @Provides
        @Singleton
        fun provideFavoritesApi(retrofit: Retrofit): FavoritesApi = retrofit.create(FavoritesApi::class.java)
    }
}
