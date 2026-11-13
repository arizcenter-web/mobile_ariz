package com.tecsup.app_ariz.core.session

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Única fuente de verdad de "quién está autenticado". Cualquier feature puede leer [state]; solo
 * `feature:auth` llama a [onSignedIn] y solo la UI de cuenta llama a [signOut].
 */
interface SessionManager {
    val state: StateFlow<SessionState>

    /** Al abrir la app: si hay cookie guardada, valida la sesión con GET /auth/me. */
    suspend fun restore()

    /** Registra al usuario devuelto por login/registro (la cookie ya la guardó la capa de red). */
    fun onSignedIn(user: User)

    /** POST /auth/logout y borrado de la cookie local, aunque la llamada falle. */
    suspend fun signOut(): AppResult<Unit>

    /** El backend respondió 401 en una ruta protegida: la sesión ya no es válida. */
    fun onSessionExpired()
}
