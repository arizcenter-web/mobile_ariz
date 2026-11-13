package com.tecsup.app_ariz.feature.auth.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Espejo de los DTOs de backend/src/modules/auth/dto.
 * La respuesta `{ user }` (AuthResponseDto) vive en :core:session porque la comparte /auth/me.
 */

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
    val rememberMe: Boolean,
)

@Serializable
data class GoogleLoginRequestDto(
    val idToken: String,
)

@Serializable
data class RegisterCustomerRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val acceptTerms: Boolean,
)

/** Los campos opcionales viajan solo si tienen valor (`explicitNulls = false` en el Json global). */
@Serializable
data class RegisterProfessionalRequestDto(
    val type: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val dni: String? = null,
    val businessName: String? = null,
    val ruc: String? = null,
    val email: String,
    val phone: String,
    val whatsapp: String,
    val city: String,
    val districts: String? = null,
    val address: String? = null,
    val website: String? = null,
    val socialLinks: String? = null,
    val password: String,
    val acceptTerms: Boolean,
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)

@Serializable
data class ResetPasswordRequestDto(
    val token: String,
    val password: String,
)

/** Respuesta de /auth/forgot-password y /auth/reset-password: `{ ok: true }`. */
@Serializable
data class OkResponseDto(
    val ok: Boolean,
)
