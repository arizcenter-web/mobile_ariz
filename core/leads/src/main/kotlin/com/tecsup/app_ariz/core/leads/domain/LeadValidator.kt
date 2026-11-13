package com.tecsup.app_ariz.core.leads.domain

/**
 * Reglas del formulario de consulta (`LeadForm` del web): nombre, correo y mensaje obligatorios;
 * teléfono y DNI opcionales; los términos aceptados. El backend sigue siendo la autoridad.
 */
object LeadValidator {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    /** Límites del DTO del backend (`create-lead.dto.ts`). */
    const val NAME_MAX = 120
    const val MESSAGE_MAX = 1000
    const val PHONE_MAX = 40
    const val DNI_MAX = 12

    fun validateName(value: String): LeadFieldError? = if (value.isBlank()) LeadFieldError.REQUIRED else null

    fun validateEmail(value: String): LeadFieldError? = when {
        value.isBlank() -> LeadFieldError.REQUIRED
        !emailRegex.matches(value.trim()) -> LeadFieldError.INVALID_EMAIL
        else -> null
    }

    fun validateMessage(value: String): LeadFieldError? = if (value.isBlank()) LeadFieldError.REQUIRED else null

    fun validateTerms(accepted: Boolean): LeadFieldError? = if (accepted) null else LeadFieldError.TERMS_NOT_ACCEPTED

    /** El backend aún no tiene campo DNI: igual que el web, se anexa al mensaje para no perderlo. */
    fun messageWithDni(message: String, dni: String): String {
        val base = message.trim()
        val id = dni.trim()
        return if (id.isEmpty()) base else "$base\n\nDNI: $id"
    }
}
