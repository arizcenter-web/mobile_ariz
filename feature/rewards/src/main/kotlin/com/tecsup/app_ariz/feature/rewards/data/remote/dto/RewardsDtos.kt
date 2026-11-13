package com.tecsup.app_ariz.feature.rewards.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class AccessRequest(val referralCode: String)

@Serializable
internal data class ReferrerDto(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val totalPoints: Int = 0,
)

@Serializable
internal data class AccessResponse(
    val valid: Boolean = false,
    val message: String? = null,
    val referrer: ReferrerDto? = null,
)

@Serializable
internal data class IncentiveDto(
    val id: String,
    val name: String = "",
    val description: String = "",
    val promotion: String? = null,
    val imageUrl: String? = null,
    val pointsRequired: Int = 0,
    val stock: Int = 0,
)

@Serializable
internal data class CreateRedemptionRequest(val referralCode: String, val incentiveId: String, val quantity: Int = 1)

@Serializable
internal data class RedemptionDto(
    val id: String,
    val incentiveName: String = "",
    val incentiveImageUrl: String? = null,
    val pointsSpent: Int = 0,
    val status: String = "PENDING",
    val requestedAt: String? = null,
    val rejectionReason: String? = null,
)

@Serializable
internal data class CampaignInfoDto(val name: String = "")

@Serializable
internal data class CampaignReferralDto(
    val referralName: String = "",
    val status: String = "",
    val points: Int = 0,
)

@Serializable
internal data class CampaignPointsDto(
    val campaign: CampaignInfoDto = CampaignInfoDto(),
    val totalPoints: Int = 0,
    val referrals: List<CampaignReferralDto> = emptyList(),
)

@Serializable
internal data class CampaignPointsResponse(
    val valid: Boolean = false,
    val campaigns: List<CampaignPointsDto> = emptyList(),
)

/** Cuerpo de `POST /referrals`. Los campos opcionales `null` no se serializan. */
@Serializable
internal data class CreateReferralRequest(
    val referrerCode: String? = null,
    val referrerName: String,
    val referrerEmail: String,
    val referrerPhone: String,
    val name: String,
    val email: String,
    val phone: String,
    val interest: String,
    val availability: String? = null,
)
