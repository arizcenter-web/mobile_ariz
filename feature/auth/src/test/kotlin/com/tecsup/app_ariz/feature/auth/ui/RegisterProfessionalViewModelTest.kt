package com.tecsup.app_ariz.feature.auth.ui

import com.tecsup.app_ariz.feature.auth.domain.model.ProfessionalType
import com.tecsup.app_ariz.feature.auth.domain.usecase.RegisterProfessionalUseCase
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError
import com.tecsup.app_ariz.feature.auth.fakes.FakeAuthRepository
import com.tecsup.app_ariz.feature.auth.fakes.FakeSessionManager
import com.tecsup.app_ariz.feature.auth.ui.registerprofessional.ProfessionalField
import com.tecsup.app_ariz.feature.auth.ui.registerprofessional.RegisterProfessionalAction
import com.tecsup.app_ariz.feature.auth.ui.registerprofessional.RegisterProfessionalViewModel
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
class RegisterProfessionalViewModelTest {

    private val repository = FakeAuthRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = RegisterProfessionalViewModel(
        RegisterProfessionalUseCase(repository, FakeSessionManager()),
    )

    private fun RegisterProfessionalViewModel.fill(vararg values: Pair<ProfessionalField, String>) {
        values.forEach { (field, value) -> onAction(RegisterProfessionalAction.FieldChanged(field, value)) }
    }

    private fun RegisterProfessionalViewModel.fillCommonFields() = fill(
        ProfessionalField.EMAIL to "ventas@sol.pe",
        ProfessionalField.PHONE to "987654321",
        ProfessionalField.WHATSAPP to "987654321",
        ProfessionalField.CITY to "Lima",
        ProfessionalField.PASSWORD to "clave1234",
    )

    @Test
    fun `persona natural exige nombre apellido y DNI`() {
        val viewModel = viewModel()

        viewModel.onAction(RegisterProfessionalAction.Submit)

        val errors = viewModel.state.value.fieldErrors
        assertEquals(FieldError.REQUIRED, errors[ProfessionalField.FIRST_NAME])
        assertEquals(FieldError.REQUIRED, errors[ProfessionalField.DNI])
        assertNull(errors[ProfessionalField.RUC]) // no aplica a una persona
    }

    @Test
    fun `empresa exige razon social y RUC en lugar de nombre y DNI`() {
        val viewModel = viewModel()
        viewModel.onAction(RegisterProfessionalAction.TypeChanged(ProfessionalType.DEVELOPER))

        viewModel.onAction(RegisterProfessionalAction.Submit)

        val errors = viewModel.state.value.fieldErrors
        assertEquals(FieldError.REQUIRED, errors[ProfessionalField.BUSINESS_NAME])
        assertEquals(FieldError.REQUIRED, errors[ProfessionalField.RUC])
        assertNull(errors[ProfessionalField.FIRST_NAME])
    }

    @Test
    fun `sin aceptar terminos no se envia`() {
        val viewModel = viewModel()
        viewModel.onAction(RegisterProfessionalAction.TypeChanged(ProfessionalType.REAL_ESTATE_AGENCY))
        viewModel.fill(ProfessionalField.BUSINESS_NAME to "Inmobiliaria Sol", ProfessionalField.RUC to "20123456789")
        viewModel.fillCommonFields()

        viewModel.onAction(RegisterProfessionalAction.Submit)

        assertEquals(FieldError.TERMS_NOT_ACCEPTED, viewModel.state.value.termsError)
        assertNull(repository.lastProfessional)
    }

    @Test
    fun `empresa valida se envia sin campos de persona`() {
        val viewModel = viewModel()
        viewModel.onAction(RegisterProfessionalAction.TypeChanged(ProfessionalType.REAL_ESTATE_AGENCY))
        viewModel.fill(ProfessionalField.BUSINESS_NAME to "Inmobiliaria Sol", ProfessionalField.RUC to "20123456789")
        viewModel.fillCommonFields()
        viewModel.onAction(RegisterProfessionalAction.TermsChanged(true))

        viewModel.onAction(RegisterProfessionalAction.Submit)

        val sent = repository.lastProfessional
        assertNotNull(sent)
        assertEquals(ProfessionalType.REAL_ESTATE_AGENCY, sent!!.type)
        assertEquals("Inmobiliaria Sol", sent.businessName)
        assertNull(sent.firstName)
        assertNull(sent.dni)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.fieldErrors.isEmpty())
    }
}
