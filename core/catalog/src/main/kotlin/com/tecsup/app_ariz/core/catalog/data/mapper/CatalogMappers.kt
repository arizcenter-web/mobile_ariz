package com.tecsup.app_ariz.core.catalog.data.mapper

import com.tecsup.app_ariz.core.catalog.data.remote.PagedDto
import com.tecsup.app_ariz.core.catalog.data.remote.ProjectDto
import com.tecsup.app_ariz.core.catalog.data.remote.PropertyDto
import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertySummary

internal fun PropertyDto.toDomain() = PropertySummary(
    id = id,
    slug = slug,
    title = title,
    description = description,
    operation = when (operation) {
        "buy" -> ListingOperation.SALE
        "rent" -> ListingOperation.RENT
        else -> ListingOperation.OTHER
    },
    price = price.amount,
    currency = price.currency.toCurrency(),
    district = location.district,
    department = location.department,
    coverImageUrl = coverImage ?: images.firstOrNull(),
    photoCount = images.size,
    areaM2 = areaM2,
    bedrooms = rooms,
    bathrooms = bathrooms,
    parkingSpaces = parkingSpaces,
    publishedAtMillis = publishedAt.isoToEpochMillis(),
)

internal fun ProjectDto.toDomain() = ProjectSummary(
    id = id,
    slug = slug,
    name = name,
    developer = developer,
    priceFrom = priceFrom,
    currency = currency.toCurrency(),
    coverImageUrl = coverImage ?: images.firstOrNull(),
    stage = runCatching { ProjectStage.valueOf(stage) }.getOrDefault(ProjectStage.PRE_SALE),
    category = runCatching { ProjectCategory.valueOf(category) }.getOrDefault(ProjectCategory.LOTIZACION),
    minAreaM2 = areaRange.min,
    totalUnits = totalLots ?: lotsCount,
    district = location.district,
    publishedAtMillis = publishedAt.isoToEpochMillis(),
    featured = featured,
    cheapestPrice = cheapestLotPrice,
)

internal fun <D, T> PagedDto<D>.toDomain(transform: (D) -> T) = Page(
    items = items.map(transform),
    total = total,
    page = page,
    hasMore = hasMore,
)

private fun String.toCurrency(): Currency = runCatching { Currency.valueOf(this) }.getOrDefault(Currency.PEN)
