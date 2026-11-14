package com.tecsup.app_ariz.feature.map.domain

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.catalog.domain.ProjectSort
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import javax.inject.Inject

/** Páginas de 50 hasta agotar el catálogo (con un tope para no pedir sin fin). */
private const val PageSize = 50
private const val MaxPages = 8

/** Proyectos publicados con punto en el mapa: los que se pueden dibujar como marcador. */
class LoadMapProjectsUseCase @Inject constructor(private val catalog: CatalogRepository) {
    suspend operator fun invoke(): AppResult<List<ProjectSummary>> {
        val all = mutableListOf<ProjectSummary>()
        var page = 1
        while (page <= MaxPages) {
            when (val result = catalog.projects(PageSize, sort = ProjectSort.RECENT, page = page)) {
                is AppResult.Failure -> return if (all.isEmpty()) result else AppResult.Success(all.withPoint())
                is AppResult.Success -> {
                    all += result.data.items
                    if (!result.data.hasMore || result.data.items.isEmpty()) break
                }
            }
            page++
        }
        return AppResult.Success(all.withPoint())
    }

    private fun List<ProjectSummary>.withPoint() = filter { it.latitude != null && it.longitude != null }
}

/** Filtro del mapa: texto (nombre, desarrolladora o distrito) y tipo de proyecto. */
fun List<ProjectSummary>.filterBy(query: String, category: com.tecsup.app_ariz.core.model.project.ProjectCategory?): List<ProjectSummary> {
    val text = query.trim().lowercase()
    return filter { project ->
        (category == null || project.category == category) &&
            (text.isEmpty() || listOf(project.name, project.developer, project.district).any { it.lowercase().contains(text) })
    }
}
