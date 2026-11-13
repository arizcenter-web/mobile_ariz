package com.tecsup.app_ariz.feature.auth.data.remote.api

import com.tecsup.app_ariz.core.session.dto.AuthResponseDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.ForgotPasswordRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.GoogleLoginRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.LoginRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.OkResponseDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.RegisterCustomerRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.RegisterProfessionalRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.ResetPasswordRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Endpoints públicos de `AuthController` (backend/src/modules/auth/auth.controller.ts). */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body body: GoogleLoginRequestDto): AuthResponseDto

    @POST("auth/register/customer")
    suspend fun registerCustomer(@Body body: RegisterCustomerRequestDto): AuthResponseDto

    @POST("auth/register/professional")
    suspend fun registerProfessional(@Body body: RegisterProfessionalRequestDto): AuthResponseDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto): OkResponseDto

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto): OkResponseDto
}
