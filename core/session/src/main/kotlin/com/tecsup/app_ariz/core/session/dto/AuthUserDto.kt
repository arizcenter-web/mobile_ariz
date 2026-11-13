package com.tecsup.app_ariz.core.session.dto

import kotlinx.serialization.Serializable

/** `AuthUser` del backend (common/types/auth-user.ts). Lo comparten /auth/login, /auth/me, etc. */
@Serializable
data class AuthUserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val professionalStatus: String? = null,
)

/** Respuesta de los endpoints de sesión: `{ user }`. */
@Serializable
data class AuthResponseDto(
    val user: AuthUserDto,
)
