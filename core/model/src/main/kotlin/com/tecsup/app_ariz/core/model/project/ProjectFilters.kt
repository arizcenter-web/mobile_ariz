package com.tecsup.app_ariz.core.model.project

import com.tecsup.app_ariz.core.model.common.Currency

/** "Publicado por" del directorio: proyectos de ARIZ u de otras inmobiliarias. */
enum class ProjectOriginFilter { ARIZ, OTHERS }

enum class ProjectSortOrder { RECENT, FEATURED, PRICE_ASC, PRICE_DESC }

/**
 * Filtros del directorio de proyectos (`ProjectDirectory` del web). Los `null` significan "sin
 * filtro". [priceMax] se interpreta en [currency] y solo se envía si hay presupuesto.
 */
data class ProjectFilters(
    val stage: ProjectStage? = null,
    val district: String? = null,
    val origin: ProjectOriginFilter? = null,
    val currency: Currency = Currency.PEN,
    val priceMax: Int? = null,
    val sort: ProjectSortOrder = ProjectSortOrder.RECENT,
)
