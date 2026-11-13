package com.tecsup.app_ariz.feature.auth.ui

import androidx.lifecycle.SavedStateHandle
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.usecase.ResetPasswordUseCase
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError
import com.tecsup.app_ariz.feature.auth.fakes.FakeAuthRepository
import com.tecsup.app_ariz.feature.auth.ui.resetpassword.ResetPasswordAction
import com.tecsup.app_ariz.feature.auth.ui.resetpassword.ResetPasswordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    private val repository = FakeAuthRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(token: String = "tok-del-correo") = ResetPasswordViewModel(
        SavedStateHandle(mapOf(ResetPasswordViewModel.TOKEN_ARG to token)),
        ResetPasswordUseCase(repository),
    )

    @Test
    fun `contraseña debil o distinta no llama al backend`() {
        val viewModel = viewModel()

        viewModel.onAction(ResetPasswordAction.PasswordChanged("corta"))
        viewModel.onAction(ResetPasswordAction.ConfirmationChanged("otra"))
        viewModel.onAction(ResetPasswordAction.Submit)

        assertEquals(FieldError.WEAK_PASSWORD, viewModel.state.value.passwordError)
        assertEquals(FieldError.PASSWORDS_MISMATCH, viewModel.state.value.confirmationError)
        assertNull(repository.lastReset)
    }

    @Test
    fun `envia el token del enlace junto con la contraseña nueva`() {
        val viewModel = viewModel(token = "abc123")

        viewModel.onAction(ResetPasswordAction.PasswordChanged("clave1234"))
        viewModel.onAction(ResetPasswordAction.ConfirmationChanged("clave1234"))
        viewModel.onAction(ResetPasswordAction.Submit)

        assertEquals("abc123" to "clave1234", repository.lastReset)
        assertTrue(viewModel.state.value.isDone)
    }

    @Test
    fun `enlace vencido muestra el error del backend`() {
        val error = AppError.Validation("El enlace no es válido o ya venció. Solicita uno nuevo.")
        repository.unitResult = AppResult.Failure(error)
        val viewModel = viewModel()

        viewModel.onAction(ResetPasswordAction.PasswordChanged("clave1234"))
        viewModel.onAction(ResetPasswordAction.ConfirmationChanged("clave1234"))
        viewModel.onAction(ResetPasswordAction.Submit)

        assertEquals(error, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isDone)
    }
}
