package com.tecsup.app_ariz.core.model.property

/** Tipos de propiedad que el backend sabe filtrar (`propertyType`). */
enum class PropertyTypeFilter { HOUSE, APARTMENT, LAND, COMMERCIAL }

enum class PropertySortOrder { RECENT, PRICE_ASC, PRICE_DESC }

/**
 * Filtros de la búsqueda de propiedades (`PropertyFiltersBar` del web). Los `null` significan
 * "sin filtro". Los precios se expresan en soles, como en el web.
 */
data class PropertyFilters(
    val operation: ListingOperation = ListingOperation.SALE,
    val type: PropertyTypeFilter? = null,
    val district: String? = null,
    val priceMin: Int? = null,
    val priceMax: Int? = null,
    val roomsMin: Int? = null,
    val areaMin: Int? = null,
    val sort: PropertySortOrder = PropertySortOrder.RECENT,
)
