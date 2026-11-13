package com.tecsup.app_ariz.core.leads.domain

import com.tecsup.app_ariz.core.common.AppResult

/** A qué se refiere la consulta: el backend exige exactamente una propiedad o un proyecto. */
sealed interface LeadTarget {
    val id: String

    data class Property(override val id: String) : LeadTarget

    data class Project(override val id: String) : LeadTarget
}

/** Consulta que el usuario envía al anunciante (`POST /leads`, público). */
data class LeadRequest(
    val target: LeadTarget,
    val name: String,
    val email: String,
    val phone: String,
    val dni: String,
    val message: String,
)

/** Motivo por el que un campo del formulario no es válido; la UI decide el texto. */
enum class LeadFieldError { REQUIRED, INVALID_EMAIL, TERMS_NOT_ACCEPTED }

interface LeadsRepository {
    suspend fun send(request: LeadRequest): AppResult<Unit>
}
