package com.tecsup.app_ariz.feature.auth.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.onSuccess
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val session: SessionManager,
) {
    suspend operator fun invoke(email: String, password: String, rememberMe: Boolean): AppResult<User> =
        repository.login(email.trim(), password, rememberMe).onSuccess(session::onSignedIn)
}
