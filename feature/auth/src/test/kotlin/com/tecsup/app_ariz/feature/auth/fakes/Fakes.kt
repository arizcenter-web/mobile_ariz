package com.tecsup.app_ariz.feature.auth.fakes

import android.content.Context
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterCustomerData
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import com.tecsup.app_ariz.feature.auth.domain.repository.GoogleIdTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val testUser = User("u1", "Ana", "ana@correo.com", Role.USER, null)

class FakeSessionManager : SessionManager {
    private val _state = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val state: StateFlow<SessionState> = _state
    var signedInUser: User? = null

    override suspend fun restore() = Unit
    override fun onSignedIn(user: User) {
        signedInUser = user
        _state.value = SessionState.SignedIn(user)
    }

    override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
    override fun onSessionExpired() {
        _state.value = SessionState.SignedOut
    }
}

/** Repositorio configurable: todas las operaciones devuelven [result] y se anotan los argumentos. */
class FakeAuthRepository(var result: AppResult<User> = AppResult.Success(testUser)) : AuthRepository {
    var loginCalls = 0
    var lastGoogleToken: String? = null
    var lastProfessional: RegisterProfessionalData? = null
    var lastReset: Pair<String, String>? = null
    var unitResult: AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun login(email: String, password: String, rememberMe: Boolean): AppResult<User> {
        loginCalls++
        return result
    }

    override suspend fun loginWithGoogle(firebaseIdToken: String): AppResult<User> {
        lastGoogleToken = firebaseIdToken
        return result
    }

    override suspend fun registerCustomer(data: RegisterCustomerData): AppResult<User> = result

    override suspend fun registerProfessional(data: RegisterProfessionalData): AppResult<User> {
        lastProfessional = data
        return result
    }

    override suspend fun requestPasswordReset(email: String): AppResult<Unit> = unitResult

    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> {
        lastReset = token to newPassword
        return unitResult
    }
}

class FakeGoogleIdTokenProvider(
    override val isConfigured: Boolean = true,
    var tokenResult: AppResult<String> = AppResult.Success("firebase-token"),
) : GoogleIdTokenProvider {
    override suspend fun requestFirebaseIdToken(activityContext: Context): AppResult<String> = tokenResult
}
