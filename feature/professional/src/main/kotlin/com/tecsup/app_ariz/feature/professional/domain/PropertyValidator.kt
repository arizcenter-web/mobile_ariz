package com.tecsup.app_ariz.feature.professional.domain

enum class PropertyField {
    TITLE, DESCRIPTION, PRICE, MAINTENANCE_FEE, DISTRICT, ADDRESS, TOTAL_AREA, CONSTRUCTED_AREA, PHOTOS, CONTACT_EMAIL,
}

enum class PropertyFieldError { REQUIRED, INVALID_NUMBER, TOO_LONG, INVALID_EMAIL, NEEDS_PHOTO }

/**
 * Reglas del `CreatePropertyDto` del backend: título ≤ 100, descripción ≤ 5000, precio ≥ 1, áreas ≥ 1,
 * distrito y dirección obligatorios y al menos una foto. El backend sigue siendo la autoridad.
 */
object PropertyValidator {
    const val TITLE_MAX = 100
    const val DESCRIPTION_MAX = 5000

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun validate(draft: PropertyDraft): Map<PropertyField, PropertyFieldError> = buildMap {
        when {
            draft.title.isBlank() -> put(PropertyField.TITLE, PropertyFieldError.REQUIRED)
            draft.title.trim().length > TITLE_MAX -> put(PropertyField.TITLE, PropertyFieldError.TOO_LONG)
        }
        when {
            draft.description.isBlank() -> put(PropertyField.DESCRIPTION, PropertyFieldError.REQUIRED)
            draft.description.trim().length > DESCRIPTION_MAX -> put(PropertyField.DESCRIPTION, PropertyFieldError.TOO_LONG)
        }
        val price = draft.price.toDoubleOrNull()
        when {
            draft.price.isBlank() -> put(PropertyField.PRICE, PropertyFieldError.REQUIRED)
            price == null || price < 1 -> put(PropertyField.PRICE, PropertyFieldError.INVALID_NUMBER)
        }
        if (draft.maintenanceFee.isNotBlank() && (draft.maintenanceFee.toDoubleOrNull() ?: -1.0) < 0) {
            put(PropertyField.MAINTENANCE_FEE, PropertyFieldError.INVALID_NUMBER)
        }
        if (draft.district.isBlank()) put(PropertyField.DISTRICT, PropertyFieldError.REQUIRED)
        if (draft.address.isBlank()) put(PropertyField.ADDRESS, PropertyFieldError.REQUIRED)
        if (draft.totalArea.isNotBlank() && (draft.totalArea.toDoubleOrNull() ?: 0.0) < 1) {
            put(PropertyField.TOTAL_AREA, PropertyFieldError.INVALID_NUMBER)
        }
        if (draft.constructedArea.isNotBlank() && (draft.constructedArea.toDoubleOrNull() ?: 0.0) < 1) {
            put(PropertyField.CONSTRUCTED_AREA, PropertyFieldError.INVALID_NUMBER)
        }
        if (draft.contactEmail.isNotBlank() && !emailRegex.matches(draft.contactEmail.trim())) {
            put(PropertyField.CONTACT_EMAIL, PropertyFieldError.INVALID_EMAIL)
        }
        if (draft.photos.isEmpty()) put(PropertyField.PHOTOS, PropertyFieldError.NEEDS_PHOTO)
    }
}
