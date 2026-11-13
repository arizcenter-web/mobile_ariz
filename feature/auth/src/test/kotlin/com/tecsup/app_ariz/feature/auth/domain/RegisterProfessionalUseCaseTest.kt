package com.tecsup.app_ariz.feature.auth.domain

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.model.ProfessionalType
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData
import com.tecsup.app_ariz.feature.auth.domain.usecase.RegisterProfessionalUseCase
import com.tecsup.app_ariz.feature.auth.fakes.FakeAuthRepository
import com.tecsup.app_ariz.feature.auth.fakes.FakeSessionManager
import com.tecsup.app_ariz.feature.auth.fakes.testUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterProfessionalUseCaseTest {

    private val repository = FakeAuthRepository()
    private val session = FakeSessionManager()
    private val useCase = RegisterProfessionalUseCase(repository, session)

    private val data = RegisterProfessionalData(
        type = ProfessionalType.INDEPENDENT_AGENT,
        firstName = "  Ana ", lastName = "Pérez", dni = "12345678",
        businessName = null, ruc = null,
        email = " ana@correo.com ", phone = "987654321", whatsapp = "987654321",
        city = " Lima ", districts = "   ", address = "", website = null, socialLinks = null,
        password = "clave1234",
    )

    @Test
    fun `recorta espacios y convierte textos vacios en nulos`() = runTest {
        useCase(data)

        val sent = repository.lastProfessional!!
        assertEquals("Ana", sent.firstName)
        assertEquals("ana@correo.com", sent.email)
        assertEquals("Lima", sent.city)
        assertNull(sent.districts)
        assertNull(sent.address)
    }

    @Test
    fun `un registro exitoso abre la sesion`() = runTest {
        useCase(data)

        assertEquals(testUser, session.signedInUser)
    }

    @Test
    fun `un registro fallido no abre la sesion`() = runTest {
        repository.result = AppResult.Failure(AppError.Validation("Ya existe una cuenta con este correo"))

        useCase(data)

        assertNull(session.signedInUser)
    }
}
