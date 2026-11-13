package com.tecsup.app_ariz.feature.auth.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RequestPasswordResetUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String): AppResult<Unit> =
        repository.requestPasswordReset(email.trim())
}
