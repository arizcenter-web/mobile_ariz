package com.tecsup.app_ariz.core.session

import com.tecsup.app_ariz.core.model.ApprovalStatus
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.session.dto.AuthUserDto
import com.tecsup.app_ariz.core.session.dto.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthUserMapperTest {

    private val dto = AuthUserDto(
        id = "u1",
        name = "Ana Pérez",
        email = "ana@correo.com",
        role = "REAL_ESTATE",
        professionalStatus = "APPROVED",
    )

    @Test
    fun `mapea rol y estado profesional conocidos`() {
        val user = dto.toDomain()

        assertEquals("u1", user.id)
        assertEquals("Ana Pérez", user.name)
        assertEquals(Role.REAL_ESTATE, user.role)
        assertEquals(ApprovalStatus.APPROVED, user.professionalStatus)
    }

    @Test
    fun `rol desconocido cae al de menor privilegio`() {
        assertEquals(Role.USER, dto.copy(role = "SUPERADMIN").toDomain().role)
    }

    @Test
    fun `usuario sin perfil profesional tiene estado nulo`() {
        assertNull(dto.copy(professionalStatus = null).toDomain().professionalStatus)
    }
}
