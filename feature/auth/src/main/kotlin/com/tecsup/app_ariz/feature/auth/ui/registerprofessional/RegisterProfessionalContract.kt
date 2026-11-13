package com.tecsup.app_ariz.feature.auth.ui.registerprofessional

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.auth.domain.model.ProfessionalType
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError

enum class ProfessionalField(val isRequired: Boolean = true) {
    FIRST_NAME,
    LAST_NAME,
    DNI,
    BUSINESS_NAME,
    RUC,
    EMAIL,
    PHONE,
    WHATSAPP,
    CITY,
    DISTRICTS(isRequired = false),
    ADDRESS(isRequired = false),
    WEBSITE(isRequired = false),
    SOCIAL_LINKS(isRequired = false),
    PASSWORD,
}

/** Campos que se muestran (y validan) según el tipo: persona natural o empresa. */
fun fieldsFor(type: ProfessionalType): List<ProfessionalField> {
    val identity = if (type.isPerson) {
        listOf(ProfessionalField.FIRST_NAME, ProfessionalField.LAST_NAME, ProfessionalField.DNI)
    } else {
        listOf(ProfessionalField.BUSINESS_NAME, ProfessionalField.RUC)
    }
    return identity + listOf(
        ProfessionalField.EMAIL,
        ProfessionalField.PHONE,
        ProfessionalField.WHATSAPP,
        ProfessionalField.CITY,
        ProfessionalField.DISTRICTS,
        ProfessionalField.ADDRESS,
        ProfessionalField.WEBSITE,
        ProfessionalField.SOCIAL_LINKS,
        ProfessionalField.PASSWORD,
    )
}

data class RegisterProfessionalUiState(
    val type: ProfessionalType = ProfessionalType.INDEPENDENT_AGENT,
    val values: Map<ProfessionalField, String> = emptyMap(),
    val acceptTerms: Boolean = false,
    val fieldErrors: Map<ProfessionalField, FieldError> = emptyMap(),
    val termsError: FieldError? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
) {
    fun valueOf(field: ProfessionalField): String = values[field].orEmpty()
    val visibleFields: List<ProfessionalField> get() = fieldsFor(type)
}

sealed interface RegisterProfessionalAction {
    data class TypeChanged(val type: ProfessionalType) : RegisterProfessionalAction
    data class FieldChanged(val field: ProfessionalField, val value: String) : RegisterProfessionalAction
    data class TermsChanged(val accepted: Boolean) : RegisterProfessionalAction
    data object Submit : RegisterProfessionalAction
}

sealed interface RegisterProfessionalEvent {
    data object Registered : RegisterProfessionalEvent
}
