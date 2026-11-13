package com.tecsup.app_ariz.feature.auth.ui.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tecsup.app_ariz.feature.auth.R
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError

@StringRes
private fun FieldError.messageRes(): Int = when (this) {
    FieldError.REQUIRED -> R.string.auth_error_required
    FieldError.INVALID_EMAIL -> R.string.auth_error_invalid_email
    FieldError.INVALID_PHONE -> R.string.auth_error_invalid_phone
    FieldError.INVALID_DNI -> R.string.auth_error_invalid_dni
    FieldError.INVALID_RUC -> R.string.auth_error_invalid_ruc
    FieldError.WEAK_PASSWORD -> R.string.auth_error_weak_password
    FieldError.PASSWORDS_MISMATCH -> R.string.auth_error_passwords_mismatch
    FieldError.TERMS_NOT_ACCEPTED -> R.string.auth_error_terms
}

/** Texto localizado del error, o `null` si el campo es válido. */
@Composable
fun FieldError?.asMessage(): String? = this?.let { stringResource(it.messageRes()) }
