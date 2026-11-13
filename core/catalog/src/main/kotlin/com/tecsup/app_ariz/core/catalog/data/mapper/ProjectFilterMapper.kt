package com.tecsup.app_ariz.core.catalog.data.mapper

import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectOriginFilter
import com.tecsup.app_ariz.core.model.project.ProjectSortOrder
import com.tecsup.app_ariz.core.model.project.ProjectStage

/** Parámetros de `GET /projects` ya en el vocabulario del backend. */
internal data class ProjectQuery(
    val origin: String?,
    val stage: String?,
    val district: String?,
    val currency: String?,
    val priceMax: Int?,
    val sort: String,
)

internal fun ProjectFilters.toProjectQuery(): ProjectQuery {
    // El backend exige priceMax >= 1; sin presupuesto tampoco tiene sentido filtrar por moneda.
    val budget = priceMax?.takeIf { it > 0 }
    return ProjectQuery(
        origin = when (origin) {
            ProjectOriginFilter.ARIZ -> "ADMIN"
            ProjectOriginFilter.OTHERS -> "AGENT"
            null -> null
        },
        stage = stage?.let {
            when (it) {
                ProjectStage.PRE_SALE -> "PRE_SALE"
                ProjectStage.UNDER_CONSTRUCTION -> "UNDER_CONSTRUCTION"
                ProjectStage.DELIVERED -> "DELIVERED"
            }
        },
        district = district?.trim()?.takeIf { it.isNotEmpty() },
        currency = if (budget != null) currency.name else null,
        priceMax = budget,
        sort = when (sort) {
            ProjectSortOrder.RECENT -> "recent"
            ProjectSortOrder.FEATURED -> "featured"
            ProjectSortOrder.PRICE_ASC -> "price-asc"
            ProjectSortOrder.PRICE_DESC -> "price-desc"
        },
    )
}
