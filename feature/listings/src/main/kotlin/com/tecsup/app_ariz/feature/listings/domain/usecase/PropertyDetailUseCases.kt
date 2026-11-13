package com.tecsup.app_ariz.feature.listings.domain.usecase

import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail
import com.tecsup.app_ariz.feature.listings.domain.repository.ListingsRepository
import javax.inject.Inject
import kotlin.math.abs

private const val SimilarCandidates = 24
private const val SimilarLimit = 5

class GetPropertyDetailUseCase @Inject constructor(
    private val repository: ListingsRepository,
) {
    suspend operator fun invoke(slug: String): AppResult<PropertyDetail> = repository.getPropertyDetail(slug)
}

/** "Avísame si baja de precio": crea el aviso de la propiedad para el usuario autenticado. */
class CreatePriceAlertUseCase @Inject constructor(
    private val repository: ListingsRepository,
) {
    suspend operator fun invoke(propertyId: String): AppResult<Unit> = repository.createPriceAlert(propertyId)
}

/**
 * Propiedades parecidas: misma operación y tipo, sin contar la actual, las 5 de precio más cercano
 * (el web ordena por afinidad; el precio es lo que más pesa). Si falla, simplemente no hay similares.
 */
class GetSimilarPropertiesUseCase @Inject constructor(
    private val catalog: CatalogRepository,
) {
    suspend operator fun invoke(property: PropertyDetail): List<PropertySummary> {
        val filters = PropertyFilters(operation = property.operation, type = property.type)
        val result = catalog.searchProperties(filters, pageSize = SimilarCandidates)
        val candidates = (result as? AppResult.Success)?.data?.items.orEmpty()
        return candidates
            .filter { it.id != property.id }
            .sortedBy { abs(it.price - property.price) }
            .take(SimilarLimit)
    }
}
