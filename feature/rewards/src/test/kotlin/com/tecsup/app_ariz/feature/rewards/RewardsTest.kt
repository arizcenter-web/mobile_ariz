package com.tecsup.app_ariz.feature.rewards

import androidx.lifecycle.SavedStateHandle
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.rewards.data.mapper.toBody
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignPoints
import com.tecsup.app_ariz.feature.rewards.domain.model.CodeAccess
import com.tecsup.app_ariz.feature.rewards.domain.model.Incentive
import com.tecsup.app_ariz.feature.rewards.domain.model.Redemption
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralAvailability
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralInterest
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralRequest
import com.tecsup.app_ariz.feature.rewards.domain.model.Referrer
import com.tecsup.app_ariz.feature.rewards.domain.repository.RewardsRepository
import com.tecsup.app_ariz.feature.rewards.domain.usecase.LoadClubUseCase
import com.tecsup.app_ariz.feature.rewards.domain.usecase.RedeemIncentiveUseCase
import com.tecsup.app_ariz.feature.rewards.domain.usecase.SubmitReferralUseCase
import com.tecsup.app_ariz.feature.rewards.domain.usecase.ValidateReferrerCodeUseCase
import com.tecsup.app_ariz.feature.rewards.domain.validation.ReferralFieldError
import com.tecsup.app_ariz.feature.rewards.domain.validation.ReferralValidator
import com.tecsup.app_ariz.feature.rewards.ui.club.ClubAction
import com.tecsup.app_ariz.feature.rewards.ui.club.ClubViewModel
import com.tecsup.app_ariz.feature.rewards.ui.club.RedeemOutcome
import com.tecsup.app_ariz.feature.rewards.ui.referral.ReferralField
import com.tecsup.app_ariz.feature.rewards.ui.referral.ReferralFormAction
import com.tecsup.app_ariz.feature.rewards.ui.referral.ReferralFormViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private val referrer = Referrer("r1", "APCOD-AAA", "Ana Pérez", "ana@correo.com", "51987654321", totalPoints = 500)
private fun incentive(id: String, points: Int, stock: Int = 5) = Incentive(id, "Premio $id", "", null, null, points, stock)

private class FakeRewards : RewardsRepository {
    var access: AppResult<CodeAccess> = AppResult.Success(CodeAccess.Valid(referrer))
    var catalog: AppResult<List<Incentive>> = AppResult.Success(listOf(incentive("i1", 300), incentive("i2", 900)))
    var redeemResult: AppResult<Unit> = AppResult.Success(Unit)
    var validatedCode: String? = null
    var redeemed: Pair<String, String>? = null
    var submitted: ReferralRequest? = null
    var submitResult: AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun validateCode(code: String): AppResult<CodeAccess> {
        validatedCode = code
        return access
    }
    override suspend fun getCatalog() = catalog
    override suspend fun redeem(code: String, incentiveId: String): AppResult<Unit> {
        redeemed = code to incentiveId
        return redeemResult
    }
    override suspend fun getMyRedemptions(code: String): AppResult<List<Redemption>> = AppResult.Failure(AppError.Network())
    override suspend fun getCampaignPoints(code: String): AppResult<List<CampaignPoints>> = AppResult.Failure(AppError.Network())
    override suspend fun submitReferral(request: ReferralRequest): AppResult<Unit> {
        submitted = request
        return submitResult
    }
}

class ReferralValidatorTest {

    @Test
    fun `celular peruano de 9 digitos que empieza en 9 y se convierte a formato internacional`() {
        assertNull(ReferralValidator.validatePhone("923 655 566"))
        assertEquals(ReferralFieldError.INVALID_PHONE, ReferralValidator.validatePhone("823655566"))
        assertEquals(ReferralFieldError.INVALID_PHONE, ReferralValidator.validatePhone("92365556"))
        assertEquals(ReferralFieldError.REQUIRED, ReferralValidator.validatePhone(""))
        assertEquals("51923655566", ReferralValidator.toInternationalPhone("923 655 566"))
    }

    @Test
    fun `correo real con dominio y TLD`() {
        assertNull(ReferralValidator.validateEmail("ana.perez@correo.com"))
        assertEquals(ReferralFieldError.INVALID_EMAIL, ReferralValidator.validateEmail("ana@correo"))
        assertEquals(ReferralFieldError.INVALID_EMAIL, ReferralValidator.validateEmail("ana..perez@correo.com"))
        assertEquals(ReferralFieldError.REQUIRED, ReferralValidator.validateEmail(" "))
    }

    @Test
    fun `el cuerpo del referido normaliza el codigo y omite los opcionales vacios`() {
        val body = ReferralRequest(
            referrerCode = " apcod-aaa ", referrerName = " Ana ", referrerEmail = "ana@correo.com", referrerPhone = "51987654321",
            name = "Luis", email = "luis@correo.com", phone = "51923655566", interest = ReferralInterest.COMPRA_LOTE, availability = null,
        ).toBody()

        assertEquals("APCOD-AAA", body.referrerCode)
        assertEquals("Ana", body.referrerName)
        assertEquals("COMPRA_LOTE", body.interest)
        assertNull(body.availability)
    }
}

class RedeemIncentiveUseCaseTest {

    private val repo = FakeRewards()
    private val useCase = RedeemIncentiveUseCase(repo)

    @Test
    fun `sin puntos suficientes no llama al backend`() = runTest {
        val result = useCase("APCOD-AAA", incentive("i2", 900), availablePoints = 500)

        assertTrue((result as AppResult.Failure).error is AppError.Validation)
        assertNull(repo.redeemed)
    }

    @Test
    fun `sin stock no llama al backend y con puntos y stock canjea`() = runTest {
        assertTrue(useCase("APCOD-AAA", incentive("i1", 100, stock = 0), 500) is AppResult.Failure)
        assertNull(repo.redeemed)

        assertTrue(useCase("APCOD-AAA", incentive("i1", 300), 500) is AppResult.Success)
        assertEquals("APCOD-AAA" to "i1", repo.redeemed)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ClubViewModelTest {

    private val repo = FakeRewards()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(code: String? = null) = ClubViewModel(
        SavedStateHandle(if (code == null) emptyMap() else mapOf(ClubViewModel.CODE_ARG to code)),
        ValidateReferrerCodeUseCase(repo),
        LoadClubUseCase(repo),
        RedeemIncentiveUseCase(repo),
    )

    @Test
    fun `un codigo vacio no llama al backend`() {
        val viewModel = viewModel()

        viewModel.onAction(ClubAction.ValidateCode)

        assertEquals(ClubViewModel.EMPTY_CODE, viewModel.state.value.codeError)
        assertNull(repo.validatedCode)
    }

    @Test
    fun `un codigo valido entra al club, carga el catalogo y los extras fallidos quedan vacios`() {
        val viewModel = viewModel()
        viewModel.onAction(ClubAction.CodeChanged("apcod-aaa"))

        viewModel.onAction(ClubAction.ValidateCode)

        val state = viewModel.state.value
        assertEquals("APCOD-AAA", repo.validatedCode)
        assertTrue(state.isInClub)
        assertEquals(500, state.points)
        assertEquals(2, state.incentives.size)
        assertTrue(state.redemptions.isEmpty())
        assertTrue(state.campaigns.isEmpty())
    }

    @Test
    fun `el codigo de la ruta entra directo y un codigo invalido muestra el mensaje`() {
        assertTrue(viewModel(code = "apcod-aaa").state.value.isInClub)

        repo.access = AppResult.Success(CodeAccess.Invalid("Código de referente no válido"))
        val invalid = viewModel(code = "zzz")
        assertFalse(invalid.state.value.isInClub)
        assertEquals("Código de referente no válido", invalid.state.value.codeError)
    }

    @Test
    fun `canjear descuenta los puntos y avisa del exito`() {
        val viewModel = viewModel(code = "APCOD-AAA")

        viewModel.onAction(ClubAction.RedeemRequested(incentive("i1", 300)))
        viewModel.onAction(ClubAction.RedeemConfirmed)

        assertEquals(200, viewModel.state.value.points)
        assertEquals(RedeemOutcome.Success, viewModel.state.value.redeemOutcome)
        assertNull(viewModel.state.value.pendingRedeem)
    }

    @Test
    fun `un canje que falla avisa y salir del club vuelve al paso del codigo`() {
        repo.redeemResult = AppResult.Failure(AppError.Validation("No hay stock"))
        val viewModel = viewModel(code = "APCOD-AAA")

        viewModel.onAction(ClubAction.RedeemRequested(incentive("i1", 300)))
        viewModel.onAction(ClubAction.RedeemConfirmed)
        assertEquals(RedeemOutcome.Failure("No hay stock"), viewModel.state.value.redeemOutcome)
        assertEquals(500, viewModel.state.value.points)

        viewModel.onAction(ClubAction.LeaveClub)
        assertFalse(viewModel.state.value.isInClub)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReferralFormViewModelTest {

    private val repo = FakeRewards()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ReferralFormViewModel(SubmitReferralUseCase(repo))

    private fun ReferralFormViewModel.fillReferred() {
        onAction(ReferralFormAction.FieldChanged(ReferralField.NAME, "Luis"))
        onAction(ReferralFormAction.FieldChanged(ReferralField.EMAIL, "luis@correo.com"))
        onAction(ReferralFormAction.FieldChanged(ReferralField.PHONE, "923 655 566"))
        onAction(ReferralFormAction.InterestChanged(ReferralInterest.COMPRA))
    }

    @Test
    fun `un miembro del club solo da los datos del referido y se envian los del referente`() {
        val viewModel = viewModel()
        viewModel.fillReferred()

        viewModel.onAction(ReferralFormAction.Submit(referrer))

        val sent = repo.submitted!!
        assertEquals("APCOD-AAA", sent.referrerCode)
        assertEquals("ana@correo.com", sent.referrerEmail)
        assertEquals("51987654321", sent.referrerPhone)
        assertEquals("51923655566", sent.phone)
        assertTrue(viewModel.state.value.sent)
    }

    @Test
    fun `quien se une al club tambien debe dar sus propios datos`() {
        val viewModel = viewModel()
        viewModel.fillReferred()

        viewModel.onAction(ReferralFormAction.Submit(null))

        assertNull(repo.submitted)
        assertEquals(ReferralFieldError.REQUIRED, viewModel.state.value.errors[ReferralField.REFERRER_NAME])
        assertEquals(ReferralFieldError.REQUIRED, viewModel.state.value.errors[ReferralField.REFERRER_EMAIL])
        assertEquals(ReferralFieldError.REQUIRED, viewModel.state.value.errors[ReferralField.REFERRER_PHONE])

        viewModel.onAction(ReferralFormAction.FieldChanged(ReferralField.REFERRER_NAME, "Ana"))
        viewModel.onAction(ReferralFormAction.FieldChanged(ReferralField.REFERRER_EMAIL, "ana@correo.com"))
        viewModel.onAction(ReferralFormAction.FieldChanged(ReferralField.REFERRER_PHONE, "987654321"))
        viewModel.onAction(ReferralFormAction.AvailabilityChanged(ReferralAvailability.INMEDIATA))
        viewModel.onAction(ReferralFormAction.Submit(null))

        assertNull(repo.submitted!!.referrerCode)
        assertEquals("51987654321", repo.submitted!!.referrerPhone)
        assertEquals(ReferralAvailability.INMEDIATA, repo.submitted!!.availability)
    }

    @Test
    fun `sin interes o con celular invalido marca los errores y un error del backend se muestra`() {
        val viewModel = viewModel()
        viewModel.onAction(ReferralFormAction.FieldChanged(ReferralField.NAME, "Luis"))
        viewModel.onAction(ReferralFormAction.FieldChanged(ReferralField.EMAIL, "luis@correo.com"))
        viewModel.onAction(ReferralFormAction.FieldChanged(ReferralField.PHONE, "123"))

        viewModel.onAction(ReferralFormAction.Submit(referrer))
        assertEquals(ReferralFieldError.INVALID_PHONE, viewModel.state.value.errors[ReferralField.PHONE])
        assertEquals(ReferralFieldError.INTEREST_REQUIRED, viewModel.state.value.errors[ReferralField.INTEREST])
        assertNull(repo.submitted)

        repo.submitResult = AppResult.Failure(AppError.Network())
        viewModel.fillReferred()
        viewModel.onAction(ReferralFormAction.Submit(referrer))
        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.sent)
    }
}
