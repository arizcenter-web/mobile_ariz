package com.tecsup.app_ariz.feature.auth.ui.login

import android.content.Context
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = true,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val isLoading: Boolean = false,
    /** `false` si la app no tiene configurado Firebase/Google: la UI oculta el botón. */
    val isGoogleAvailable: Boolean = false,
    /** Error devuelto por el backend (credenciales inválidas, 429, red...). */
    val error: AppError? = null,
)

sealed interface LoginAction {
    data class EmailChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data class RememberMeChanged(val value: Boolean) : LoginAction
    data object Submit : LoginAction

    /** El selector de cuentas de Google necesita una Activity; el contexto no se almacena. */
    data class GoogleSignIn(val activityContext: Context) : LoginAction
}

/** Eventos de una sola vez (no forman parte del estado). */
sealed interface LoginEvent {
    data object Authenticated : LoginEvent
}
