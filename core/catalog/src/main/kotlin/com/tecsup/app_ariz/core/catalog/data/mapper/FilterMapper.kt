package com.tecsup.app_ariz.core.catalog.data.mapper

import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySortOrder
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter

/** Parámetros de `GET /properties` ya en el vocabulario del backend (`buildQuery` del web). */
internal data class PropertyQuery(
    val operationType: String?,
    val propertyType: String?,
    val district: String?,
    val priceMin: Int?,
    val priceMax: Int?,
    val bedroomsMin: Int?,
    val areaMin: Int?,
    val sort: String,
)

internal fun PropertyFilters.toQuery() = PropertyQuery(
    operationType = if (operation == ListingOperation.RENT) "RENT" else "SALE",
    propertyType = type?.let {
        when (it) {
            PropertyTypeFilter.HOUSE -> "HOUSE"
            PropertyTypeFilter.APARTMENT -> "APARTMENT"
            PropertyTypeFilter.LAND -> "LAND"
            PropertyTypeFilter.COMMERCIAL -> "COMMERCIAL"
        }
    },
    district = district?.trim()?.takeIf { it.isNotEmpty() },
    // El backend exige min >= 1: un 0 se interpreta como "sin filtro".
    priceMin = priceMin?.takeIf { it > 0 },
    priceMax = priceMax?.takeIf { it > 0 },
    bedroomsMin = roomsMin?.takeIf { it > 0 },
    areaMin = areaMin?.takeIf { it > 0 },
    sort = when (sort) {
        PropertySortOrder.RECENT -> "recent"
        PropertySortOrder.PRICE_ASC -> "price-asc"
        PropertySortOrder.PRICE_DESC -> "price-desc"
    },
)
