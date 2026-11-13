package com.tecsup.app_ariz.feature.agents.data.mapper

import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.feature.agents.data.remote.dto.AgentProfileDto
import com.tecsup.app_ariz.feature.agents.data.remote.dto.AgentPropertyDto
import com.tecsup.app_ariz.feature.agents.domain.model.AgentProfile

internal fun AgentProfileDto.toDomain() = AgentProfile(
    id = id,
    name = name,
    isVerified = status == "APPROVED",
    whatsapp = whatsapp?.takeIf { it.isNotBlank() },
    phone = phone?.takeIf { it.isNotBlank() },
    email = email?.takeIf { it.isNotBlank() },
    city = city,
    districts = districts?.takeIf { it.isNotBlank() },
    website = website,
    properties = properties.map { it.toDomain() },
)

private fun AgentPropertyDto.toDomain() = PropertySummary(
    id = id,
    slug = slug,
    title = title,
    description = description,
    operation = if (operation == "rent") ListingOperation.RENT else ListingOperation.SALE,
    price = price.amount,
    currency = runCatching { Currency.valueOf(price.currency) }.getOrDefault(Currency.PEN),
    district = location.district,
    department = location.department,
    coverImageUrl = images.firstOrNull(),
    photoCount = images.size,
    areaM2 = areaM2,
    bedrooms = rooms,
    bathrooms = bathrooms,
    parkingSpaces = parkingSpaces,
    publishedAtMillis = publishedAt.isoToEpochMillis(),
)
