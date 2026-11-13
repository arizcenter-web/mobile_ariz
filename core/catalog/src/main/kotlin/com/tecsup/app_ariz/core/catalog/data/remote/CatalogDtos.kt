package com.tecsup.app_ariz.core.catalog.data.remote

import kotlinx.serialization.Serializable

/** DTOs de `GET /properties` y `GET /projects`. Solo llevan lo que la app usa; el resto se ignora. */
@Serializable
internal data class PagedDto<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val hasMore: Boolean = false,
)

@Serializable
internal data class PriceDto(val amount: Double = 0.0, val currency: String = "PEN")

@Serializable
internal data class LocationDto(
    val department: String = "",
    val district: String = "",
)

@Serializable
internal data class PropertyDto(
    val id: String,
    val slug: String,
    val title: String,
    val description: String = "",
    val operation: String = "buy",
    val price: PriceDto = PriceDto(),
    val location: LocationDto = LocationDto(),
    val images: List<String> = emptyList(),
    val coverImage: String? = null,
    val rooms: Int = 0,
    val bathrooms: Int = 0,
    val parkingSpaces: Int = 0,
    val areaM2: Double = 0.0,
    val publishedAt: String? = null,
)

@Serializable
internal data class AreaRangeDto(val min: Double = 0.0)

@Serializable
internal data class ProjectDto(
    val id: String,
    val slug: String,
    val name: String,
    val developer: String = "",
    val category: String = "LOTIZACION",
    val stage: String = "PRE_SALE",
    val currency: String = "PEN",
    val priceFrom: Double = 0.0,
    val cheapestLotPrice: Double? = null,
    val featured: Boolean = false,
    val totalLots: Int? = null,
    val lotsCount: Int = 0,
    val areaRange: AreaRangeDto = AreaRangeDto(),
    val location: LocationDto = LocationDto(),
    val coverImage: String? = null,
    val images: List<String> = emptyList(),
    val publishedAt: String? = null,
)
