package com.tecsup.app_ariz.feature.listings

import androidx.lifecycle.SavedStateHandle
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
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.feature.listings.data.mapper.toDomain
import com.tecsup.app_ariz.feature.listings.data.remote.dto.AgentDto
import com.tecsup.app_ariz.feature.listings.data.remote.dto.DetailsDto
import com.tecsup.app_ariz.feature.listings.data.remote.dto.LocationDto
import com.tecsup.app_ariz.feature.listings.data.remote.dto.PriceDto
import com.tecsup.app_ariz.feature.listings.data.remote.dto.PropertyDetailDto
import com.tecsup.app_ariz.feature.listings.domain.model.AgentType
import com.tecsup.app_ariz.feature.listings.domain.model.ConservationStatus
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail
import com.tecsup.app_ariz.feature.listings.domain.repository.ListingsRepository
import com.tecsup.app_ariz.feature.listings.domain.usecase.CreatePriceAlertUseCase
import com.tecsup.app_ariz.feature.listings.domain.usecase.GetPropertyDetailUseCase
import com.tecsup.app_ariz.feature.listings.domain.usecase.GetSimilarPropertiesUseCase
import com.tecsup.app_ariz.feature.listings.ui.detail.PriceAlertStatus
import com.tecsup.app_ariz.feature.listings.ui.detail.PropertyDetailAction
import com.tecsup.app_ariz.feature.listings.ui.detail.PropertyDetailEvent
import com.tecsup.app_ariz.feature.listings.ui.detail.PropertyDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private fun dto(
    details: DetailsDto? = null,
    location: LocationDto = LocationDto(district = "Tumbes", province = "Tumbes", department = "Tumbes"),
    agent: AgentDto = AgentDto(id = "a1", name = "Ariz Inmobiliaria", type = "REAL_ESTATE", status = "APPROVED"),
) = PropertyDetailDto(
    id = "1", slug = "casa", title = "Casa", operation = "rent", type = "houses",
    price = PriceDto(1_500.0, "USD"), maintenanceFee = JsonPrimitive("80.00"),
    location = location, images = listOf("a.jpg"), publishedAt = "2023-11-14T22:13:20.000Z",
    rooms = 3, bathrooms = 2, parkingSpaces = 1, details = details, agent = agent,
)

class PropertyDetailMapperTest {

    @Test
    fun `areas como texto decimal de Prisma se leen como numero`() {
        val detail = dto(details = DetailsDto(totalArea = JsonPrimitive("120.50"), constructedArea = JsonPrimitive(90))).toDomain()

        assertEquals(120.5, detail.totalArea!!, 0.0)
        assertEquals(90.0, detail.constructedArea!!, 0.0)
        assertEquals(80.0, detail.maintenanceFee!!, 0.0)
    }

    @Test
    fun `valores vacios o cero quedan como null`() {
        val detail = dto(details = DetailsDto(totalArea = JsonPrimitive("0.00"))).toDomain()

        assertNull(detail.totalArea)
        assertNull(detail.constructedArea)
        assertNull(detail.conservation)
    }

    @Test
    fun `operacion tipo moneda y estado de conservacion`() {
        val detail = dto(details = DetailsDto(conservationStatus = "REMODELED", constructionYear = 2015)).toDomain()

        assertEquals(ListingOperation.RENT, detail.operation)
        assertEquals(PropertyTypeFilter.HOUSE, detail.type)
        assertEquals(Currency.USD, detail.currency)
        assertEquals(ConservationStatus.REMODELED, detail.conservation)
        assertEquals(2015, detail.constructionYear)
        assertTrue(detail.isRent)
    }

    @Test
    fun `ubicacion sin direccion es aproximada y sin coordenadas`() {
        val detail = dto(location = LocationDto(district = "Zarumilla", province = "Zarumilla", department = "Tumbes")).toDomain()

        assertNull(detail.location.address)
        assertFalse(detail.location.hasCoordinates)
        assertEquals("Zarumilla, Zarumilla, Tumbes", detail.location.line)
    }

    @Test
    fun `anunciante aprobado es verificado y el tipo se reconoce`() {
        val detail = dto().toDomain()

        assertTrue(detail.advertiser.isVerified)
        assertEquals(AgentType.REAL_ESTATE, detail.advertiser.type)

        val pending = dto(agent = AgentDto(name = "X", type = "ALGO_NUEVO", status = "PENDING")).toDomain()
        assertFalse(pending.advertiser.isVerified)
        assertEquals(AgentType.OTHER, pending.advertiser.type)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyDetailViewModelTest {

    private val current = dto().toDomain()

    private fun summary(id: String, price: Double) = PropertySummary(
        id = id, slug = id, title = id, description = "", operation = ListingOperation.RENT, price = price,
        currency = Currency.USD, district = "", department = "", coverImageUrl = null, photoCount = 0,
        areaM2 = 0.0, bedrooms = 0, bathrooms = 0, parkingSpaces = 0, publishedAtMillis = null,
    )

    private class FakeCatalog(val items: List<PropertySummary>, val fail: Boolean = false) : CatalogRepository {
        var lastFilters: PropertyFilters? = null
        override suspend fun searchProperties(filters: PropertyFilters, pageSize: Int, page: Int): AppResult<Page<PropertySummary>> {
            lastFilters = filters
            return if (fail) AppResult.Failure(AppError.Network()) else AppResult.Success(Page(items, items.size, 1, false))
        }
        override suspend fun recentProperties(pageSize: Int, page: Int) = error("no se usa")
        override suspend fun searchProjects(filters: ProjectFilters, pageSize: Int, page: Int): AppResult<Page<ProjectSummary>> = error("no se usa")
        override suspend fun projects(pageSize: Int, origin: ProjectOrigin?, sort: ProjectSort?, page: Int): AppResult<Page<ProjectSummary>> =
            error("no se usa")
    }

    private class FakeListings(var result: AppResult<PropertyDetail>) : ListingsRepository {
        var requestedSlug: String? = null
        var alertResult: AppResult<Unit> = AppResult.Success(Unit)
        var alertPropertyId: String? = null
        override suspend fun createPriceAlert(propertyId: String): AppResult<Unit> {
            alertPropertyId = propertyId
            return alertResult
        }
        override suspend fun getPropertyDetail(slug: String): AppResult<PropertyDetail> {
            requestedSlug = slug
            return result
        }
    }

    private class FakeSession(initial: SessionState = SessionState.SignedOut) : SessionManager {
        private val _state = MutableStateFlow(initial)
        override val state: StateFlow<SessionState> = _state
        override suspend fun restore() = Unit
        override fun onSignedIn(user: User) = Unit
        override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
        override fun onSessionExpired() = Unit
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `similares excluyen la actual, ordenan por precio cercano y son maximo 5`() = runTest {
        val items = listOf(
            summary("1", 1_500.0), summary("far", 9_000.0), summary("near", 1_450.0),
            summary("a", 1_600.0), summary("b", 1_700.0), summary("c", 1_800.0), summary("d", 1_900.0),
        )
        val catalog = FakeCatalog(items)

        val similar = GetSimilarPropertiesUseCase(catalog)(current)

        assertEquals(listOf("near", "a", "b", "c", "d"), similar.map { it.id })
        assertEquals(ListingOperation.RENT, catalog.lastFilters?.operation)
        assertEquals(PropertyTypeFilter.HOUSE, catalog.lastFilters?.type)
    }

    @Test
    fun `si falla la busqueda de similares no hay similares`() = runTest {
        assertTrue(GetSimilarPropertiesUseCase(FakeCatalog(emptyList(), fail = true))(current).isEmpty())
    }

    @Test
    fun `el ViewModel pide el slug de la ruta y carga detalle y similares`() {
        val listings = FakeListings(AppResult.Success(current))
        val viewModel = PropertyDetailViewModel(
            SavedStateHandle(mapOf(PropertyDetailViewModel.SLUG_ARG to "casa")),
            GetPropertyDetailUseCase(listings),
            GetSimilarPropertiesUseCase(FakeCatalog(listOf(summary("x", 1_400.0)))),
            CreatePriceAlertUseCase(listings),
            FakeSession(),
        )

        assertEquals("casa", listings.requestedSlug)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(current, viewModel.state.value.detail)
        assertEquals(listOf("x"), viewModel.state.value.similar.map { it.id })
    }

    @Test
    fun `error de carga se muestra y reintentar lo recupera`() {
        val listings = FakeListings(AppResult.Failure(AppError.NotFound()))
        val viewModel = PropertyDetailViewModel(
            SavedStateHandle(mapOf(PropertyDetailViewModel.SLUG_ARG to "casa")),
            GetPropertyDetailUseCase(listings),
            GetSimilarPropertiesUseCase(FakeCatalog(emptyList())),
            CreatePriceAlertUseCase(listings),
            FakeSession(),
        )
        assertNotNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.detail)

        listings.result = AppResult.Success(current)
        viewModel.onAction(PropertyDetailAction.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals(current, viewModel.state.value.detail)
    }

    private fun alertViewModel(session: SessionState, listings: FakeListings): Pair<PropertyDetailViewModel, FakeSession> {
        val fakeSession = FakeSession(session)
        val viewModel = PropertyDetailViewModel(
            SavedStateHandle(mapOf(PropertyDetailViewModel.SLUG_ARG to "casa")),
            GetPropertyDetailUseCase(listings),
            GetSimilarPropertiesUseCase(FakeCatalog(emptyList())),
            CreatePriceAlertUseCase(listings),
            fakeSession,
        )
        return viewModel to fakeSession
    }

    @Test
    fun `el aviso de precio con sesion se crea y queda creado`() {
        val listings = FakeListings(AppResult.Success(current))
        val (viewModel, _) = alertViewModel(SessionState.SignedIn(User("u1", "Ana", "ana@correo.com", Role.USER, null)), listings)

        viewModel.onAction(PropertyDetailAction.CreatePriceAlert)

        assertEquals(current.id, listings.alertPropertyId)
        assertEquals(PriceAlertStatus.CREATED, viewModel.state.value.priceAlert)
    }

    @Test
    fun `el aviso de precio sin sesion pide iniciar sesion y no llama al backend`() = runTest {
        val listings = FakeListings(AppResult.Success(current))
        val (viewModel, _) = alertViewModel(SessionState.SignedOut, listings)

        viewModel.onAction(PropertyDetailAction.CreatePriceAlert)

        assertEquals(PropertyDetailEvent.LoginRequired, viewModel.events.first())
        assertNull(listings.alertPropertyId)
        assertEquals(PriceAlertStatus.IDLE, viewModel.state.value.priceAlert)
    }

    @Test
    fun `si el aviso falla queda en error y se puede reintentar`() {
        val listings = FakeListings(AppResult.Success(current))
        listings.alertResult = AppResult.Failure(AppError.Network())
        val (viewModel, _) = alertViewModel(SessionState.SignedIn(User("u1", "Ana", "ana@correo.com", Role.USER, null)), listings)

        viewModel.onAction(PropertyDetailAction.CreatePriceAlert)
        assertEquals(PriceAlertStatus.FAILED, viewModel.state.value.priceAlert)

        listings.alertResult = AppResult.Success(Unit)
        viewModel.onAction(PropertyDetailAction.CreatePriceAlert)
        assertEquals(PriceAlertStatus.CREATED, viewModel.state.value.priceAlert)
    }
}
