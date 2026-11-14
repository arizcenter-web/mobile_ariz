package com.tecsup.app_ariz.feature.professional.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoints del panel del profesional (`REAL_ESTATE`); todos usan la cookie de sesión. */
internal interface ProfessionalApi {

    @GET("professionals/me")
    suspend fun profile(): ProfileDto

    @PATCH("professionals/me")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): ProfileDto

    @PATCH("professionals/me/password")
    suspend fun changePassword(@Body body: PasswordChangeRequest)

    @GET("professionals/me/settings")
    suspend fun settings(): SettingsDto

    @PATCH("professionals/me/settings")
    suspend fun updateSettings(@Body body: SettingsDto): SettingsDto

    @GET("properties/owner/me")
    suspend fun myProperties(): List<OwnerPropertyDto>

    @GET("properties/owner/{id}")
    suspend fun propertyForEdit(@Path("id") id: String): PropertyEditDto

    @POST("properties")
    suspend fun createProperty(@Body body: PropertyBody): OwnerPropertyDto

    /** Propiedad de ARIZ (solo administrador): sin plan ni membresía. */
    @POST("admin/properties")
    suspend fun createAdminProperty(@Body body: PropertyBody): OwnerPropertyDto

    @PATCH("properties/{id}")
    suspend fun updateProperty(@Path("id") id: String, @Body body: PropertyBody): OwnerPropertyDto

    @PATCH("properties/{id}/publish")
    suspend fun publishProperty(@Path("id") id: String): OwnerPropertyDto

    @DELETE("properties/{id}")
    suspend fun deleteProperty(@Path("id") id: String)

    @GET("projects/owner/me")
    suspend fun myProjects(): List<OwnerProjectDto>

    @POST("projects")
    suspend fun createProject(@Body body: ProjectBody): JsonObject

    /** Detalle completo de todos mis proyectos (con lotes, medios y demás datos). */
    @GET("projects/owner/me")
    suspend fun myProjectsRaw(): JsonArray

    @GET("admin/projects/{id}")
    suspend fun adminProject(@Path("id") id: String): JsonObject

    @PATCH("projects/{id}")
    suspend fun updateProject(@Path("id") id: String, @Body body: JsonObject): JsonObject

    @POST("admin/projects")
    suspend fun createAdminProject(@Body body: ProjectBody): JsonObject

    @PATCH("admin/projects/{id}/publish")
    suspend fun publishAdminProject(@Path("id") id: String): JsonObject

    @PATCH("projects/{id}/publish")
    suspend fun publishProject(@Path("id") id: String): OwnerProjectDto

    @DELETE("projects/{id}")
    suspend fun deleteProject(@Path("id") id: String)

    @GET("leads/mine")
    suspend fun myLeads(): List<LeadDto>

    @GET("plans")
    suspend fun plans(): List<PlanDto>

    /** Sin suscripción el backend responde 200 con cuerpo vacío: se lee a mano en el repositorio. */
    @GET("plans/me/subscription")
    suspend fun mySubscription(): ResponseBody

    @POST("plans/{id}/request")
    suspend fun requestPlan(@Path("id") id: String, @Body body: PlanRequestBody): SubscriptionDto
}
