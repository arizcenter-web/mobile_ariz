package com.tecsup.app_ariz.feature.customer.domain

import com.tecsup.app_ariz.core.favorites.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCustomerProfileUseCase @Inject constructor(private val repository: CustomerRepository) {
    suspend operator fun invoke() = repository.getProfile()
}

class UpdateCustomerProfileUseCase @Inject constructor(private val repository: CustomerRepository) {
    suspend operator fun invoke(update: CustomerProfileUpdate) = repository.updateProfile(update)
}

class ChangeCustomerPasswordUseCase @Inject constructor(private val repository: CustomerRepository) {
    suspend operator fun invoke(current: String, new: String) = repository.changePassword(current, new)
}

class GetMembershipUseCase @Inject constructor(private val repository: CustomerRepository) {
    suspend operator fun invoke() = repository.getMembership()
}

class RequestMembershipUseCase @Inject constructor(private val repository: CustomerRepository) {
    suspend operator fun invoke(pack: MembershipPackage, receiptUri: String) = repository.requestMembership(pack, receiptUri)
}

class GetPropertyStatsUseCase @Inject constructor(private val repository: CustomerRepository) {
    suspend operator fun invoke() = repository.getPropertyStats()
}

/** Cantidad de favoritos del cliente, en vivo. */
class ObserveFavoriteCountUseCase @Inject constructor(private val favorites: FavoritesRepository) {
    operator fun invoke(): Flow<Int> = favorites.favoriteIds.map { it.size }
}
