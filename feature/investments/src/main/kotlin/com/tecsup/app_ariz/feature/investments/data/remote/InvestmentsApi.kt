package com.tecsup.app_ariz.feature.investments.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
internal data class InvestmentTypeDto(
    val id: String,
    val name: String = "",
    val description: String? = null,
)

@Serializable
internal data class InvestmentOpportunityDto(
    val id: String,
    val investmentTypeId: String? = null,
    val investmentType: InvestmentTypeDto? = null,
    val name: String = "",
    val slug: String = "",
    val summary: String = "",
    val description: String = "",
    val areaLabel: String = "",
    val investmentKind: String = "",
    val assetType: String = "",
    val initialInvestment: Double = 0.0,
    val roiPercent: Double = 0.0,
    val estimatedReturnLabel: String? = null,
    val estimatedTermMonths: Int? = null,
    val investmentGoal: String = "plusvalia",
    val status: String = "AVAILABLE",
    val riskLevel: String? = null,
    val ctaLabel: String = "",
    val location: String = "",
    val department: String = "",
    val province: String = "",
    val district: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coverImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),
    val highlights: List<String> = emptyList(),
)

/** Endpoints públicos de `InvestmentOpportunitiesController`. */
internal interface InvestmentsApi {

    @GET("investment-opportunities")
    suspend fun opportunities(): List<InvestmentOpportunityDto>

    @GET("investment-opportunities/types")
    suspend fun types(): List<InvestmentTypeDto>

    @GET("investment-opportunities/{slug}")
    suspend fun opportunity(@Path("slug") slug: String): InvestmentOpportunityDto
}
