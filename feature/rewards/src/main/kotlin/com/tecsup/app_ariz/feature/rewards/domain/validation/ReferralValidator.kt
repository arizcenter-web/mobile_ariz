package com.tecsup.app_ariz.feature.rewards.domain.validation

enum class ReferralFieldError { REQUIRED, INVALID_EMAIL, INVALID_PHONE, INTEREST_REQUIRED }

/**
 * Reglas del formulario de referidos, las mismas del `ReferralModal` del web: correo real y celular
 * peruano (9 dígitos que empiezan en 9; el prefijo de país 51 se agrega al enviar).
 */
object ReferralValidator {

    // usuario@dominio.tld con TLD alfabético de 2+ letras y sin puntos dobles.
    private val emailRegex = Regex(
        "^[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@" +
            "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$",
    )
    private val peruMobileRegex = Regex("^9\\d{8}$")

    fun validateRequired(value: String): ReferralFieldError? =
        if (value.isBlank()) ReferralFieldError.REQUIRED else null

    fun validateEmail(value: String): ReferralFieldError? = when {
        value.isBlank() -> ReferralFieldError.REQUIRED
        !emailRegex.matches(value.trim()) -> ReferralFieldError.INVALID_EMAIL
        else -> null
    }

    fun validatePhone(value: String): ReferralFieldError? = when {
        value.isBlank() -> ReferralFieldError.REQUIRED
        !peruMobileRegex.matches(value.filter { it.isDigit() }) -> ReferralFieldError.INVALID_PHONE
        else -> null
    }

    /** "923 655 566" -> "51923655566" (formato internacional, como `buildPeruPhone` del web). */
    fun toInternationalPhone(value: String): String = "51" + value.filter { it.isDigit() }
}
