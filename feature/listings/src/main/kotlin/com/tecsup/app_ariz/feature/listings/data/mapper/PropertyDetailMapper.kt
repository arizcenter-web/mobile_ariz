package com.tecsup.app_ariz.feature.listings.data.mapper

import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter
import com.tecsup.app_ariz.feature.listings.data.remote.dto.PropertyDetailDto
import com.tecsup.app_ariz.feature.listings.domain.model.Advertiser
import com.tecsup.app_ariz.feature.listings.domain.model.AgentType
import com.tecsup.app_ariz.feature.listings.domain.model.ConservationStatus
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyLocation
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal fun PropertyDetailDto.toDomain() = PropertyDetail(
    id = id,
    slug = slug,
    title = title,
    description = description,
    operation = when (operation) {
        "buy" -> ListingOperation.SALE
        "rent" -> ListingOperation.RENT
        else -> ListingOperation.OTHER
    },
    type = when (type) {
        "houses" -> PropertyTypeFilter.HOUSE
        "apartments" -> PropertyTypeFilter.APARTMENT
        "land" -> PropertyTypeFilter.LAND
        "commercial" -> PropertyTypeFilter.COMMERCIAL
        else -> null
    },
    price = price.amount,
    currency = runCatching { Currency.valueOf(price.currency) }.getOrDefault(Currency.PEN),
    maintenanceFee = maintenanceFee.asDouble()?.takeIf { it > 0 },
    location = PropertyLocation(
        address = location.address?.takeIf { it.isNotBlank() },
        district = location.district,
        province = location.province,
        department = location.department,
        latitude = location.lat,
        longitude = location.lng,
    ),
    images = images,
    publishedAtMillis = publishedAt.isoToEpochMillis(),
    totalArea = details?.totalArea.asDouble()?.takeIf { it > 0 },
    constructedArea = details?.constructedArea.asDouble()?.takeIf { it > 0 },
    bedrooms = rooms,
    bathrooms = bathrooms,
    halfBathrooms = details?.halfBathrooms ?: 0,
    parkingSpaces = parkingSpaces,
    constructionYear = details?.constructionYear,
    conservation = details?.conservationStatus?.let { status ->
        runCatching { ConservationStatus.valueOf(status) }.getOrNull()
    },
    amenities = amenities.map { it.name }.filter { it.isNotBlank() },
    advertiser = Advertiser(
        id = agent.id,
        name = agent.name,
        type = when (agent.type.uppercase()) {
            "REAL_ESTATE", "REAL_ESTATE_AGENCY" -> AgentType.REAL_ESTATE
            "AGENT" -> AgentType.AGENT
            "INDEPENDENT", "INDEPENDENT_AGENT" -> AgentType.INDEPENDENT
            "INDIVIDUAL_OWNER" -> AgentType.INDIVIDUAL_OWNER
            "DEVELOPER" -> AgentType.DEVELOPER
            "ARCHITECT" -> AgentType.ARCHITECT
            else -> AgentType.OTHER
        },
        isVerified = agent.status == "APPROVED",
        whatsapp = agent.whatsapp?.takeIf { it.isNotBlank() },
        phone = agent.phone?.takeIf { it.isNotBlank() },
    ),
)

/** Acepta número o texto numérico (`"120.00"`); `null` si no hay valor interpretable. */
internal fun JsonPrimitive?.asDouble(): Double? = this?.contentOrNull?.toDoubleOrNull()
