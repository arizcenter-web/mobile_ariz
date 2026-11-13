package com.tecsup.app_ariz.feature.projects.domain.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail

interface ProjectsRepository {
    suspend fun getProjectDetail(slug: String): AppResult<ProjectDetail>
}
