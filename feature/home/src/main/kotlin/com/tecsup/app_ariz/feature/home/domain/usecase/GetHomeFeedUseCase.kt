package com.tecsup.app_ariz.feature.home.domain.usecase

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.catalog.domain.ProjectOrigin
import com.tecsup.app_ariz.core.catalog.domain.ProjectSort
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.home.domain.model.HomeFeed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private const val SectionSize = 5

/**
 * Carga las tres filas del inicio en paralelo. Como en el web, una fila que falla se muestra vacía
 * y el resto sigue; solo si fallan las tres se devuelve el error (para ofrecer "Reintentar").
 */
class GetHomeFeedUseCase @Inject constructor(
    private val catalog: CatalogRepository,
) {
    suspend operator fun invoke(): AppResult<HomeFeed> = coroutineScope {
        val arizProjects = async { catalog.projects(SectionSize, origin = ProjectOrigin.ARIZ) }
        val properties = async { catalog.recentProperties(SectionSize) }
        val featured = async {
            catalog.projects(SectionSize, origin = ProjectOrigin.AGENTS, sort = ProjectSort.FEATURED)
        }

        val projectsResult = arizProjects.await()
        val propertiesResult = properties.await()
        val featuredResult = featured.await()

        val failures = listOf(projectsResult, propertiesResult, featuredResult).filterIsInstance<AppResult.Failure>()
        if (failures.size == 3) {
            AppResult.Failure(failures.first().error)
        } else {
            AppResult.Success(
                HomeFeed(
                    arizProjects = (projectsResult as? AppResult.Success)?.data?.items.orEmpty(),
                    featuredProperties = (propertiesResult as? AppResult.Success)?.data?.items.orEmpty(),
                    featuredProjects = (featuredResult as? AppResult.Success)?.data?.items.orEmpty(),
                ),
            )
        }
    }
}
