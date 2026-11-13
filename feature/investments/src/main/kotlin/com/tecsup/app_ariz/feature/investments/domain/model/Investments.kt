package com.tecsup.app_ariz.feature.investments.domain.model

enum class InvestmentGoal { PLUSVALIA, CONSTRUCCION, RENTA }

enum class InvestmentStatus { AVAILABLE, COMING_SOON, CLOSED }

enum class InvestmentRisk { LOW, MEDIUM, HIGH }

/** Categoría de inversión ("Terrenos", "Preventa"...), `GET /investment-opportunities/types`. */
data class InvestmentType(
    val id: String,
    val name: String,
    val description: String?,
)

/** Oportunidad de inversión publicada (`GET /investment-opportunities` y `/:slug`). Los montos son en soles. */
data class InvestmentOpportunity(
    val id: String,
    val typeId: String?,
    val typeName: String,
    val name: String,
    val slug: String,
    val summary: String,
    val description: String,
    val areaLabel: String,
    val assetType: String,
    val initialInvestment: Double,
    val roiPercent: Double,
    /** Texto de retorno que prefiere mostrar el admin ("12% anual"); si falta se usa "[roi]% ROI". */
    val estimatedReturnLabel: String?,
    val termMonths: Int?,
    val goal: InvestmentGoal,
    val status: InvestmentStatus,
    val risk: InvestmentRisk?,
    val ctaLabel: String,
    val location: String,
    val district: String,
    val province: String,
    val department: String,
    val latitude: Double?,
    val longitude: Double?,
    val coverImageUrl: String?,
    val gallery: List<String>,
    val highlights: List<String>,
) {
    val hasCoordinates: Boolean get() = latitude != null && longitude != null

    val returnLabel: String get() = estimatedReturnLabel ?: "${roiPercent.toInt()}% ROI"
}

/** Resultado del evaluador de inversión para un capital, un plazo y un objetivo. */
data class InvestmentSimulation(
    /** Oportunidades que se pueden pagar con el capital (y que encajan con el objetivo). */
    val compatible: List<InvestmentOpportunity>,
    /** Retorno referencial en soles. */
    val estimatedReturn: Long,
)
