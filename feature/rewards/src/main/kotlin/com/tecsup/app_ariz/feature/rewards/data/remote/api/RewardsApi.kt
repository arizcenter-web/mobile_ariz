package com.tecsup.app_ariz.feature.rewards.data.remote.api

import com.tecsup.app_ariz.feature.rewards.data.remote.dto.AccessRequest
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.AccessResponse
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.CampaignPointsResponse
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.CreateRedemptionRequest
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.CreateReferralRequest
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.IncentiveDto
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.RedemptionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Endpoints públicos del programa de referentes: se identifican con el código, sin sesión. */
internal interface RewardsApi {

    @POST("redemptions/access")
    suspend fun access(@Body body: AccessRequest): AccessResponse

    @GET("redemptions/catalog")
    suspend fun catalog(): List<IncentiveDto>

    @POST("redemptions")
    suspend fun redeem(@Body body: CreateRedemptionRequest)

    @GET("redemptions/my")
    suspend fun myRedemptions(@Query("code") code: String): List<RedemptionDto>

    @GET("referrals/my-campaign-points")
    suspend fun campaignPoints(@Query("code") code: String): CampaignPointsResponse

    @POST("referrals")
    suspend fun createReferral(@Body body: CreateReferralRequest)
}
