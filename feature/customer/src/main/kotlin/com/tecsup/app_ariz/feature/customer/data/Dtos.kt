package com.tecsup.app_ariz.feature.customer.data

import kotlinx.serialization.Serializable

@Serializable
internal data class CustomerProfileDto(
    val firstName: String = "",
    val lastName: String? = null,
    val email: String = "",
    val phone: String? = null,
    val biography: String? = null,
)

@Serializable
internal data class CustomerProfileBody(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val biography: String,
)

@Serializable
internal data class CustomerPasswordBody(val currentPassword: String, val newPassword: String)

@Serializable
internal data class MembershipPlanDto(val propertyLimit: Int = 0)

@Serializable
internal data class MembershipSubscriptionDto(
    val status: String = "PENDING_PAYMENT",
    val rejectionReason: String? = null,
    val expiresAt: String? = null,
    val plan: MembershipPlanDto? = null,
)

@Serializable
internal data class MembershipDto(
    val subscription: MembershipSubscriptionDto? = null,
    val propertyCount: Int = 0,
    val trialAvailable: Boolean = false,
)

@Serializable
internal data class CustomerPropertyDto(val status: String = "", val operation: String = "buy")

@Serializable
internal data class MembershipRequestBody(
    val propertyLimit: Int,
    val durationDays: Int,
    val price: Double,
    val receiptUrl: String,
)
