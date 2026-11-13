package com.tecsup.app_ariz.feature.projects.data.remote.api

import com.tecsup.app_ariz.feature.projects.data.remote.dto.ProjectDetailDto
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ProjectsApi {

    @GET("projects/{slug}")
    suspend fun projectBySlug(@Path("slug") slug: String): ProjectDetailDto
}
