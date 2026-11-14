package com.tecsup.app_ariz.feature.projects.domain.model

import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectStage

enum class LotStatus { AVAILABLE, RESERVED, SOLD, BLOCKED }

enum class LotMode { INDIVIDUAL, GROUP, OTHER }

data class ProjectLocation(
    val zone: String?,
    val street: String?,
    val district: String,
    val province: String,
    val department: String,
    val latitude: Double?,
    val longitude: Double?,
) {
    val hasCoordinates: Boolean get() = latitude != null && longitude != null

    /** "Zona, Calle, Distrito, Provincia, Departamento" sin partes vacías (`locationLine` del web). */
    val line: String
        get() = listOfNotNull(zone, street, district, province, department).filter { it.isNotBlank() }.joinToString(", ")
}

data class ProjectLot(
    val id: String,
    val number: String,
    val block: String?,
    val areaM2: Double,
    val price: Double,
    val status: LotStatus,
)

data class ProjectMilestone(val title: String, val dateMillis: Long?, val note: String?)

data class ProjectAdvertiser(
    val name: String,
    /** `true` si lo publica ARIZ (el web muestra "Publicado por ARIZ"). */
    val isArizOfficial: Boolean,
    val typeCode: String,
    val isVerified: Boolean,
    val whatsapp: String?,
    val phone: String?,
)

/** Detalle público de un proyecto (`GET /projects/:slug`). */
data class ProjectDetail(
    val id: String,
    val slug: String,
    val name: String,
    val description: String,
    val category: ProjectCategory,
    val stage: ProjectStage,
    val lotMode: LotMode,
    val currency: Currency,
    val priceFrom: Double,
    val priceTo: Double,
    val developer: String,
    val featured: Boolean,
    val location: ProjectLocation,
    val gallery: List<String>,
    val planUrls: List<String>,
    val renderUrls: List<String>,
    val tourUrl: String?,
    val publishedAtMillis: Long?,
    val constructionStartMillis: Long?,
    val estimatedDeliveryMillis: Long?,
    val totalLots: Int?,
    val totalBlocks: Int?,
    val minAreaM2: Double,
    val maxAreaM2: Double,
    val downPayment: Double?,
    val monthlyPayment: Double?,
    val financing: List<String>,
    val services: List<String>,
    val amenities: List<String>,
    val certifications: List<String>,
    val urbanizationName: String?,
    val registryEntry: String?,
    val ruc: String?,
    val lots: List<ProjectLot>,
    val milestones: List<ProjectMilestone>,
    val advertiser: ProjectAdvertiser,
    /** Hay un plano publicado (se ve con el código que entrega un asesor). */
    val hasPublishedSitePlan: Boolean = false,
) {
    val isApartments: Boolean get() = category == ProjectCategory.DEPARTAMENTOS
}
