package com.tecsup.app_ariz.feature.investments.data.mapper

import com.tecsup.app_ariz.feature.investments.data.remote.InvestmentOpportunityDto
import com.tecsup.app_ariz.feature.investments.data.remote.InvestmentTypeDto
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentGoal
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentRisk
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentStatus
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentType

internal fun InvestmentTypeDto.toDomain() = InvestmentType(id = id, name = name, description = description?.takeIf { it.isNotBlank() })

internal fun InvestmentOpportunityDto.toDomain() = InvestmentOpportunity(
    id = id,
    typeId = investmentTypeId,
    // El web muestra el nombre del tipo y, si no hay, la clase de inversión.
    typeName = investmentType?.name ?: investmentKind,
    name = name,
    slug = slug,
    summary = summary,
    description = description,
    areaLabel = areaLabel,
    assetType = assetType,
    initialInvestment = initialInvestment,
    roiPercent = roiPercent,
    estimatedReturnLabel = estimatedReturnLabel?.takeIf { it.isNotBlank() },
    termMonths = estimatedTermMonths?.takeIf { it > 0 },
    goal = when (investmentGoal) {
        "construccion" -> InvestmentGoal.CONSTRUCCION
        "renta" -> InvestmentGoal.RENTA
        else -> InvestmentGoal.PLUSVALIA
    },
    status = runCatching { InvestmentStatus.valueOf(status) }.getOrDefault(InvestmentStatus.AVAILABLE),
    risk = riskLevel?.let { runCatching { InvestmentRisk.valueOf(it) }.getOrNull() },
    ctaLabel = ctaLabel,
    location = location,
    district = district,
    province = province,
    department = department,
    latitude = latitude,
    longitude = longitude,
    coverImageUrl = coverImageUrl?.takeIf { it.isNotBlank() },
    gallery = galleryImages,
    highlights = highlights,
)
