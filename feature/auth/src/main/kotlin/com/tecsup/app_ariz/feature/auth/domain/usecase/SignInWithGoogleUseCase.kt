package com.tecsup.app_ariz.feature.auth.domain.usecase

import android.content.Context
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.onSuccess
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import com.tecsup.app_ariz.feature.auth.domain.repository.GoogleIdTokenProvider
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val tokenProvider: GoogleIdTokenProvider,
    private val repository: AuthRepository,
    private val session: SessionManager,
) {
    val isAvailable: Boolean get() = tokenProvider.isConfigured

    suspend operator fun invoke(activityContext: Context): AppResult<User> =
        when (val token = tokenProvider.requestFirebaseIdToken(activityContext)) {
            is AppResult.Failure -> token
            is AppResult.Success -> repository.loginWithGoogle(token.data).onSuccess(session::onSignedIn)
        }
}
