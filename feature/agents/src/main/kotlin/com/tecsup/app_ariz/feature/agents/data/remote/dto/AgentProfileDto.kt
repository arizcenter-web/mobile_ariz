package com.tecsup.app_ariz.feature.agents.data.remote.dto

import kotlinx.serialization.Serializable

/** Respuesta de `GET /agents/:slugOrId`. Solo los campos que la app muestra. */
@Serializable
internal data class AgentProfileDto(
    val id: String,
    val name: String = "",
    val status: String = "",
    val whatsapp: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val city: String = "",
    val districts: String? = null,
    val website: String? = null,
    val properties: List<AgentPropertyDto> = emptyList(),
)

@Serializable
internal data class AgentPropertyDto(
    val id: String,
    val slug: String,
    val title: String,
    val description: String = "",
    val operation: String = "buy",
    val price: PriceDto = PriceDto(),
    val location: LocationDto = LocationDto(),
    val images: List<String> = emptyList(),
    val rooms: Int = 0,
    val bathrooms: Int = 0,
    val parkingSpaces: Int = 0,
    val areaM2: Double = 0.0,
    val publishedAt: String? = null,
)

@Serializable
internal data class PriceDto(val amount: Double = 0.0, val currency: String = "PEN")

@Serializable
internal data class LocationDto(val department: String = "", val district: String = "")
