package com.tecsup.app_ariz.feature.listings.domain.model

import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter

enum class ConservationStatus { NEW, EXCELLENT, GOOD, REMODELED, TO_REMODEL, UNDER_CONSTRUCTION }

enum class AgentType { REAL_ESTATE, AGENT, INDEPENDENT, INDIVIDUAL_OWNER, DEVELOPER, ARCHITECT, OTHER }

data class PropertyLocation(
    /** `null` cuando el anunciante eligió mostrar solo una ubicación aproximada. */
    val address: String?,
    val district: String,
    val province: String,
    val department: String,
    val latitude: Double?,
    val longitude: Double?,
) {
    val hasCoordinates: Boolean get() = latitude != null && longitude != null

    /** "Dirección, Distrito, Provincia, Departamento" sin partes vacías (`locationLine` del web). */
    val line: String get() = listOfNotNull(address, district, province, department).filter { it.isNotBlank() }.joinToString(", ")
}

data class Advertiser(
    val id: String,
    val name: String,
    val type: AgentType,
    val isVerified: Boolean,
    val whatsapp: String?,
    val phone: String?,
)

/** Detalle público de una propiedad (`GET /properties/:slug`). */
data class PropertyDetail(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val operation: ListingOperation,
    val type: PropertyTypeFilter?,
    val price: Double,
    val currency: Currency,
    val maintenanceFee: Double?,
    val location: PropertyLocation,
    val images: List<String>,
    val publishedAtMillis: Long?,
    val totalArea: Double?,
    val constructedArea: Double?,
    val bedrooms: Int,
    val bathrooms: Int,
    val halfBathrooms: Int,
    val parkingSpaces: Int,
    val constructionYear: Int?,
    val conservation: ConservationStatus?,
    val amenities: List<String>,
    val advertiser: Advertiser,
) {
    val isRent: Boolean get() = operation == ListingOperation.RENT
}
