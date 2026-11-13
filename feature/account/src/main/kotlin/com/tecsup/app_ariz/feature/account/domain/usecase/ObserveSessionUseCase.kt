package com.tecsup.app_ariz.feature.account.domain.usecase

import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveSessionUseCase @Inject constructor(
    private val session: SessionManager,
) {
    operator fun invoke(): StateFlow<SessionState> = session.state
}
