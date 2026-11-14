package com.tecsup.app_ariz.feature.customer.data

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.media.MediaRepository
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.customer.domain.CustomerProfile
import com.tecsup.app_ariz.feature.customer.domain.CustomerProfileUpdate
import com.tecsup.app_ariz.feature.customer.domain.CustomerRepository
import com.tecsup.app_ariz.feature.customer.domain.Membership
import com.tecsup.app_ariz.feature.customer.domain.MembershipPackage
import com.tecsup.app_ariz.feature.customer.domain.MembershipStatus
import com.tecsup.app_ariz.feature.customer.domain.MembershipSummary
import com.tecsup.app_ariz.feature.customer.domain.PropertyStats
import javax.inject.Inject

internal class CustomerRepositoryImpl @Inject constructor(
    private val api: CustomerApi,
    private val media: MediaRepository,
) : CustomerRepository {

    override suspend fun getProfile(): AppResult<CustomerProfile> = safeApiCall { api.profile() }.map { it.toDomain() }

    override suspend fun updateProfile(update: CustomerProfileUpdate): AppResult<CustomerProfile> =
        safeApiCall {
            api.updateProfile(CustomerProfileBody(update.firstName.trim(), update.lastName.trim(), update.phone.trim(), update.biography.trim()))
        }.map { it.toDomain() }

    override suspend fun changePassword(current: String, new: String): AppResult<Unit> =
        safeApiCall { api.changePassword(CustomerPasswordBody(current, new)) }.map { }

    override suspend fun getMembership(): AppResult<MembershipSummary> = safeApiCall { api.membership() }.map { dto ->
        MembershipSummary(
            membership = dto.subscription?.let { s ->
                Membership(
                    status = runCatching { MembershipStatus.valueOf(s.status) }.getOrDefault(MembershipStatus.PENDING_PAYMENT),
                    propertyLimit = s.plan?.propertyLimit ?: 0,
                    expiresAtMillis = s.expiresAt.isoToEpochMillis(),
                    rejectionReason = s.rejectionReason,
                )
            },
            publishedCount = dto.propertyCount,
            trialAvailable = dto.trialAvailable,
        )
    }

    override suspend fun getPropertyStats(): AppResult<PropertyStats> = safeApiCall { api.myProperties() }.map { list ->
        PropertyStats(
            total = list.size,
            published = list.count { it.status == "PUBLISHED" },
            paused = list.count { it.status == "PAUSED" },
            rentals = list.count { it.operation.equals("rent", ignoreCase = true) },
        )
    }

    override suspend fun requestMembership(pack: MembershipPackage, receiptUri: String): AppResult<Unit> {
        val upload = when (val r = media.uploadImage(receiptUri, folder = "ariz/receipts")) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return r
        }
        return safeApiCall {
            api.requestMembership(MembershipRequestBody(pack.propertyLimit, pack.durationDays, pack.price, upload.url))
        }.map { }
    }
}

private fun CustomerProfileDto.toDomain() = CustomerProfile(
    firstName = firstName,
    lastName = lastName.orEmpty(),
    email = email,
    phone = phone.orEmpty(),
    biography = biography.orEmpty(),
)
