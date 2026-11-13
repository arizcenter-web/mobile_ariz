package com.tecsup.app_ariz.core.catalog.domain

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySummary

enum class ProjectOrigin(val apiValue: String) { ARIZ("ADMIN"), AGENTS("AGENT") }

enum class ProjectSort(val apiValue: String) { RECENT("recent"), FEATURED("featured") }

/** Catálogo público de propiedades y proyectos. Lo comparten Inicio, Buscar, Listings y Projects. */
interface CatalogRepository {
    suspend fun recentProperties(pageSize: Int, page: Int = 1): AppResult<Page<PropertySummary>>

    /** Búsqueda con filtros y orden (`GET /properties`). */
    suspend fun searchProperties(
        filters: PropertyFilters,
        pageSize: Int,
        page: Int = 1,
    ): AppResult<Page<PropertySummary>>

    suspend fun projects(
        pageSize: Int,
        origin: ProjectOrigin? = null,
        sort: ProjectSort? = null,
        page: Int = 1,
    ): AppResult<Page<ProjectSummary>>

    /** Directorio de proyectos con filtros y orden (`GET /projects`). */
    suspend fun searchProjects(
        filters: ProjectFilters,
        pageSize: Int,
        page: Int = 1,
    ): AppResult<Page<ProjectSummary>>
}
