package com.tecsup.app_ariz.feature.auth.domain.model

/**
 * Datos del registro de un profesional. Según [type] se usan `firstName/lastName/dni` (persona)
 * o `businessName/ruc` (empresa); los campos que no aplican quedan en `null`.
 */
data class RegisterProfessionalData(
    val type: ProfessionalType,
    val firstName: String?,
    val lastName: String?,
    val dni: String?,
    val businessName: String?,
    val ruc: String?,
    val email: String,
    val phone: String,
    val whatsapp: String,
    val city: String,
    val districts: String?,
    val address: String?,
    val website: String?,
    val socialLinks: String?,
    val password: String,
)
