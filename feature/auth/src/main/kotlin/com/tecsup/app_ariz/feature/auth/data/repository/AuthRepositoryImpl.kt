package com.tecsup.app_ariz.feature.auth.data.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.core.session.dto.toDomain
import com.tecsup.app_ariz.feature.auth.data.mapper.toRequest
import com.tecsup.app_ariz.feature.auth.data.remote.api.AuthApi
import com.tecsup.app_ariz.feature.auth.data.remote.dto.ForgotPasswordRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.GoogleLoginRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.LoginRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.ResetPasswordRequestDto
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterCustomerData
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * La cookie de sesión que devuelve el backend la guarda `SessionCookieJar` (core:network);
 * este repositorio solo traduce DTOs y errores. Avisar a `SessionManager` es tarea de los casos de uso.
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
) : AuthRepository {

    override suspend fun login(email: String, password: String, rememberMe: Boolean): AppResult<User> =
        safeApiCall { api.login(LoginRequestDto(email, password, rememberMe)) }
            .map { it.user.toDomain() }

    override suspend fun loginWithGoogle(firebaseIdToken: String): AppResult<User> =
        safeApiCall { api.loginWithGoogle(GoogleLoginRequestDto(firebaseIdToken)) }
            .map { it.user.toDomain() }

    override suspend fun registerCustomer(data: RegisterCustomerData): AppResult<User> =
        safeApiCall { api.registerCustomer(data.toRequest()) }
            .map { it.user.toDomain() }

    override suspend fun registerProfessional(data: RegisterProfessionalData): AppResult<User> =
        safeApiCall { api.registerProfessional(data.toRequest()) }
            .map { it.user.toDomain() }

    override suspend fun requestPasswordReset(email: String): AppResult<Unit> =
        safeApiCall { api.forgotPassword(ForgotPasswordRequestDto(email)) }
            .map { }

    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> =
        safeApiCall { api.resetPassword(ResetPasswordRequestDto(token, newPassword)) }
            .map { }
}
