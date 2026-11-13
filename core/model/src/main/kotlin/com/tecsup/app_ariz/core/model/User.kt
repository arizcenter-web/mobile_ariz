package com.tecsup.app_ariz.core.model

/** Usuario autenticado. Refleja `AuthUser` del backend (respuesta de /auth/login y /auth/me). */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    /** Estado de aprobación del perfil profesional; `null` si el usuario no es profesional. */
    val professionalStatus: ApprovalStatus?,
)

/** Enum `Role` de prisma/schema.prisma. */
enum class Role { ADMIN, USER, REAL_ESTATE }

/** Enum `ApprovalStatus` de prisma/schema.prisma. */
enum class ApprovalStatus { PENDING, APPROVED, REJECTED }
