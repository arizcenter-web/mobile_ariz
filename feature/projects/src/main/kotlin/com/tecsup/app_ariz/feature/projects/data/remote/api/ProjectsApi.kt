package com.tecsup.app_ariz.feature.projects.data.remote.api

import com.tecsup.app_ariz.feature.projects.data.remote.dto.ProjectDetailDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

internal interface ProjectsApi {

    @GET("projects/{slug}")
    suspend fun projectBySlug(@Path("slug") slug: String): ProjectDetailDto

    /** Plano publicado: exige el código que coordina un asesor (403 si falta o es incorrecto). */
    @GET("projects/{slug}/site-plan")
    suspend fun sitePlan(@Path("slug") slug: String, @Query("code") code: String): ResponseBody

    @Streaming
    @GET("projects/{slug}/site-plan/lots/{id}/pdf")
    suspend fun lotSheet(@Path("slug") slug: String, @Path("id") id: String, @Query("code") code: String): ResponseBody

    @Streaming
    @GET("projects/{slug}/site-plan/apartments/{id}/pdf")
    suspend fun apartmentSheet(@Path("slug") slug: String, @Path("id") id: String, @Query("code") code: String): ResponseBody
}
