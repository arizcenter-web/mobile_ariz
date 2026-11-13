package com.tecsup.app_ariz.feature.investments

import androidx.lifecycle.SavedStateHandle
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.investments.data.mapper.toDomain
import com.tecsup.app_ariz.feature.investments.data.remote.InvestmentOpportunityDto
import com.tecsup.app_ariz.feature.investments.data.remote.InvestmentTypeDto
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentGoal
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentRisk
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentStatus
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentType
import com.tecsup.app_ariz.feature.investments.domain.repository.InvestmentsRepository
import com.tecsup.app_ariz.feature.investments.domain.usecase.GetInvestmentCatalogUseCase
import com.tecsup.app_ariz.feature.investments.domain.usecase.GetInvestmentOpportunityUseCase
import com.tecsup.app_ariz.feature.investments.domain.usecase.SimulateInvestmentUseCase
import com.tecsup.app_ariz.feature.investments.ui.detail.InvestmentDetailAction
import com.tecsup.app_ariz.feature.investments.ui.detail.InvestmentDetailViewModel
import com.tecsup.app_ariz.feature.investments.ui.list.InvestmentListAction
import com.tecsup.app_ariz.feature.investments.ui.list.InvestmentListViewModel
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

private fun opportunity(
    id: String,
    initial: Double,
    roi: Double = 10.0,
    goal: InvestmentGoal = InvestmentGoal.PLUSVALIA,
    typeId: String? = "t1",
) = InvestmentOpportunity(
    id = id, typeId = typeId, typeName = "Terrenos", name = "Op $id", slug = id, summary = "", description = "",
    areaLabel = "100 m²", assetType = "Lote", initialInvestment = initial, roiPercent = roi, estimatedReturnLabel = null,
    termMonths = null, goal = goal, status = InvestmentStatus.AVAILABLE, risk = null, ctaLabel = "", location = "Tumbes",
    district = "Tumbes", province = "Tumbes", department = "Tumbes", latitude = null, longitude = null,
    coverImageUrl = null, gallery = emptyList(), highlights = emptyList(),
)

class SimulateInvestmentUseCaseTest {

    private val simulate = SimulateInvestmentUseCase()

    @Test
    fun `con 20 mil o mas el objetivo ya no importa pero el capital debe alcanzar`() {
        val ops = listOf(
            opportunity("a", 10_000.0, roi = 10.0, goal = InvestmentGoal.RENTA),
            opportunity("b", 60_000.0, roi = 20.0),
        )

        val result = simulate(ops, capital = 50_000, termMonths = 12, goal = InvestmentGoal.PLUSVALIA)

        assertEquals(listOf("a"), result.compatible.map { it.id })
        assertEquals(5_000L, result.estimatedReturn)
    }

    @Test
    fun `con poco capital el objetivo debe coincidir`() {
        val ops = listOf(
            opportunity("a", 5_000.0, goal = InvestmentGoal.RENTA),
            opportunity("b", 5_000.0, goal = InvestmentGoal.PLUSVALIA),
        )

        val result = simulate(ops, capital = 10_000, termMonths = 12, goal = InvestmentGoal.PLUSVALIA)

        assertEquals(listOf("b"), result.compatible.map { it.id })
    }

    @Test
    fun `sin compatibles asume 14 por ciento y el plazo escala el retorno`() {
        val result = simulate(emptyList(), capital = 50_000, termMonths = 24, goal = InvestmentGoal.RENTA)

        assertTrue(result.compatible.isEmpty())
        assertEquals(14_000L, result.estimatedReturn)
    }
}

class InvestmentMapperTest {

    @Test
    fun `tipo de la oportunidad, meta, estado y riesgo se interpretan`() {
        val op = InvestmentOpportunityDto(
            id = "1", investmentType = InvestmentTypeDto("t1", "Preventa"), investmentKind = "kind", investmentGoal = "renta",
            status = "COMING_SOON", riskLevel = "HIGH", estimatedTermMonths = 0, estimatedReturnLabel = " ", coverImageUrl = "",
        ).toDomain()

        assertEquals("Preventa", op.typeName)
        assertEquals(InvestmentGoal.RENTA, op.goal)
        assertEquals(InvestmentStatus.COMING_SOON, op.status)
        assertEquals(InvestmentRisk.HIGH, op.risk)
        assertNull(op.termMonths)
        assertNull(op.estimatedReturnLabel)
        assertNull(op.coverImageUrl)
    }

    @Test
    fun `sin tipo usa la clase de inversion, y el retorno cae en el ROI`() {
        val op = InvestmentOpportunityDto(id = "1", investmentKind = "Terrenos", roiPercent = 12.4).toDomain()

        assertEquals("Terrenos", op.typeName)
        assertEquals("12% ROI", op.returnLabel)
        assertEquals(InvestmentGoal.PLUSVALIA, op.goal)
        assertFalse(op.hasCoordinates)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class InvestmentViewModelsTest {

    private class FakeRepo : InvestmentsRepository {
        var opportunities: AppResult<List<InvestmentOpportunity>> =
            AppResult.Success(listOf(opportunity("a", 10_000.0, typeId = "t1"), opportunity("b", 80_000.0, typeId = "t2")))
        var types: AppResult<List<InvestmentType>> = AppResult.Success(listOf(InvestmentType("t1", "Terrenos", null)))
        var detail: AppResult<InvestmentOpportunity> = AppResult.Failure(AppError.NotFound())
        var requestedSlug: String? = null

        override suspend fun getOpportunities() = opportunities
        override suspend fun getTypes() = types
        override suspend fun getOpportunity(slug: String): AppResult<InvestmentOpportunity> {
            requestedSlug = slug
            return detail
        }
    }

    private val repo = FakeRepo()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun listViewModel() = InvestmentListViewModel(GetInvestmentCatalogUseCase(repo), SimulateInvestmentUseCase())

    @Test
    fun `carga el catalogo y calcula el evaluador con los valores iniciales`() {
        val state = listViewModel().state.value

        assertFalse(state.isLoading)
        assertEquals(2, state.opportunities.size)
        assertEquals(1, state.types.size)
        assertEquals(listOf("a"), state.simulation.compatible.map { it.id })
    }

    @Test
    fun `elegir una categoria filtra y mover el capital recalcula los compatibles`() {
        val viewModel = listViewModel()

        viewModel.onAction(InvestmentListAction.TypeSelected("t2"))
        assertEquals(listOf("b"), viewModel.state.value.visible.map { it.id })
        assertTrue(viewModel.state.value.simulation.compatible.isEmpty())

        viewModel.onAction(InvestmentListAction.CapitalChanged(100_000))
        assertEquals(listOf("b"), viewModel.state.value.simulation.compatible.map { it.id })

        viewModel.onAction(InvestmentListAction.TypeSelected(null))
        assertEquals(2, viewModel.state.value.visible.size)
    }

    @Test
    fun `si fallan las categorias el catalogo igual se muestra, y si fallan las oportunidades hay error`() {
        repo.types = AppResult.Failure(AppError.Network())
        assertTrue(listViewModel().state.value.types.isEmpty())
        assertEquals(2, listViewModel().state.value.opportunities.size)

        repo.opportunities = AppResult.Failure(AppError.Network())
        val failed = listViewModel()
        assertNotNull(failed.state.value.error)

        repo.opportunities = AppResult.Success(listOf(opportunity("a", 1.0)))
        failed.onAction(InvestmentListAction.Retry)
        assertNull(failed.state.value.error)
    }

    @Test
    fun `el detalle pide el slug de la ruta y reintentar lo recupera`() {
        val viewModel = InvestmentDetailViewModel(
            SavedStateHandle(mapOf(InvestmentDetailViewModel.SLUG_ARG to "mi-op")),
            GetInvestmentOpportunityUseCase(repo),
        )
        assertEquals("mi-op", repo.requestedSlug)
        assertNotNull(viewModel.state.value.error)

        repo.detail = AppResult.Success(opportunity("mi-op", 1.0))
        viewModel.onAction(InvestmentDetailAction.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals("mi-op", viewModel.state.value.opportunity?.slug)
    }
}
