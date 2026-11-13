package com.tecsup.app_ariz.feature.account.ui.account

import com.tecsup.app_ariz.core.session.SessionState

data class AccountUiState(
    val session: SessionState = SessionState.Restoring,
    val isSigningOut: Boolean = false,
)

sealed interface AccountAction {
    data object SignOut : AccountAction
}
