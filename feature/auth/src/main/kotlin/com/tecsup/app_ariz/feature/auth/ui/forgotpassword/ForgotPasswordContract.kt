package com.tecsup.app_ariz.feature.auth.ui.forgotpassword

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: FieldError? = null,
    val isLoading: Boolean = false,
    /** El backend responde `ok` aunque el correo no exista (no revela qué cuentas hay). */
    val isSent: Boolean = false,
    val error: AppError? = null,
)

sealed interface ForgotPasswordAction {
    data class EmailChanged(val value: String) : ForgotPasswordAction
    data object Submit : ForgotPasswordAction
}
