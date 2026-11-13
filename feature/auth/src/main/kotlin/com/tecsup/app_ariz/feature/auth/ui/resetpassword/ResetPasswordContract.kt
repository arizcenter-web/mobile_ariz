package com.tecsup.app_ariz.feature.auth.ui.resetpassword

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError

data class ResetPasswordUiState(
    val password: String = "",
    val confirmation: String = "",
    val passwordError: FieldError? = null,
    val confirmationError: FieldError? = null,
    val isLoading: Boolean = false,
    /** La contraseña ya se cambió: la pantalla ofrece volver a iniciar sesión. */
    val isDone: Boolean = false,
    val error: AppError? = null,
)

sealed interface ResetPasswordAction {
    data class PasswordChanged(val value: String) : ResetPasswordAction
    data class ConfirmationChanged(val value: String) : ResetPasswordAction
    data object Submit : ResetPasswordAction
}
