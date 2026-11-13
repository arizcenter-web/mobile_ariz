package com.tecsup.app_ariz.core.session

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.network.SessionCookieStore
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.core.session.dto.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSessionManager @Inject constructor(
    private val api: SessionApi,
    private val cookies: SessionCookieStore,
) : SessionManager {

    private val _state = MutableStateFlow<SessionState>(SessionState.Restoring)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    override suspend fun restore() {
        if (!cookies.hasSession()) {
            _state.value = SessionState.SignedOut
            return
        }
        _state.value = when (val result = safeApiCall { api.me() }) {
            is AppResult.Success -> SessionState.SignedIn(result.data.user.toDomain())
            is AppResult.Failure -> {
                // Solo un 401 prueba que la cookie ya no sirve. Sin red se conserva para reintentar luego.
                if (result.error is AppError.Unauthorized) cookies.clear()
                SessionState.SignedOut
            }
        }
    }

    override fun onSignedIn(user: User) {
        _state.value = SessionState.SignedIn(user)
    }

    override suspend fun signOut(): AppResult<Unit> {
        val result = safeApiCall { api.logout() }
        // La sesión local se cierra siempre: el usuario pidió salir aunque no haya conexión.
        cookies.clear()
        _state.value = SessionState.SignedOut
        return result
    }

    override fun onSessionExpired() {
        cookies.clear()
        _state.value = SessionState.SignedOut
    }
}
