package com.tecsup.app_ariz.feature.search.ui

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySortOrder
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter

data class SearchUiState(
    /** Filtros ya aplicados: son los que produjeron [items]. */
    val filters: PropertyFilters = PropertyFilters(),
    /** Textos que el usuario está escribiendo; se aplican con "Aplicar" o la tecla de búsqueda. */
    val districtText: String = "",
    val priceMinText: String = "",
    val priceMaxText: String = "",
    val areaMinText: String = "",
    val showMoreFilters: Boolean = false,

    val items: List<PropertySummary> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val hasMore: Boolean = false,
    /** Primera carga o cambio de filtros: se muestra el indicador a pantalla. */
    val isLoading: Boolean = true,
    /** Siguiente página: se muestra el indicador al final de la lista. */
    val isLoadingMore: Boolean = false,
    val error: AppError? = null,
) {
    /** Hay algún filtro distinto del inicial: se ofrece "Limpiar filtros" en el estado vacío. */
    val hasActiveFilters: Boolean
        get() = filters != PropertyFilters() ||
            districtText.isNotBlank() || priceMinText.isNotEmpty() || priceMaxText.isNotEmpty() || areaMinText.isNotEmpty()
}

sealed interface SearchAction {
    data class DistrictChanged(val value: String) : SearchAction
    data object DistrictSubmitted : SearchAction
    data class OperationChanged(val value: ListingOperation) : SearchAction
    data class TypeChanged(val value: PropertyTypeFilter?) : SearchAction
    data class RoomsChanged(val value: Int?) : SearchAction
    data class SortChanged(val value: PropertySortOrder) : SearchAction
    data class PriceMinChanged(val value: String) : SearchAction
    data class PriceMaxChanged(val value: String) : SearchAction
    data class AreaMinChanged(val value: String) : SearchAction
    data object ToggleMoreFilters : SearchAction
    data object ClearFilters : SearchAction
    data object ApplyMoreFilters : SearchAction
    data object LoadMore : SearchAction
    data object Retry : SearchAction
}
