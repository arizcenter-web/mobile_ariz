package com.tecsup.app_ariz.feature.rewards.data.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.rewards.data.mapper.toBody
import com.tecsup.app_ariz.feature.rewards.data.mapper.toDomain
import com.tecsup.app_ariz.feature.rewards.data.remote.api.RewardsApi
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.AccessRequest
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.CreateRedemptionRequest
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignPoints
import com.tecsup.app_ariz.feature.rewards.domain.model.CodeAccess
import com.tecsup.app_ariz.feature.rewards.domain.model.Incentive
import com.tecsup.app_ariz.feature.rewards.domain.model.Redemption
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralRequest
import com.tecsup.app_ariz.feature.rewards.domain.repository.RewardsRepository
import javax.inject.Inject

internal class RewardsRepositoryImpl @Inject constructor(
    private val api: RewardsApi,
) : RewardsRepository {

    override suspend fun validateCode(code: String): AppResult<CodeAccess> =
        safeApiCall { api.access(AccessRequest(code.trim().uppercase())) }.map { it.toDomain() }

    override suspend fun getCatalog(): AppResult<List<Incentive>> =
        safeApiCall { api.catalog() }.map { list -> list.map { it.toDomain() } }

    override suspend fun redeem(code: String, incentiveId: String): AppResult<Unit> =
        safeApiCall { api.redeem(CreateRedemptionRequest(code.trim().uppercase(), incentiveId)) }

    override suspend fun getMyRedemptions(code: String): AppResult<List<Redemption>> =
        safeApiCall { api.myRedemptions(code.trim().uppercase()) }.map { list -> list.map { it.toDomain() } }

    override suspend fun getCampaignPoints(code: String): AppResult<List<CampaignPoints>> =
        safeApiCall { api.campaignPoints(code.trim().uppercase()) }.map { response ->
            if (response.valid) response.campaigns.map { it.toDomain() } else emptyList()
        }

    override suspend fun submitReferral(request: ReferralRequest): AppResult<Unit> =
        safeApiCall { api.createReferral(request.toBody()) }
}
