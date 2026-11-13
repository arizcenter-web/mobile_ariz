package com.tecsup.app_ariz.feature.investments.domain.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentType

interface InvestmentsRepository {
    suspend fun getOpportunities(): AppResult<List<InvestmentOpportunity>>

    suspend fun getTypes(): AppResult<List<InvestmentType>>

    suspend fun getOpportunity(slug: String): AppResult<InvestmentOpportunity>
}
