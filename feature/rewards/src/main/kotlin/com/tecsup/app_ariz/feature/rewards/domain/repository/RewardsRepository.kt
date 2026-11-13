package com.tecsup.app_ariz.feature.rewards.domain.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignPoints
import com.tecsup.app_ariz.feature.rewards.domain.model.CodeAccess
import com.tecsup.app_ariz.feature.rewards.domain.model.Incentive
import com.tecsup.app_ariz.feature.rewards.domain.model.Redemption
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralRequest

/** Programa de referentes (Ariz Club). Todas las llamadas se identifican con el código del referente. */
interface RewardsRepository {
    suspend fun validateCode(code: String): AppResult<CodeAccess>

    suspend fun getCatalog(): AppResult<List<Incentive>>

    suspend fun redeem(code: String, incentiveId: String): AppResult<Unit>

    suspend fun getMyRedemptions(code: String): AppResult<List<Redemption>>

    suspend fun getCampaignPoints(code: String): AppResult<List<CampaignPoints>>

    suspend fun submitReferral(request: ReferralRequest): AppResult<Unit>
}
