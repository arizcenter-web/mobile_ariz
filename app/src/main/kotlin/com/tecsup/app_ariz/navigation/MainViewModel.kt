package com.tecsup.app_ariz.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.network.SessionEvents
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    sessionEvents: SessionEvents,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = sessionManager.state

    /** Se emite cuando el backend invalida la sesión (401 en ruta protegida). */
    val sessionExpired: SharedFlow<Unit> = sessionEvents.expired

    init {
        // Al abrir la app se valida la cookie guardada con /auth/me.
        viewModelScope.launch { sessionManager.restore() }
    }

    fun onSessionExpired() = sessionManager.onSessionExpired()
}
