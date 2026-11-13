package com.tecsup.app_ariz.core.model.property

import com.tecsup.app_ariz.core.model.common.Currency

/** Operación de una propiedad publicada (`operation` del backend: buy / rent / invest). */
enum class ListingOperation { SALE, RENT, OTHER }

/** Resumen de una propiedad para tarjetas de listado. Es lo que devuelve `GET /properties`. */
data class PropertySummary(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val operation: ListingOperation,
    val price: Double,
    val currency: Currency,
    val district: String,
    val department: String,
    val coverImageUrl: String?,
    val photoCount: Int,
    val areaM2: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val parkingSpaces: Int,
    /** Instante de publicación en milisegundos desde epoch; `null` si el backend no lo envía. */
    val publishedAtMillis: Long?,
)
