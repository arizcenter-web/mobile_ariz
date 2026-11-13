package com.tecsup.app_ariz.feature.search.domain.usecase

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySummary
import javax.inject.Inject

const val SearchPageSize = 10

/** Busca propiedades publicadas con los filtros dados, de [SearchPageSize] en [SearchPageSize]. */
class SearchPropertiesUseCase @Inject constructor(
    private val catalog: CatalogRepository,
) {
    suspend operator fun invoke(filters: PropertyFilters, page: Int): AppResult<Page<PropertySummary>> =
        catalog.searchProperties(filters, pageSize = SearchPageSize, page = page)
}
