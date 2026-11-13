package com.tecsup.app_ariz.feature.investments.data.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.investments.data.mapper.toDomain
import com.tecsup.app_ariz.feature.investments.data.remote.InvestmentsApi
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentType
import com.tecsup.app_ariz.feature.investments.domain.repository.InvestmentsRepository
import javax.inject.Inject

internal class InvestmentsRepositoryImpl @Inject constructor(
    private val api: InvestmentsApi,
) : InvestmentsRepository {

    override suspend fun getOpportunities(): AppResult<List<InvestmentOpportunity>> =
        safeApiCall { api.opportunities() }.map { list -> list.map { it.toDomain() } }

    override suspend fun getTypes(): AppResult<List<InvestmentType>> =
        safeApiCall { api.types() }.map { list -> list.map { it.toDomain() } }

    override suspend fun getOpportunity(slug: String): AppResult<InvestmentOpportunity> =
        safeApiCall { api.opportunity(slug) }.map { it.toDomain() }
}
