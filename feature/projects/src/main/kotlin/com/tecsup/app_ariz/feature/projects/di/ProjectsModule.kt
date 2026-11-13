package com.tecsup.app_ariz.feature.projects.di

import com.tecsup.app_ariz.feature.projects.data.remote.api.ProjectsApi
import com.tecsup.app_ariz.feature.projects.data.repository.ProjectsRepositoryImpl
import com.tecsup.app_ariz.feature.projects.domain.repository.ProjectsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProjectsModule {

    @Binds
    abstract fun bindProjectsRepository(impl: ProjectsRepositoryImpl): ProjectsRepository

    companion object {
        @Provides
        @Singleton
        fun provideProjectsApi(retrofit: Retrofit): ProjectsApi = retrofit.create(ProjectsApi::class.java)
    }
}
