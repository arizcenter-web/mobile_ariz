package com.tecsup.app_ariz.feature.customer.domain

import com.tecsup.app_ariz.core.common.AppResult

interface CustomerRepository {
    suspend fun getProfile(): AppResult<CustomerProfile>
    suspend fun updateProfile(update: CustomerProfileUpdate): AppResult<CustomerProfile>
    suspend fun changePassword(current: String, new: String): AppResult<Unit>
    suspend fun getMembership(): AppResult<MembershipSummary>
    suspend fun getPropertyStats(): AppResult<PropertyStats>

    /** Sube la foto del comprobante y solicita el paquete con su URL. */
    suspend fun requestMembership(pack: MembershipPackage, receiptUri: String): AppResult<Unit>
}
