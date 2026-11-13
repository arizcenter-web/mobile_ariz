package com.tecsup.app_ariz.core.favorites.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
internal data class FavoritePropertyDto(val id: String = "", val slug: String = "", val title: String = "")

@Serializable
internal data class FavoriteDto(
    val id: String,
    val propertyId: String,
    val createdAt: String? = null,
    val property: FavoritePropertyDto = FavoritePropertyDto(),
)

@Serializable
internal data class ToggleRequest(val propertyId: String)

@Serializable
internal data class ToggleResponse(val active: Boolean)

/** Endpoints protegidos de `FavoritesController` (requieren la cookie de sesión). */
internal interface FavoritesApi {

    @GET("favorites")
    suspend fun list(): List<FavoriteDto>

    @POST("favorites/toggle")
    suspend fun toggle(@Body body: ToggleRequest): ToggleResponse
}
