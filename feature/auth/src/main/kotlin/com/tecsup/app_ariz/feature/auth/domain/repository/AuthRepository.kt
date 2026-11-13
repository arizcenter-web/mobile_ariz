package com.tecsup.app_ariz.feature.auth.domain.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterCustomerData
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData

interface AuthRepository {
    suspend fun login(email: String, password: String, rememberMe: Boolean): AppResult<User>
    suspend fun loginWithGoogle(firebaseIdToken: String): AppResult<User>
    suspend fun registerCustomer(data: RegisterCustomerData): AppResult<User>
    suspend fun registerProfessional(data: RegisterProfessionalData): AppResult<User>
    suspend fun requestPasswordReset(email: String): AppResult<Unit>
    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>
}
