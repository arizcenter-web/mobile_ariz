package com.tecsup.app_ariz.feature.listings.domain.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail

interface ListingsRepository {
    suspend fun getPropertyDetail(slug: String): AppResult<PropertyDetail>

    /** Crea (o reactiva) el aviso de bajada de precio de la propiedad. Requiere sesión. */
    suspend fun createPriceAlert(propertyId: String): AppResult<Unit>
}
