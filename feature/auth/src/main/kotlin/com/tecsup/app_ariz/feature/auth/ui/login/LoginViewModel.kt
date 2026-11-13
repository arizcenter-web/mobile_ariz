package com.tecsup.app_ariz.feature.auth.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.feature.auth.domain.usecase.LoginUseCase
import com.tecsup.app_ariz.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.tecsup.app_ariz.feature.auth.domain.validation.AuthValidators
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState(isGoogleAvailable = signInWithGoogleUseCase.isAvailable))
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged ->
                _state.update { it.copy(email = action.value, emailError = null, error = null) }
            is LoginAction.PasswordChanged ->
                _state.update { it.copy(password = action.value, passwordError = null, error = null) }
            is LoginAction.RememberMeChanged ->
                _state.update { it.copy(rememberMe = action.value) }
            LoginAction.Submit -> submit()
            is LoginAction.GoogleSignIn -> signInWithGoogle(action.activityContext)
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLoading) return

        val emailError = AuthValidators.validateEmail(current.email)
        val passwordError = if (current.password.isEmpty()) FieldError.REQUIRED else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        launchAuthentication { loginUseCase(current.email, current.password, current.rememberMe) }
    }

    private fun signInWithGoogle(activityContext: Context) {
        if (_state.value.isLoading) return
        launchAuthentication { signInWithGoogleUseCase(activityContext) }
    }

    /** Ejecuta un intento de autenticación gestionando carga, error y el evento de éxito. */
    private fun launchAuthentication(attempt: suspend () -> AppResult<User>) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = attempt()) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.Authenticated)
                }
                is AppResult.Failure -> _state.update {
                    // Cerrar el selector de Google no es un error que mostrar.
                    it.copy(isLoading = false, error = result.error.takeUnless { error -> error is AppError.Cancelled })
                }
            }
        }
    }
}
