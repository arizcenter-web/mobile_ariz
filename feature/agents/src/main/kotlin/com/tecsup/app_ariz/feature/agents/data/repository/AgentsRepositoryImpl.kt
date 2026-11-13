package com.tecsup.app_ariz.feature.agents.data.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.agents.data.mapper.toDomain
import com.tecsup.app_ariz.feature.agents.data.remote.api.AgentsApi
import com.tecsup.app_ariz.feature.agents.domain.model.AgentProfile
import com.tecsup.app_ariz.feature.agents.domain.repository.AgentsRepository
import javax.inject.Inject

internal class AgentsRepositoryImpl @Inject constructor(
    private val api: AgentsApi,
) : AgentsRepository {

    override suspend fun getProfile(slugOrId: String): AppResult<AgentProfile> =
        safeApiCall { api.profile(slugOrId) }.map { it.toDomain() }
}
