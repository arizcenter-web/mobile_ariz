package com.tecsup.app_ariz.feature.agents.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.agents.domain.model.AgentProfile
import com.tecsup.app_ariz.feature.agents.domain.repository.AgentsRepository
import javax.inject.Inject

class GetAgentProfileUseCase @Inject constructor(
    private val repository: AgentsRepository,
) {
    suspend operator fun invoke(slugOrId: String): AppResult<AgentProfile> = repository.getProfile(slugOrId)
}
