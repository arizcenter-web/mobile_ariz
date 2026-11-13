package com.tecsup.app_ariz.feature.projects

import androidx.lifecycle.SavedStateHandle
import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.catalog.domain.ProjectOrigin
import com.tecsup.app_ariz.core.catalog.domain.ProjectSort
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectOriginFilter
import com.tecsup.app_ariz.core.model.project.ProjectSortOrder
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.feature.projects.data.mapper.toDomain
import com.tecsup.app_ariz.feature.projects.data.remote.dto.AgentDto
import com.tecsup.app_ariz.feature.projects.data.remote.dto.LocationDto
import com.tecsup.app_ariz.feature.projects.data.remote.dto.LotDto
import com.tecsup.app_ariz.feature.projects.data.remote.dto.MediaDto
import com.tecsup.app_ariz.feature.projects.data.remote.dto.ProjectDetailDto
import com.tecsup.app_ariz.feature.projects.domain.model.LotStatus
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.domain.repository.ProjectsRepository
import com.tecsup.app_ariz.feature.projects.domain.usecase.GetProjectDetailUseCase
import com.tecsup.app_ariz.feature.projects.domain.usecase.SearchProjectsUseCase
import com.tecsup.app_ariz.feature.projects.ui.detail.ProjectDetailAction
import com.tecsup.app_ariz.feature.projects.ui.detail.ProjectDetailViewModel
import com.tecsup.app_ariz.feature.projects.ui.list.ProjectListAction
import com.tecsup.app_ariz.feature.projects.ui.list.ProjectListViewModel
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

private fun summary(id: String) = ProjectSummary(
    id = id, slug = id, name = "Proyecto $id", developer = "ARIZ", priceFrom = 50.0, currency = Currency.PEN,
    coverImageUrl = null, stage = ProjectStage.PRE_SALE, category = ProjectCategory.LOTIZACION,
    minAreaM2 = 100.0, totalUnits = 10, district = "Tumbes", publishedAtMillis = null,
)

private fun detailDto(
    media: List<MediaDto> = emptyList(),
    images: List<String> = emptyList(),
    lots: List<LotDto> = emptyList(),
    agent: AgentDto = AgentDto(name = "ARIZ Proyectos", type = "ADMIN", status = "APPROVED"),
) = ProjectDetailDto(
    id = "1", slug = "p", name = "P", stage = "UNDER_CONSTRUCTION", category = "DEPARTAMENTOS",
    currency = "USD", priceFrom = 80_000.0, priceTo = 120_000.0, publishedAt = "2023-11-14T22:13:20.000Z",
    location = LocationDto(district = "Tumbes", province = "Tumbes", department = "Tumbes", latitude = -3.5, longitude = -80.4),
    media = media, images = images, lots = lots, agent = agent, totalLots = 0, downPayment = 0.0,
)

class ProjectDetailMapperTest {

    @Test
    fun `la galeria usa las fotos IMAGE de media y si no hay, la lista images`() {
        val withMedia = detailDto(
            media = listOf(MediaDto("IMAGE", "a.jpg"), MediaDto("FLOOR_PLAN", "plan.pdf"), MediaDto("RENDER", "r.jpg")),
            images = listOf("ignorada.jpg"),
        ).toDomain()
        assertEquals(listOf("a.jpg"), withMedia.gallery)
        assertEquals(listOf("plan.pdf"), withMedia.planUrls)
        assertEquals(listOf("r.jpg"), withMedia.renderUrls)

        assertEquals(listOf("x.jpg"), detailDto(images = listOf("x.jpg")).toDomain().gallery)
    }

    @Test
    fun `categoria etapa y moneda se interpretan y los ceros quedan como null`() {
        val detail = detailDto().toDomain()

        assertEquals(ProjectCategory.DEPARTAMENTOS, detail.category)
        assertTrue(detail.isApartments)
        assertEquals(ProjectStage.UNDER_CONSTRUCTION, detail.stage)
        assertEquals(Currency.USD, detail.currency)
        assertNull(detail.totalLots)
        assertNull(detail.downPayment)
        assertTrue(detail.location.hasCoordinates)
    }

    @Test
    fun `estado de lote desconocido cae en disponible y el anunciante ARIZ es oficial`() {
        val detail = detailDto(lots = listOf(LotDto(id = "l", lotNumber = "7", status = "RARO"))).toDomain()

        assertEquals(LotStatus.AVAILABLE, detail.lots.single().status)
        assertTrue(detail.advertiser.isArizOfficial)
        assertTrue(detail.advertiser.isVerified)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelsTest {

    private class FakeCatalog : CatalogRepository {
        val calls = mutableListOf<Pair<ProjectFilters, Int>>()
        var failNext = false
        override suspend fun searchProjects(filters: ProjectFilters, pageSize: Int, page: Int): AppResult<Page<ProjectSummary>> {
            calls += filters to page
            if (failNext) {
                failNext = false
                return AppResult.Failure(AppError.Network())
            }
            return AppResult.Success(Page(listOf(summary("p$page")), total = 2, page = page, hasMore = page < 2))
        }
        override suspend fun recentProperties(pageSize: Int, page: Int): AppResult<Page<PropertySummary>> = error("no se usa")
        override suspend fun searchProperties(filters: PropertyFilters, pageSize: Int, page: Int): AppResult<Page<PropertySummary>> = error("no se usa")
        override suspend fun projects(pageSize: Int, origin: ProjectOrigin?, sort: ProjectSort?, page: Int): AppResult<Page<ProjectSummary>> = error("no se usa")
    }

    private class FakeProjects(var result: AppResult<ProjectDetail>) : ProjectsRepository {
        var requestedSlug: String? = null
        override suspend fun getProjectDetail(slug: String): AppResult<ProjectDetail> {
            requestedSlug = slug
            return result
        }
    }

    private val catalog = FakeCatalog()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun listViewModel() = ProjectListViewModel(SearchProjectsUseCase(catalog))

    @Test
    fun `el directorio carga la primera pagina sin filtros`() {
        val viewModel = listViewModel()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf("p1"), viewModel.state.value.items.map { it.id })
        assertEquals(ProjectFilters(), catalog.calls.single().first)
        assertTrue(viewModel.state.value.hasMore)
    }

    @Test
    fun `cambiar etapa origen y orden reinicia la busqueda desde la pagina 1`() {
        val viewModel = listViewModel()

        viewModel.onAction(ProjectListAction.StageChanged(ProjectStage.DELIVERED))
        viewModel.onAction(ProjectListAction.OriginChanged(ProjectOriginFilter.ARIZ))
        viewModel.onAction(ProjectListAction.SortChanged(ProjectSortOrder.FEATURED))

        val (filters, page) = catalog.calls.last()
        assertEquals(ProjectStage.DELIVERED, filters.stage)
        assertEquals(ProjectOriginFilter.ARIZ, filters.origin)
        assertEquals(ProjectSortOrder.FEATURED, filters.sort)
        assertEquals(1, page)
    }

    @Test
    fun `el distrito escrito solo se aplica al confirmar`() {
        val viewModel = listViewModel()

        viewModel.onAction(ProjectListAction.DistrictChanged("  Zarumilla "))
        assertEquals(1, catalog.calls.size)

        viewModel.onAction(ProjectListAction.DistrictSubmitted)
        assertEquals("Zarumilla", catalog.calls.last().first.district)
    }

    @Test
    fun `cargar mas agrega la siguiente pagina y reintentar recupera un error inicial`() {
        val viewModel = listViewModel()
        viewModel.onAction(ProjectListAction.LoadMore)
        assertEquals(listOf("p1", "p2"), viewModel.state.value.items.map { it.id })
        assertFalse(viewModel.state.value.hasMore)

        catalog.failNext = true
        viewModel.onAction(ProjectListAction.StageChanged(null))
        assertNotNull(viewModel.state.value.error)

        viewModel.onAction(ProjectListAction.Retry)
        assertNull(viewModel.state.value.error)
        assertEquals(listOf("p1"), viewModel.state.value.items.map { it.id })
    }

    @Test
    fun `el detalle pide el slug de la ruta y reintentar lo recupera`() {
        val repo = FakeProjects(AppResult.Failure(AppError.NotFound()))
        val viewModel = ProjectDetailViewModel(
            SavedStateHandle(mapOf(ProjectDetailViewModel.SLUG_ARG to "mi-proyecto")),
            GetProjectDetailUseCase(repo),
        )
        assertEquals("mi-proyecto", repo.requestedSlug)
        assertNotNull(viewModel.state.value.error)

        repo.result = AppResult.Success(detailDto().toDomain())
        viewModel.onAction(ProjectDetailAction.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals("P", viewModel.state.value.detail?.name)
    }
}
