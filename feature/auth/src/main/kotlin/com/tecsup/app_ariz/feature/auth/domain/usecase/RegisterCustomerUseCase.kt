package com.tecsup.app_ariz.feature.auth.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.onSuccess
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterCustomerData
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterCustomerUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val session: SessionManager,
) {
    suspend operator fun invoke(data: RegisterCustomerData): AppResult<User> =
        repository.registerCustomer(
            data.copy(
                firstName = data.firstName.trim(),
                lastName = data.lastName.trim(),
                email = data.email.trim(),
                phone = data.phone.trim(),
            ),
        ).onSuccess(session::onSignedIn)
}
