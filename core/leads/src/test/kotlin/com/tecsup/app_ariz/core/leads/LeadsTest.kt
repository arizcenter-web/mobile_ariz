package com.tecsup.app_ariz.core.leads

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.leads.data.toBody
import com.tecsup.app_ariz.core.leads.domain.LeadFieldError
import com.tecsup.app_ariz.core.leads.domain.LeadRequest
import com.tecsup.app_ariz.core.leads.domain.LeadTarget
import com.tecsup.app_ariz.core.leads.domain.LeadValidator
import com.tecsup.app_ariz.core.leads.domain.LeadsRepository
import com.tecsup.app_ariz.core.leads.ui.LeadFormAction
import com.tecsup.app_ariz.core.leads.ui.LeadFormViewModel
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

class LeadValidatorTest {

    @Test
    fun `nombre correo y mensaje son obligatorios`() {
        assertEquals(LeadFieldError.REQUIRED, LeadValidator.validateName("  "))
        assertEquals(LeadFieldError.REQUIRED, LeadValidator.validateEmail(""))
        assertEquals(LeadFieldError.REQUIRED, LeadValidator.validateMessage(" "))
        assertNull(LeadValidator.validateName("Ana"))
    }

    @Test
    fun `correo con formato invalido y terminos sin aceptar`() {
        assertEquals(LeadFieldError.INVALID_EMAIL, LeadValidator.validateEmail("ana@correo"))
        assertNull(LeadValidator.validateEmail(" ana@correo.com "))
        assertEquals(LeadFieldError.TERMS_NOT_ACCEPTED, LeadValidator.validateTerms(false))
        assertNull(LeadValidator.validateTerms(true))
    }

    @Test
    fun `el DNI se anexa al mensaje como hace el web`() {
        assertEquals("Hola", LeadValidator.messageWithDni(" Hola ", " "))
        assertEquals("Hola\n\nDNI: 12345678", LeadValidator.messageWithDni("Hola", "12345678"))
    }
}

class LeadRequestMappingTest {

    @Test
    fun `propiedad usa propertyId y PROPERTY_FORM, y el telefono vacio no se envia`() {
        val body = LeadRequest(LeadTarget.Property("p1"), " Ana ", " ana@correo.com ", "  ", "", "Hola").toBody()

        assertEquals("p1", body.propertyId)
        assertNull(body.projectId)
        assertEquals("PROPERTY_FORM", body.source)
        assertEquals("Ana", body.name)
        assertEquals("ana@correo.com", body.email)
        assertNull(body.phone)
    }

    @Test
    fun `proyecto usa projectId y PROJECT_FORM`() {
        val body = LeadRequest(LeadTarget.Project("j1"), "Ana", "ana@correo.com", "999", "12345678", "Hola").toBody()

        assertEquals("j1", body.projectId)
        assertNull(body.propertyId)
        assertEquals("PROJECT_FORM", body.source)
        assertEquals("Hola\n\nDNI: 12345678", body.message)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LeadFormViewModelTest {

    private class FakeRepo : LeadsRepository {
        var sent: LeadRequest? = null
        var result: AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun send(request: LeadRequest): AppResult<Unit> {
            sent = request
            return result
        }
    }

    private class FakeSession(initial: SessionState) : SessionManager {
        override val state: StateFlow<SessionState> = MutableStateFlow(initial)
        override suspend fun restore() = Unit
        override fun onSignedIn(user: User) = Unit
        override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
        override fun onSessionExpired() = Unit
    }

    private val repo = FakeRepo()
    private val target = LeadTarget.Property("p1")

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(session: SessionState = SessionState.SignedOut) = LeadFormViewModel(repo, FakeSession(session))

    @Test
    fun `con sesion se precargan nombre y correo y el mensaje sugerido solo si esta vacio`() {
        val viewModel = viewModel(SessionState.SignedIn(User("u1", "Ana Pérez", "ana@correo.com", Role.USER, null)))

        viewModel.prepare("Hola!")
        assertEquals("Ana Pérez", viewModel.state.value.name)
        assertEquals("ana@correo.com", viewModel.state.value.email)
        assertEquals("Hola!", viewModel.state.value.message)

        viewModel.onAction(LeadFormAction.MessageChanged("Mi mensaje"))
        viewModel.prepare("Otro")
        assertEquals("Mi mensaje", viewModel.state.value.message)
    }

    @Test
    fun `enviar con campos vacios marca los errores y no llama al backend`() {
        val viewModel = viewModel()
        viewModel.onAction(LeadFormAction.TermsChanged(false))

        viewModel.onAction(LeadFormAction.Submit(target))

        val state = viewModel.state.value
        assertEquals(LeadFieldError.REQUIRED, state.nameError)
        assertEquals(LeadFieldError.REQUIRED, state.emailError)
        assertEquals(LeadFieldError.REQUIRED, state.messageError)
        assertEquals(LeadFieldError.TERMS_NOT_ACCEPTED, state.termsError)
        assertNull(repo.sent)
        assertFalse(state.sent)
    }

    @Test
    fun `un envio valido llega al backend y queda confirmado, limpiando el mensaje`() {
        val viewModel = viewModel()
        viewModel.onAction(LeadFormAction.NameChanged("Ana"))
        viewModel.onAction(LeadFormAction.EmailChanged("ana@correo.com"))
        viewModel.onAction(LeadFormAction.DniChanged("12a34"))
        viewModel.onAction(LeadFormAction.MessageChanged("Hola"))

        viewModel.onAction(LeadFormAction.Submit(target))

        assertEquals(target, repo.sent?.target)
        assertEquals("1234", repo.sent?.dni)
        assertTrue(viewModel.state.value.sent)
        assertEquals("", viewModel.state.value.message)

        viewModel.onAction(LeadFormAction.Dismissed)
        assertFalse(viewModel.state.value.sent)
    }

    @Test
    fun `un error del backend se muestra y no confirma`() {
        repo.result = AppResult.Failure(AppError.Network())
        val viewModel = viewModel()
        viewModel.onAction(LeadFormAction.NameChanged("Ana"))
        viewModel.onAction(LeadFormAction.EmailChanged("ana@correo.com"))
        viewModel.onAction(LeadFormAction.MessageChanged("Hola"))

        viewModel.onAction(LeadFormAction.Submit(target))

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.sent)
        assertFalse(viewModel.state.value.isSending)
    }
}
