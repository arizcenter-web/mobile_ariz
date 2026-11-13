package com.tecsup.app_ariz.feature.account.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.feature.account.domain.usecase.ObserveSessionUseCase
import com.tecsup.app_ariz.feature.account.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val isSigningOut = MutableStateFlow(false)

    val state: StateFlow<AccountUiState> = combine(observeSession(), isSigningOut) { session, signingOut ->
        AccountUiState(session = session, isSigningOut = signingOut)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AccountUiState())

    fun onAction(action: AccountAction) {
        when (action) {
            AccountAction.SignOut -> signOut()
        }
    }

    private fun signOut() {
        if (isSigningOut.value) return
        isSigningOut.value = true
        viewModelScope.launch {
            // La sesión local se cierra siempre (ver SessionManager.signOut); no hay error que mostrar.
            signOutUseCase()
            isSigningOut.update { false }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
