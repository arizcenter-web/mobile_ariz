package com.tecsup.app_ariz.feature.auth.ui.register

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError

enum class RegisterField { FIRST_NAME, LAST_NAME, EMAIL, PHONE, PASSWORD }

data class RegisterUiState(
    val values: Map<RegisterField, String> = emptyMap(),
    val acceptTerms: Boolean = false,
    val fieldErrors: Map<RegisterField, FieldError> = emptyMap(),
    val termsError: FieldError? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
) {
    fun valueOf(field: RegisterField): String = values[field].orEmpty()
}

sealed interface RegisterAction {
    data class FieldChanged(val field: RegisterField, val value: String) : RegisterAction
    data class TermsChanged(val accepted: Boolean) : RegisterAction
    data object Submit : RegisterAction
}

sealed interface RegisterEvent {
    data object Registered : RegisterEvent
}
