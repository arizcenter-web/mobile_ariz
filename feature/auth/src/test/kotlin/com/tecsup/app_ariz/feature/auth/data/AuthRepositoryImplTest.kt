package com.tecsup.app_ariz.feature.auth.data

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.session.dto.AuthResponseDto
import com.tecsup.app_ariz.core.session.dto.AuthUserDto
import com.tecsup.app_ariz.feature.auth.data.remote.api.AuthApi
import com.tecsup.app_ariz.feature.auth.data.remote.dto.ForgotPasswordRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.GoogleLoginRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.LoginRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.OkResponseDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.RegisterCustomerRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.RegisterProfessionalRequestDto
import com.tecsup.app_ariz.feature.auth.data.remote.dto.ResetPasswordRequestDto
import com.tecsup.app_ariz.feature.auth.data.repository.AuthRepositoryImpl
import com.tecsup.app_ariz.feature.auth.domain.model.ProfessionalType
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterCustomerData
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AuthRepositoryImplTest {

    private val userDto = AuthUserDto("u1", "Ana", "ana@correo.com", "USER", null)

    private class FakeAuthApi(private val onCall: () -> AuthResponseDto) : AuthApi {
        var lastLogin: LoginRequestDto? = null
        var lastGoogle: GoogleLoginRequestDto? = null
        var lastRegister: RegisterCustomerRequestDto? = null
        var lastProfessional: RegisterProfessionalRequestDto? = null
        var lastReset: ResetPasswordRequestDto? = null

        override suspend fun login(body: LoginRequestDto) = onCall().also { lastLogin = body }
        override suspend fun loginWithGoogle(body: GoogleLoginRequestDto) = onCall().also { lastGoogle = body }
        override suspend fun registerCustomer(body: RegisterCustomerRequestDto) = onCall().also { lastRegister = body }
        override suspend fun registerProfessional(body: RegisterProfessionalRequestDto) =
            onCall().also { lastProfessional = body }

        override suspend fun forgotPassword(body: ForgotPasswordRequestDto) = OkResponseDto(ok = true)
        override suspend fun resetPassword(body: ResetPasswordRequestDto): OkResponseDto {
            onCall()
            lastReset = body
            return OkResponseDto(ok = true)
        }
    }

    private fun repository(api: FakeAuthApi) = AuthRepositoryImpl(api)

    @Test
    fun `login exitoso devuelve el usuario de dominio`() = runTest {
        val api = FakeAuthApi { AuthResponseDto(userDto) }

        val result = repository(api).login("ana@correo.com", "clave1234", rememberMe = true)

        assertTrue(result is AppResult.Success)
        assertEquals(Role.USER, (result as AppResult.Success).data.role)
        assertEquals(LoginRequestDto("ana@correo.com", "clave1234", true), api.lastLogin)
    }

    @Test
    fun `fallo de red se traduce a AppError Network`() = runTest {
        val api = FakeAuthApi { throw IOException("sin conexion") }

        val result = repository(api).login("ana@correo.com", "clave1234", rememberMe = false)

        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Network)
    }

    @Test
    fun `login con Google envia el idToken`() = runTest {
        val api = FakeAuthApi { AuthResponseDto(userDto) }

        repository(api).loginWithGoogle("token-firebase")

        assertEquals(GoogleLoginRequestDto("token-firebase"), api.lastGoogle)
    }

    @Test
    fun `registro de cliente envia acceptTerms en true`() = runTest {
        val api = FakeAuthApi { AuthResponseDto(userDto) }

        repository(api).registerCustomer(
            RegisterCustomerData("Ana", "Pérez", "ana@correo.com", "987654321", "clave1234"),
        )

        assertEquals(true, api.lastRegister?.acceptTerms)
    }

    @Test
    fun `registro profesional mapea tipo y campos de empresa`() = runTest {
        val api = FakeAuthApi { AuthResponseDto(userDto.copy(role = "REAL_ESTATE")) }

        val result = repository(api).registerProfessional(
            RegisterProfessionalData(
                type = ProfessionalType.REAL_ESTATE_AGENCY,
                firstName = null, lastName = null, dni = null,
                businessName = "Inmobiliaria Sol", ruc = "20123456789",
                email = "ventas@sol.pe", phone = "987654321", whatsapp = "987654321",
                city = "Lima", districts = null, address = null, website = null, socialLinks = null,
                password = "clave1234",
            ),
        )

        assertEquals(Role.REAL_ESTATE, (result as AppResult.Success).data.role)
        val sent = api.lastProfessional!!
        assertEquals("REAL_ESTATE_AGENCY", sent.type)
        assertEquals("20123456789", sent.ruc)
        assertEquals(true, sent.acceptTerms)
    }

    @Test
    fun `recuperar y restablecer contraseña devuelven exito`() = runTest {
        val api = FakeAuthApi { AuthResponseDto(userDto) }
        val repository = repository(api)

        assertTrue(repository.requestPasswordReset("ana@correo.com") is AppResult.Success)
        assertTrue(repository.resetPassword("tok", "clave1234") is AppResult.Success)
        assertEquals(ResetPasswordRequestDto("tok", "clave1234"), api.lastReset)
    }
}
