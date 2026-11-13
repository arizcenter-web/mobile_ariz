package com.tecsup.app_ariz.feature.home.domain

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.catalog.domain.ProjectOrigin
import com.tecsup.app_ariz.core.catalog.domain.ProjectSort
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.feature.home.domain.usecase.GetHomeFeedUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHomeFeedUseCaseTest {

    private val property = PropertySummary(
        id = "p1", slug = "casa", title = "Casa", description = "", operation = ListingOperation.SALE, price = 100.0,
        currency = Currency.PEN, district = "Tumbes", department = "Tumbes", coverImageUrl = null, photoCount = 0,
        areaM2 = 90.0, bedrooms = 3, bathrooms = 2, parkingSpaces = 1, publishedAtMillis = null,
    )
    private val project = ProjectSummary(
        id = "j1", slug = "lotes", name = "Lotes", developer = "ARIZ", priceFrom = 50.0, currency = Currency.USD,
        coverImageUrl = null, stage = ProjectStage.PRE_SALE, category = ProjectCategory.LOTIZACION,
        minAreaM2 = 120.0, totalUnits = 10, district = "Tumbes", publishedAtMillis = null,
    )

    private fun <T> pageOf(item: T) = AppResult.Success(Page(listOf(item), total = 1, page = 1, hasMore = false))

    private class FakeCatalog(
        val properties: AppResult<Page<PropertySummary>>,
        val arizProjects: AppResult<Page<ProjectSummary>>,
        val agentProjects: AppResult<Page<ProjectSummary>>,
    ) : CatalogRepository {
        override suspend fun recentProperties(pageSize: Int, page: Int) = properties
        override suspend fun searchProperties(filters: PropertyFilters, pageSize: Int, page: Int) = properties
        override suspend fun searchProjects(filters: ProjectFilters, pageSize: Int, page: Int): AppResult<Page<ProjectSummary>> = error("no se usa")
        override suspend fun projects(pageSize: Int, origin: ProjectOrigin?, sort: ProjectSort?, page: Int) =
            if (origin == ProjectOrigin.ARIZ) arizProjects else agentProjects
    }

    @Test
    fun `junta las tres filas cuando todo responde`() = runTest {
        val useCase = GetHomeFeedUseCase(FakeCatalog(pageOf(property), pageOf(project), pageOf(project)))

        val feed = (useCase() as AppResult.Success).data

        assertEquals(listOf(property), feed.featuredProperties)
        assertEquals(listOf(project), feed.arizProjects)
        assertEquals(listOf(project), feed.featuredProjects)
    }

    @Test
    fun `una fila que falla queda vacia y las demas siguen`() = runTest {
        val failure = AppResult.Failure(AppError.Network())
        val useCase = GetHomeFeedUseCase(FakeCatalog(failure, pageOf(project), failure))

        val feed = (useCase() as AppResult.Success).data

        assertTrue(feed.featuredProperties.isEmpty())
        assertTrue(feed.featuredProjects.isEmpty())
        assertEquals(listOf(project), feed.arizProjects)
    }

    @Test
    fun `si fallan las tres filas devuelve el error`() = runTest {
        val failure = AppResult.Failure(AppError.Network())
        val useCase = GetHomeFeedUseCase(FakeCatalog(failure, failure, failure))

        assertTrue(useCase() is AppResult.Failure)
    }
}
