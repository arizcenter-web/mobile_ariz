package com.tecsup.app_ariz.core.session

import com.tecsup.app_ariz.core.model.User

sealed interface SessionState {
    /** Aún no se sabe si hay sesión válida (arranque de la app). */
    data object Restoring : SessionState

    data object SignedOut : SessionState

    data class SignedIn(val user: User) : SessionState
}
