package com.tecsup.app_ariz.core.session

import com.tecsup.app_ariz.core.session.dto.AuthResponseDto
import retrofit2.http.GET
import retrofit2.http.POST

/** Endpoints protegidos de sesión de `AuthController`. */
interface SessionApi {

    @GET("auth/me")
    suspend fun me(): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout()
}
