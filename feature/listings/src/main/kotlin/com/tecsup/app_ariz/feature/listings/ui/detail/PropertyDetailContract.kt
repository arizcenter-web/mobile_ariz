package com.tecsup.app_ariz.feature.listings.ui.detail

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail

/** Estado del botón "Avísame si baja de precio". */
enum class PriceAlertStatus { IDLE, CREATING, CREATED, FAILED }

data class PropertyDetailUiState(
    val isLoading: Boolean = true,
    val detail: PropertyDetail? = null,
    val similar: List<PropertySummary> = emptyList(),
    val error: AppError? = null,
    val priceAlert: PriceAlertStatus = PriceAlertStatus.IDLE,
)

sealed interface PropertyDetailAction {
    data object Retry : PropertyDetailAction
    data object CreatePriceAlert : PropertyDetailAction
}

/** Eventos de una sola vez hacia la pantalla. */
sealed interface PropertyDetailEvent {
    /** El aviso de precio necesita una cuenta: llevar al usuario a iniciar sesión. */
    data object LoginRequired : PropertyDetailEvent
}
