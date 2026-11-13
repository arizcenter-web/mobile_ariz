package com.tecsup.app_ariz.feature.rewards.data.mapper

import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.AccessResponse
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.CampaignPointsDto
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.CreateReferralRequest
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.IncentiveDto
import com.tecsup.app_ariz.feature.rewards.data.remote.dto.RedemptionDto
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignPoints
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignReferral
import com.tecsup.app_ariz.feature.rewards.domain.model.CodeAccess
import com.tecsup.app_ariz.feature.rewards.domain.model.Incentive
import com.tecsup.app_ariz.feature.rewards.domain.model.Redemption
import com.tecsup.app_ariz.feature.rewards.domain.model.RedemptionStatus
import com.tecsup.app_ariz.feature.rewards.domain.model.Referrer
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralRequest

internal fun AccessResponse.toDomain(): CodeAccess {
    val referrer = referrer
    return if (valid && referrer != null) {
        CodeAccess.Valid(
            Referrer(referrer.id, referrer.code, referrer.name, referrer.email, referrer.phone, referrer.totalPoints),
        )
    } else {
        CodeAccess.Invalid(message)
    }
}

internal fun IncentiveDto.toDomain() = Incentive(
    id = id,
    name = name,
    description = description,
    promotion = promotion?.takeIf { it.isNotBlank() },
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    pointsRequired = pointsRequired,
    stock = stock,
)

internal fun RedemptionDto.toDomain() = Redemption(
    id = id,
    incentiveName = incentiveName,
    imageUrl = incentiveImageUrl?.takeIf { it.isNotBlank() },
    pointsSpent = pointsSpent,
    status = runCatching { RedemptionStatus.valueOf(status) }.getOrDefault(RedemptionStatus.PENDING),
    requestedAtMillis = requestedAt.isoToEpochMillis(),
    rejectionReason = rejectionReason?.takeIf { it.isNotBlank() },
)

internal fun CampaignPointsDto.toDomain() = CampaignPoints(
    campaignName = campaign.name,
    totalPoints = totalPoints,
    referrals = referrals.map { CampaignReferral(it.referralName, it.status, it.points) },
)

internal fun ReferralRequest.toBody() = CreateReferralRequest(
    referrerCode = referrerCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() },
    referrerName = referrerName.trim(),
    referrerEmail = referrerEmail.trim(),
    referrerPhone = referrerPhone.trim(),
    name = name.trim(),
    email = email.trim(),
    phone = phone.trim(),
    interest = interest.name,
    availability = availability?.name,
)
