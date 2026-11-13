package com.tecsup.app_ariz.feature.investments.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentType
import com.tecsup.app_ariz.feature.investments.domain.repository.InvestmentsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Oportunidades y categorías de inversión. */
data class InvestmentCatalog(
    val opportunities: List<InvestmentOpportunity>,
    val types: List<InvestmentType>,
)

/**
 * Carga el catálogo en paralelo. Las oportunidades son imprescindibles; las categorías son un
 * filtro opcional: si fallan, se muestra el catálogo sin ellas.
 */
class GetInvestmentCatalogUseCase @Inject constructor(
    private val repository: InvestmentsRepository,
) {
    suspend operator fun invoke(): AppResult<InvestmentCatalog> = coroutineScope {
        val opportunities = async { repository.getOpportunities() }
        val types = async { repository.getTypes() }
        when (val result = opportunities.await()) {
            is AppResult.Failure -> result
            is AppResult.Success -> AppResult.Success(
                InvestmentCatalog(result.data, (types.await() as? AppResult.Success)?.data.orEmpty()),
            )
        }
    }
}

class GetInvestmentOpportunityUseCase @Inject constructor(
    private val repository: InvestmentsRepository,
) {
    suspend operator fun invoke(slug: String): AppResult<InvestmentOpportunity> = repository.getOpportunity(slug)
}
