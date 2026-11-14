package com.tecsup.app_ariz.feature.projects.data.mapper

import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.feature.projects.data.remote.dto.ProjectDetailDto
import com.tecsup.app_ariz.feature.projects.domain.model.LotMode
import com.tecsup.app_ariz.feature.projects.domain.model.LotStatus
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectAdvertiser
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectLocation
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectLot
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectMilestone

internal fun ProjectDetailDto.toDomain(): ProjectDetail {
    // El web usa las fotos de `media` (type IMAGE) y, si no hay, la lista `images` del resumen.
    val mediaImages = media.filter { it.type == "IMAGE" }.map { it.url }
    return ProjectDetail(
        id = id,
        slug = slug,
        name = name,
        description = description,
        category = runCatching { ProjectCategory.valueOf(category) }.getOrDefault(ProjectCategory.LOTIZACION),
        stage = runCatching { ProjectStage.valueOf(stage) }.getOrDefault(ProjectStage.PRE_SALE),
        lotMode = when (lotMode) {
            "INDIVIDUAL" -> LotMode.INDIVIDUAL
            "GROUP" -> LotMode.GROUP
            else -> LotMode.OTHER
        },
        currency = runCatching { Currency.valueOf(currency) }.getOrDefault(Currency.PEN),
        priceFrom = priceFrom,
        priceTo = priceTo,
        developer = developer,
        featured = featured,
        location = ProjectLocation(
            zone = location?.zone?.takeIf { it.isNotBlank() },
            street = location?.street?.takeIf { it.isNotBlank() },
            district = location?.district.orEmpty(),
            province = location?.province.orEmpty(),
            department = location?.department.orEmpty(),
            latitude = location?.latitude,
            longitude = location?.longitude,
        ),
        gallery = mediaImages.ifEmpty { images },
        planUrls = media.filter { it.type == "FLOOR_PLAN" }.map { it.url },
        renderUrls = media.filter { it.type == "RENDER" }.map { it.url },
        tourUrl = tourUrl?.takeIf { it.isNotBlank() },
        publishedAtMillis = (publishedAt ?: createdAt).isoToEpochMillis(),
        constructionStartMillis = constructionStart.isoToEpochMillis(),
        estimatedDeliveryMillis = estimatedDelivery.isoToEpochMillis(),
        totalLots = totalLots?.takeIf { it > 0 },
        totalBlocks = totalBlocks?.takeIf { it > 0 },
        minAreaM2 = areaRange.min,
        maxAreaM2 = areaRange.max,
        downPayment = downPayment?.takeIf { it > 0 },
        monthlyPayment = monthlyPayment?.takeIf { it > 0 },
        financing = financing,
        services = services,
        amenities = amenities,
        certifications = certifications,
        urbanizationName = urbanizationName?.takeIf { it.isNotBlank() },
        registryEntry = registryEntry?.takeIf { it.isNotBlank() },
        ruc = ruc?.takeIf { it.isNotBlank() },
        lots = lots.map {
            ProjectLot(
                id = it.id,
                number = it.lotNumber,
                block = it.block?.takeIf { block -> block.isNotBlank() },
                areaM2 = it.areaM2,
                price = it.price,
                status = runCatching { LotStatus.valueOf(it.status) }.getOrDefault(LotStatus.AVAILABLE),
            )
        },
        milestones = milestones.map {
            ProjectMilestone(title = it.title, dateMillis = it.date.isoToEpochMillis(), note = it.note?.takeIf { n -> n.isNotBlank() })
        },
        advertiser = ProjectAdvertiser(
            name = agent.name,
            isArizOfficial = agent.type == "ADMIN",
            typeCode = agent.type,
            isVerified = agent.status == "APPROVED",
            whatsapp = agent.whatsapp?.takeIf { it.isNotBlank() },
            phone = agent.phone?.takeIf { it.isNotBlank() },
        ),
        hasPublishedSitePlan = hasPublishedSitePlan,
    )
}
