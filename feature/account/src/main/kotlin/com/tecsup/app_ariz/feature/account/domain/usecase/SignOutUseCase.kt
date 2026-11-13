package com.tecsup.app_ariz.feature.account.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.session.SessionManager
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val session: SessionManager,
) {
    suspend operator fun invoke(): AppResult<Unit> = session.signOut()
}
