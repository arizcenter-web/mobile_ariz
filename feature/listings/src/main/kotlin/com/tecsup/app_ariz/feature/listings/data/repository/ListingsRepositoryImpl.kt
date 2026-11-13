package com.tecsup.app_ariz.feature.listings.data.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.listings.data.mapper.toDomain
import com.tecsup.app_ariz.feature.listings.data.remote.api.ListingsApi
import com.tecsup.app_ariz.feature.listings.data.remote.api.PriceAlertRequest
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail
import com.tecsup.app_ariz.feature.listings.domain.repository.ListingsRepository
import javax.inject.Inject

internal class ListingsRepositoryImpl @Inject constructor(
    private val api: ListingsApi,
) : ListingsRepository {

    override suspend fun getPropertyDetail(slug: String): AppResult<PropertyDetail> =
        safeApiCall { api.propertyBySlug(slug) }.map { it.toDomain() }

    override suspend fun createPriceAlert(propertyId: String): AppResult<Unit> =
        safeApiCall { api.createPriceAlert(PriceAlertRequest(propertyId)) }
}
