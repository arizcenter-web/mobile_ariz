package com.tecsup.app_ariz.feature.agents.domain.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.agents.domain.model.AgentProfile

interface AgentsRepository {
    suspend fun getProfile(slugOrId: String): AppResult<AgentProfile>
}
