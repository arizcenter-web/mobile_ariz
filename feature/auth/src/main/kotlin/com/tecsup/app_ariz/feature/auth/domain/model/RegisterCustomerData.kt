package com.tecsup.app_ariz.feature.auth.domain.model

/** Datos necesarios para registrar a un cliente. Ya validados por la capa de UI. */
data class RegisterCustomerData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
)
