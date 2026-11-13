package com.tecsup.app_ariz.feature.agents.data.remote.api

import com.tecsup.app_ariz.feature.agents.data.remote.dto.AgentProfileDto
import retrofit2.http.GET
import retrofit2.http.Path

internal interface AgentsApi {

    @GET("agents/{slugOrId}")
    suspend fun profile(@Path("slugOrId") slugOrId: String): AgentProfileDto
}
