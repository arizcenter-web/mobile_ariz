package com.tecsup.app_ariz.feature.info

import com.tecsup.app_ariz.feature.info.data.toBody
import com.tecsup.app_ariz.feature.info.domain.ClaimDraft
import com.tecsup.app_ariz.feature.info.domain.ClaimError
import com.tecsup.app_ariz.feature.info.domain.ClaimField
import com.tecsup.app_ariz.feature.info.domain.ClaimKind
import com.tecsup.app_ariz.feature.info.domain.ClaimValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaimTest {
    private val valid = ClaimDraft(
        fullName = "Ana Pérez", address = "Jr. Lima 123", dniOrCe = "12345678", email = "ana@correo.com",
        claimedAmount = "150,50", assetDescription = "Servicio de corretaje", claimDetails = "El servicio no se prestó como se acordó.",
        consumerRequest = "Solicito la devolución del monto pagado.",
    )

    @Test
    fun `un reclamo completo no tiene errores`() {
        assertTrue(ClaimValidator.validate(valid).isEmpty())
    }

    @Test
    fun `el formulario vacio marca todos los obligatorios`() {
        val errors = ClaimValidator.validate(ClaimDraft())
        assertEquals(ClaimField.entries.toSet(), errors.keys)
        assertTrue(errors.values.all { it == ClaimError.REQUIRED })
    }

    @Test
    fun `aplica los minimos y limites del web`() {
        val errors = ClaimValidator.validate(valid.copy(fullName = "Al", dniOrCe = "1234", email = "ana@", claimedAmount = "-5", claimDetails = "corto", assetDescription = "corto"))
        assertEquals(ClaimError.TOO_SHORT_3, errors[ClaimField.FULL_NAME])
        assertEquals(ClaimError.DNI_LENGTH, errors[ClaimField.DNI])
        assertEquals(ClaimError.INVALID_EMAIL, errors[ClaimField.EMAIL])
        assertEquals(ClaimError.INVALID_AMOUNT, errors[ClaimField.AMOUNT])
        assertEquals(ClaimError.TOO_SHORT_20, errors[ClaimField.DETAILS])
        assertEquals(ClaimError.TOO_SHORT_10, errors[ClaimField.ASSET_DESCRIPTION])
        assertEquals(ClaimError.DNI_LENGTH, ClaimValidator.validate(valid.copy(dniOrCe = "1234567890123"))[ClaimField.DNI])
    }

    @Test
    fun `el cuerpo usa los tipos del backend y omite lo vacio`() {
        val body = valid.copy(kind = ClaimKind.COMPLAINT).toBody()
        assertEquals("Complaint", body.type)
        assertEquals(150.5, body.claimedAmount, 0.0)
        assertNull(body.phone)
        assertNull(body.parentGuardian)
        assertEquals("Claim", valid.toBody().type)
    }
}
