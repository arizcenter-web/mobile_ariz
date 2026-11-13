package com.tecsup.app_ariz.feature.search.ui

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.catalog.domain.ProjectOrigin
import com.tecsup.app_ariz.core.catalog.domain.ProjectSort
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySortOrder
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter
import com.tecsup.app_ariz.feature.search.domain.usecase.SearchPropertiesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private class FakeCatalog : CatalogRepository {
        val calls = mutableListOf<Pair<PropertyFilters, Int>>()
        var failNext = false
        var totalPages = 2

        override suspend fun searchProperties(filters: PropertyFilters, pageSize: Int, page: Int): AppResult<Page<PropertySummary>> {
            calls += filters to page
            if (failNext) {
                failNext = false
                return AppResult.Failure(AppError.Network())
            }
            return AppResult.Success(
                Page(listOf(property("p$page")), total = totalPages, page = page, hasMore = page < totalPages),
            )
        }

        override suspend fun recentProperties(pageSize: Int, page: Int): AppResult<Page<PropertySummary>> =
            error("no se usa en Buscar")

        override suspend fun searchProjects(filters: ProjectFilters, pageSize: Int, page: Int): AppResult<Page<ProjectSummary>> = error("no se usa")
        override suspend fun projects(
            pageSize: Int, origin: ProjectOrigin?, sort: ProjectSort?, page: Int,
        ): AppResult<Page<ProjectSummary>> = error("no se usa en Buscar")
    }

    private val catalog = FakeCatalog()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SearchViewModel(SearchPropertiesUseCase(catalog))

    @Test
    fun `al abrir carga la primera pagina sin filtros`() {
        val viewModel = viewModel()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("p1"), state.items.map { it.id })
        assertEquals(PropertyFilters(), catalog.calls.single().first)
        assertTrue(state.hasMore)
    }

    @Test
    fun `cambiar un filtro reinicia la busqueda desde la pagina 1`() {
        val viewModel = viewModel()

        viewModel.onAction(SearchAction.OperationChanged(ListingOperation.RENT))
        viewModel.onAction(SearchAction.TypeChanged(PropertyTypeFilter.HOUSE))

        val last = catalog.calls.last()
        assertEquals(ListingOperation.RENT, last.first.operation)
        assertEquals(PropertyTypeFilter.HOUSE, last.first.type)
        assertEquals(1, last.second)
        assertEquals(listOf("p1"), viewModel.state.value.items.map { it.id })
    }

    @Test
    fun `los textos de distrito y precio solo se aplican al confirmar`() {
        val viewModel = viewModel()

        viewModel.onAction(SearchAction.DistrictChanged("  Zarumilla "))
        viewModel.onAction(SearchAction.PriceMinChanged("1a2b3"))
        assertEquals(1, catalog.calls.size)
        assertEquals("123", viewModel.state.value.priceMinText)

        viewModel.onAction(SearchAction.ApplyMoreFilters)

        val applied = catalog.calls.last().first
        assertEquals("Zarumilla", applied.district)
        assertEquals(123, applied.priceMin)
        assertNull(applied.priceMax)
    }

    @Test
    fun `cargar mas agrega la siguiente pagina y se detiene al final`() {
        val viewModel = viewModel()

        viewModel.onAction(SearchAction.LoadMore)
        assertEquals(listOf("p1", "p2"), viewModel.state.value.items.map { it.id })
        assertFalse(viewModel.state.value.hasMore)

        viewModel.onAction(SearchAction.LoadMore)
        assertEquals(2, catalog.calls.size)
    }

    @Test
    fun `un error en la primera carga se muestra y reintentar lo recupera`() {
        catalog.failNext = true
        val viewModel = viewModel()
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.items.isEmpty())

        viewModel.onAction(SearchAction.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals(listOf("p1"), viewModel.state.value.items.map { it.id })
    }

    @Test
    fun `limpiar filtros vuelve a la busqueda inicial y borra los textos escritos`() {
        val viewModel = viewModel()
        assertFalse(viewModel.state.value.hasActiveFilters)

        viewModel.onAction(SearchAction.TypeChanged(PropertyTypeFilter.HOUSE))
        viewModel.onAction(SearchAction.DistrictChanged("Zarumilla"))
        viewModel.onAction(SearchAction.PriceMinChanged("500"))
        assertTrue(viewModel.state.value.hasActiveFilters)

        viewModel.onAction(SearchAction.ClearFilters)

        val state = viewModel.state.value
        assertFalse(state.hasActiveFilters)
        assertEquals("", state.districtText)
        assertEquals("", state.priceMinText)
        assertEquals(PropertyFilters(), catalog.calls.last().first)
        assertEquals(1, catalog.calls.last().second)
    }

    @Test
    fun `cambiar el orden se envia al backend`() {
        val viewModel = viewModel()

        viewModel.onAction(SearchAction.SortChanged(PropertySortOrder.PRICE_ASC))

        assertEquals(PropertySortOrder.PRICE_ASC, catalog.calls.last().first.sort)
    }
}

private fun property(id: String) = PropertySummary(
    id = id, slug = id, title = "Propiedad $id", description = "", operation = ListingOperation.SALE,
    price = 100.0, currency = Currency.PEN, district = "Tumbes", department = "Tumbes", coverImageUrl = null,
    photoCount = 0, areaM2 = 50.0, bedrooms = 1, bathrooms = 1, parkingSpaces = 0, publishedAtMillis = null,
)
