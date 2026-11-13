package com.tecsup.app_ariz.feature.auth.domain.validation

/** Motivo por el que un campo no es válido. La UI decide el texto (strings.xml). */
enum class FieldError {
    REQUIRED,
    INVALID_EMAIL,
    INVALID_PHONE,
    INVALID_DNI,
    INVALID_RUC,
    WEAK_PASSWORD,
    PASSWORDS_MISMATCH,
    TERMS_NOT_ACCEPTED,
}

/**
 * Reglas de validación en el cliente. Replican las del backend (`register-customer.dto.ts`,
 * `register-professional.dto.ts`) para dar feedback inmediato; el backend sigue siendo la
 * autoridad y su mensaje se muestra si rechaza.
 */
object AuthValidators {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    // Entre 8 y 72 caracteres, con al menos una letra y un número (PASSWORD_RULE del backend).
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")
    private val dniRegex = Regex("^\\d{8}$")
    private val rucRegex = Regex("^\\d{11}$")

    private const val PHONE_MIN_LENGTH = 7
    private const val PHONE_MAX_LENGTH = 20

    fun validateRequired(value: String): FieldError? =
        if (value.isBlank()) FieldError.REQUIRED else null

    fun validateEmail(value: String): FieldError? = when {
        value.isBlank() -> FieldError.REQUIRED
        !emailRegex.matches(value.trim()) -> FieldError.INVALID_EMAIL
        else -> null
    }

    fun validatePhone(value: String): FieldError? = when {
        value.isBlank() -> FieldError.REQUIRED
        value.trim().length !in PHONE_MIN_LENGTH..PHONE_MAX_LENGTH -> FieldError.INVALID_PHONE
        else -> null
    }

    fun validateDni(value: String): FieldError? = when {
        value.isBlank() -> FieldError.REQUIRED
        !dniRegex.matches(value.trim()) -> FieldError.INVALID_DNI
        else -> null
    }

    fun validateRuc(value: String): FieldError? = when {
        value.isBlank() -> FieldError.REQUIRED
        !rucRegex.matches(value.trim()) -> FieldError.INVALID_RUC
        else -> null
    }

    fun validateNewPassword(value: String): FieldError? = when {
        value.isEmpty() -> FieldError.REQUIRED
        !passwordRegex.matches(value) -> FieldError.WEAK_PASSWORD
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): FieldError? =
        if (password == confirmation) null else FieldError.PASSWORDS_MISMATCH

    fun validateTerms(accepted: Boolean): FieldError? =
        if (accepted) null else FieldError.TERMS_NOT_ACCEPTED
}
