package com.tecsup.app_ariz.core.model.project

import com.tecsup.app_ariz.core.model.common.Currency

enum class ProjectStage { PRE_SALE, UNDER_CONSTRUCTION, DELIVERED }

enum class ProjectCategory { LOTIZACION, DEPARTAMENTOS }

/** Resumen de un proyecto inmobiliario para tarjetas de listado. Es lo que devuelve `GET /projects`. */
data class ProjectSummary(
    val id: String,
    val slug: String,
    val name: String,
    val developer: String,
    val priceFrom: Double,
    val currency: Currency,
    val coverImageUrl: String?,
    val stage: ProjectStage,
    val category: ProjectCategory,
    val minAreaM2: Double,
    val totalUnits: Int,
    val district: String,
    val publishedAtMillis: Long?,
    val featured: Boolean = false,
    /** Precio del lote más barato; el web lo prefiere a `priceFrom` en el directorio de proyectos. */
    val cheapestPrice: Double? = null,
    /** Punto del proyecto en el mapa; `null` si aún no se definió. */
    val latitude: Double? = null,
    val longitude: Double? = null,
)
