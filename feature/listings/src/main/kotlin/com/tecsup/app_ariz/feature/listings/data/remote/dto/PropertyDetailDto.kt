package com.tecsup.app_ariz.feature.listings.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

/**
 * Respuesta de `GET /properties/:slug`. Las áreas llegan como `Decimal` de Prisma (texto, p. ej.
 * `"120.00"`) o como número según la ruta, por eso se leen como [JsonPrimitive].
 */
@Serializable
internal data class PropertyDetailDto(
    val id: String,
    val slug: String,
    val title: String,
    val description: String = "",
    val operation: String = "buy",
    val type: String = "",
    val price: PriceDto = PriceDto(),
    val maintenanceFee: JsonPrimitive? = null,
    val location: LocationDto = LocationDto(),
    val images: List<String> = emptyList(),
    val publishedAt: String? = null,
    val rooms: Int = 0,
    val bathrooms: Int = 0,
    val parkingSpaces: Int = 0,
    val details: DetailsDto? = null,
    val amenities: List<AmenityDto> = emptyList(),
    val agent: AgentDto,
)

@Serializable
internal data class PriceDto(val amount: Double = 0.0, val currency: String = "PEN")

@Serializable
internal data class LocationDto(
    val department: String = "",
    val province: String = "",
    val district: String = "",
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
internal data class DetailsDto(
    val totalArea: JsonPrimitive? = null,
    val constructedArea: JsonPrimitive? = null,
    val halfBathrooms: Int? = null,
    val constructionYear: Int? = null,
    val conservationStatus: String? = null,
)

@Serializable
internal data class AmenityDto(val id: String = "", val name: String = "")

@Serializable
internal data class AgentDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val status: String = "",
    val whatsapp: String? = null,
    val phone: String? = null,
)
