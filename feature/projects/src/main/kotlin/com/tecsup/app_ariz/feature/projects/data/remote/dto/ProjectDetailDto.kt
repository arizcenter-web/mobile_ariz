package com.tecsup.app_ariz.feature.projects.data.remote.dto

import kotlinx.serialization.Serializable

/** Respuesta de `GET /projects/:slug`. Solo los campos que la app muestra; el resto se ignora. */
@Serializable
internal data class ProjectDetailDto(
    val id: String,
    val slug: String,
    val name: String,
    val description: String = "",
    val category: String = "LOTIZACION",
    val stage: String = "PRE_SALE",
    val lotMode: String = "INDIVIDUAL",
    val currency: String = "PEN",
    val priceFrom: Double = 0.0,
    val priceTo: Double = 0.0,
    val developer: String = "",
    val featured: Boolean = false,
    val location: LocationDto? = null,
    val images: List<String> = emptyList(),
    val media: List<MediaDto> = emptyList(),
    val tourUrl: String? = null,
    val publishedAt: String? = null,
    val createdAt: String? = null,
    val constructionStart: String? = null,
    val estimatedDelivery: String? = null,
    val totalLots: Int? = null,
    val totalBlocks: Int? = null,
    val areaRange: AreaRangeDto = AreaRangeDto(),
    val downPayment: Double? = null,
    val monthlyPayment: Double? = null,
    val financing: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val certifications: List<String> = emptyList(),
    val urbanizationName: String? = null,
    val registryEntry: String? = null,
    val ruc: String? = null,
    val lots: List<LotDto> = emptyList(),
    val milestones: List<MilestoneDto> = emptyList(),
    val agent: AgentDto = AgentDto(),
    val hasPublishedSitePlan: Boolean = false,
)

@Serializable
internal data class LocationDto(
    val zone: String? = null,
    val street: String? = null,
    val district: String = "",
    val province: String = "",
    val department: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
internal data class AreaRangeDto(val min: Double = 0.0, val max: Double = 0.0)

@Serializable
internal data class MediaDto(val type: String = "", val url: String = "")

@Serializable
internal data class LotDto(
    val id: String = "",
    val lotNumber: String = "",
    val block: String? = null,
    val areaM2: Double = 0.0,
    val price: Double = 0.0,
    val status: String = "AVAILABLE",
)

@Serializable
internal data class MilestoneDto(val title: String = "", val date: String? = null, val note: String? = null)

@Serializable
internal data class AgentDto(
    val name: String = "",
    val type: String = "",
    val status: String = "",
    val whatsapp: String? = null,
    val phone: String? = null,
)
