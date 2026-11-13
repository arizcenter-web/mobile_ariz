package com.tecsup.app_ariz.feature.auth.domain.repository

import android.content.Context
import com.tecsup.app_ariz.core.common.AppResult

/**
 * Obtiene un ID token de Firebase tras el selector de cuentas de Google, que es lo que verifica
 * `POST /auth/google` en el backend.
 *
 * Es la única interfaz del dominio que menciona `Context`: el selector de cuentas es UI del
 * sistema y necesita una Activity. El contexto solo se usa durante la llamada; no se almacena.
 */
interface GoogleIdTokenProvider {
    /** `false` si falta `google-services.json` / el client ID web: la UI oculta el botón. */
    val isConfigured: Boolean

    /** Devuelve `AppError.Cancelled` si el usuario cierra el selector. */
    suspend fun requestFirebaseIdToken(activityContext: Context): AppResult<String>
}
