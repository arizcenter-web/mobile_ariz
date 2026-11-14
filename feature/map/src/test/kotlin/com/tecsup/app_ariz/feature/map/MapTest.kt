package com.tecsup.app_ariz.feature.map

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.catalog.domain.ProjectOrigin
import com.tecsup.app_ariz.core.catalog.domain.ProjectSort
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.feature.map.domain.LoadMapProjectsUseCase
import com.tecsup.app_ariz.feature.map.domain.filterBy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MapTest {
    private fun project(id: String, name: String, district: String = "Ate", category: ProjectCategory = ProjectCategory.LOTIZACION, lat: Double? = -12.0, lng: Double? = -77.0) =
        ProjectSummary(
            id = id, slug = id, name = name, developer = "ARIZ", priceFrom = 1000.0, currency = Currency.PEN, coverImageUrl = null,
            stage = ProjectStage.PRE_SALE, category = category, minAreaM2 = 100.0, totalUnits = 10, district = district, publishedAtMillis = null,
            latitude = lat, longitude = lng,
        )

    private class FakeCatalog(private val pages: List<List<ProjectSummary>>) : CatalogRepository {
        var requested = 0
        override suspend fun projects(pageSize: Int, origin: ProjectOrigin?, sort: ProjectSort?, page: Int): AppResult<Page<ProjectSummary>> {
            requested++
            val items = pages.getOrElse(page - 1) { emptyList() }
            return AppResult.Success(Page(items = items, total = pages.sumOf { it.size }, page = page, hasMore = page < pages.size))
        }
        override suspend fun recentProperties(pageSize: Int, page: Int): AppResult<Page<PropertySummary>> = error("no se usa")
        override suspend fun searchProperties(filters: PropertyFilters, pageSize: Int, page: Int): AppResult<Page<PropertySummary>> = error("no se usa")
        override suspend fun searchProjects(filters: ProjectFilters, pageSize: Int, page: Int): AppResult<Page<ProjectSummary>> = error("no se usa")
    }

    @Test
    fun `junta todas las paginas y deja solo los proyectos con punto en el mapa`() = runBlocking {
        val catalog = FakeCatalog(listOf(listOf(project("a", "Lomas"), project("b", "Sin punto", lat = null)), listOf(project("c", "Cerro"))))
        val result = LoadMapProjectsUseCase(catalog)() as AppResult.Success
        assertEquals(listOf("a", "c"), result.data.map { it.id })
        assertEquals(2, catalog.requested)
    }

    @Test
    fun `el filtro busca por nombre, desarrolladora o distrito y por tipo`() {
        val all = listOf(
            project("a", "Lomas del Sol", district = "Ate"),
            project("b", "Torres Mar", district = "Miraflores", category = ProjectCategory.DEPARTAMENTOS),
        )
        assertEquals(listOf("a"), all.filterBy("lomas", null).map { it.id })
        assertEquals(listOf("b"), all.filterBy("miraflores", null).map { it.id })
        assertEquals(listOf("b"), all.filterBy("", ProjectCategory.DEPARTAMENTOS).map { it.id })
        assertEquals(2, all.filterBy("  ariz ", null).size) // también por desarrolladora
        assertEquals(0, all.filterBy("lomas", ProjectCategory.DEPARTAMENTOS).size)
    }
}
