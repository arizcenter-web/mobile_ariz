package com.tecsup.app_ariz.feature.auth.ui

import android.content.Context
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.usecase.LoginUseCase
import com.tecsup.app_ariz.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError
import com.tecsup.app_ariz.feature.auth.fakes.FakeAuthRepository
import com.tecsup.app_ariz.feature.auth.fakes.FakeGoogleIdTokenProvider
import com.tecsup.app_ariz.feature.auth.fakes.FakeSessionManager
import com.tecsup.app_ariz.feature.auth.fakes.testUser
import com.tecsup.app_ariz.feature.auth.ui.login.LoginAction
import com.tecsup.app_ariz.feature.auth.ui.login.LoginEvent
import com.tecsup.app_ariz.feature.auth.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val repository = FakeAuthRepository()
    private val session = FakeSessionManager()
    private val google = FakeGoogleIdTokenProvider()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LoginViewModel(
        LoginUseCase(repository, session),
        SignInWithGoogleUseCase(google, repository, session),
    )

    private fun LoginViewModel.fillValidCredentials() {
        onAction(LoginAction.EmailChanged("ana@correo.com"))
        onAction(LoginAction.PasswordChanged("clave1234"))
    }

    @Test
    fun `campos invalidos muestran errores y no llaman al repositorio`() {
        val viewModel = viewModel()

        viewModel.onAction(LoginAction.EmailChanged("no-es-correo"))
        viewModel.onAction(LoginAction.Submit)

        assertEquals(FieldError.INVALID_EMAIL, viewModel.state.value.emailError)
        assertEquals(FieldError.REQUIRED, viewModel.state.value.passwordError)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `login exitoso emite Authenticated y registra la sesion`() = runTest {
        val viewModel = viewModel()
        viewModel.fillValidCredentials()

        viewModel.onAction(LoginAction.Submit)

        assertEquals(LoginEvent.Authenticated, viewModel.events.first())
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(testUser, session.signedInUser)
    }

    @Test
    fun `credenciales rechazadas dejan el error y no abren sesion`() {
        val error = AppError.Unauthorized("Credenciales inválidas")
        repository.result = AppResult.Failure(error)
        val viewModel = viewModel()
        viewModel.fillValidCredentials()

        viewModel.onAction(LoginAction.Submit)

        assertEquals(error, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(session.signedInUser)
    }

    @Test
    fun `editar un campo limpia el error general`() {
        repository.result = AppResult.Failure(AppError.Network())
        val viewModel = viewModel()
        viewModel.fillValidCredentials()
        viewModel.onAction(LoginAction.Submit)

        viewModel.onAction(LoginAction.PasswordChanged("otra1234"))

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `el boton de Google depende de que Firebase este configurado`() {
        assertTrue(viewModel().state.value.isGoogleAvailable)

        val withoutGoogle = LoginViewModel(
            LoginUseCase(repository, session),
            SignInWithGoogleUseCase(FakeGoogleIdTokenProvider(isConfigured = false), repository, session),
        )
        assertFalse(withoutGoogle.state.value.isGoogleAvailable)
    }

    @Test
    fun `login con Google envia el token de Firebase y emite Authenticated`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(LoginAction.GoogleSignIn(Mockito.mock(Context::class.java)))

        assertEquals(LoginEvent.Authenticated, viewModel.events.first())
        assertEquals("firebase-token", repository.lastGoogleToken)
        assertEquals(testUser, session.signedInUser)
    }

    @Test
    fun `cancelar el selector de Google no muestra error`() {
        google.tokenResult = AppResult.Failure(AppError.Cancelled)
        val viewModel = viewModel()

        viewModel.onAction(LoginAction.GoogleSignIn(Mockito.mock(Context::class.java)))

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(repository.lastGoogleToken)
    }
}
