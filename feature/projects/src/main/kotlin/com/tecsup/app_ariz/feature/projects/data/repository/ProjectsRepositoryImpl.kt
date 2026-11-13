package com.tecsup.app_ariz.feature.projects.data.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.projects.data.mapper.toDomain
import com.tecsup.app_ariz.feature.projects.data.remote.api.ProjectsApi
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.domain.repository.ProjectsRepository
import javax.inject.Inject

internal class ProjectsRepositoryImpl @Inject constructor(
    private val api: ProjectsApi,
) : ProjectsRepository {

    override suspend fun getProjectDetail(slug: String): AppResult<ProjectDetail> =
        safeApiCall { api.projectBySlug(slug) }.map { it.toDomain() }
}
