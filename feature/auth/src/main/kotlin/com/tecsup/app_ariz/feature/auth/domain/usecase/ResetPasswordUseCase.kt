package com.tecsup.app_ariz.feature.auth.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(token: String, newPassword: String): AppResult<Unit> =
        repository.resetPassword(token.trim(), newPassword)
}
