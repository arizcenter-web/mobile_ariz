package com.tecsup.app_ariz.feature.info.domain

import com.tecsup.app_ariz.core.common.AppResult

/** Consulta general a ARIZ (`POST /contact-reports`, público). */
data class ContactReport(
    val name: String,
    val email: String,
    val phone: String,
    val topic: String,
    val message: String,
)

enum class ContactFieldError { REQUIRED, INVALID_EMAIL, INVALID_PHONE, MESSAGE_TOO_SHORT }

/**
 * Reglas del `CreateContactReportDto` del backend: nombre y correo obligatorios, celular peruano
 * de 9 dígitos que empieza en 9 (opcional) y mensaje de al menos 5 caracteres.
 */
object ContactValidator {
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val phoneRegex = Regex("^9\\d{8}$")

    const val MESSAGE_MIN = 5
    const val MESSAGE_MAX = 2000
    const val NAME_MAX = 120

    fun validateName(value: String) = if (value.isBlank()) ContactFieldError.REQUIRED else null

    fun validateEmail(value: String) = when {
        value.isBlank() -> ContactFieldError.REQUIRED
        !emailRegex.matches(value.trim()) -> ContactFieldError.INVALID_EMAIL
        else -> null
    }

    /** El teléfono es opcional; si se escribe debe ser un celular válido. */
    fun validatePhone(value: String) = when {
        value.isBlank() -> null
        !phoneRegex.matches(value.trim()) -> ContactFieldError.INVALID_PHONE
        else -> null
    }

    fun validateMessage(value: String) = when {
        value.isBlank() -> ContactFieldError.REQUIRED
        value.trim().length < MESSAGE_MIN -> ContactFieldError.MESSAGE_TOO_SHORT
        else -> null
    }
}

interface ContactRepository {
    suspend fun send(report: ContactReport): AppResult<Unit>
}
