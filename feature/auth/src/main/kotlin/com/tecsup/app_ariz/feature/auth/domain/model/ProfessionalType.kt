package com.tecsup.app_ariz.feature.auth.domain.model

/** Tipos de profesional que se pueden registrar (el backend no admite INDIVIDUAL_OWNER aquí). */
enum class ProfessionalType {
    INDEPENDENT_AGENT,
    REAL_ESTATE_AGENCY,
    DEVELOPER,
    ;

    /** Persona natural (nombre, apellido y DNI) frente a empresa (razón social y RUC). */
    val isPerson: Boolean get() = this == INDEPENDENT_AGENT
}
