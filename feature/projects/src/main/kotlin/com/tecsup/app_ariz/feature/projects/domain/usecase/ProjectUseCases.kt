package com.tecsup.app_ariz.feature.projects.domain.usecase

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.domain.repository.ProjectsRepository
import javax.inject.Inject

const val ProjectsPageSize = 12

/** Directorio de proyectos publicados, con filtros, de [ProjectsPageSize] en [ProjectsPageSize]. */
class SearchProjectsUseCase @Inject constructor(
    private val catalog: CatalogRepository,
) {
    suspend operator fun invoke(filters: ProjectFilters, page: Int): AppResult<Page<ProjectSummary>> =
        catalog.searchProjects(filters, pageSize = ProjectsPageSize, page = page)
}

class GetProjectDetailUseCase @Inject constructor(
    private val repository: ProjectsRepository,
) {
    suspend operator fun invoke(slug: String): AppResult<ProjectDetail> = repository.getProjectDetail(slug)
}
