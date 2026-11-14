package com.tecsup.app_ariz.feature.professional

import com.tecsup.app_ariz.feature.professional.domain.LotDraft
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalType
import com.tecsup.app_ariz.feature.professional.domain.ProjectDraft
import com.tecsup.app_ariz.feature.professional.domain.ProjectField
import com.tecsup.app_ariz.feature.professional.domain.ProjectFieldError
import com.tecsup.app_ariz.feature.professional.domain.ProjectValidator
import com.tecsup.app_ariz.feature.professional.domain.canManageProjects
import com.tecsup.app_ariz.feature.professional.domain.isAgency
import com.tecsup.app_ariz.feature.professional.data.toBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectDraftTest {
    private fun valid() = ProjectDraft(
        name = "Residencial Sol", description = "Lotes con servicios", priceFrom = "50000", priceTo = "90000",
        department = "Tumbes", province = "Tumbes", district = "Tumbes", estimatedDelivery = "2027-06-01",
        latitude = "-3.57", longitude = "-80.45", photos = listOf("https://img/1.jpg"),
        lots = listOf(LotDraft("1", "A", "120", "55000"), LotDraft("2", "A", "130", "60000")),
    )

    @Test
    fun `un proyecto completo es valido`() {
        assertTrue(ProjectValidator.validate(valid(), requireCoordinates = true).isEmpty())
    }

    @Test
    fun `publicar exige coordenadas pero guardar borrador no`() {
        val noCoords = valid().copy(latitude = "", longitude = "")
        assertTrue(ProjectValidator.validate(noCoords, requireCoordinates = false).isEmpty())
        val errors = ProjectValidator.validate(noCoords, requireCoordinates = true)
        assertEquals(ProjectFieldError.REQUIRED, errors[ProjectField.LATITUDE])
        assertEquals(ProjectFieldError.REQUIRED, errors[ProjectField.LONGITUDE])
    }

    @Test
    fun `precio hasta menor que desde y fecha mal escrita se rechazan`() {
        val errors = ProjectValidator.validate(valid().copy(priceTo = "100", estimatedDelivery = "01/06/2027"), requireCoordinates = false)
        assertEquals(ProjectFieldError.PRICE_RANGE, errors[ProjectField.PRICE_TO])
        assertEquals(ProjectFieldError.INVALID_DATE, errors[ProjectField.DELIVERY])
    }

    @Test
    fun `los lotes necesitan datos validos y numeros unicos`() {
        val none = ProjectValidator.validate(valid().copy(lots = listOf(LotDraft())), false)
        assertEquals(ProjectFieldError.NEEDS_LOT, none[ProjectField.LOTS])
        val invalid = ProjectValidator.validate(valid().copy(lots = listOf(LotDraft("1", "", "0", "5"))), false)
        assertEquals(ProjectFieldError.LOT_INVALID, invalid[ProjectField.LOTS])
        val dup = ProjectValidator.validate(valid().copy(lots = listOf(LotDraft("1", "", "10", "5"), LotDraft(" 1 ", "B", "10", "5"))), false)
        assertEquals(ProjectFieldError.LOT_DUPLICATE, dup[ProjectField.LOTS])
    }

    @Test
    fun `la latitud fuera de rango es invalida`() {
        val errors = ProjectValidator.validate(valid().copy(latitude = "95"), requireCoordinates = true)
        assertEquals(ProjectFieldError.INVALID_NUMBER, errors[ProjectField.LATITUDE])
    }

    @Test
    fun `el cuerpo manda lotizacion, lotes individuales y la primera foto de portada`() {
        val body = valid().copy(lots = valid().lots + LotDraft()).toBody()
        assertEquals("LOTIZACION", body.category)
        assertEquals("INDIVIDUAL", body.lotMode)
        assertEquals(2, body.lots.size) // el lote vacío se descarta
        assertEquals("AVAILABLE", body.lots.first().status)
        assertTrue(body.media.single().isCover)
        assertEquals("IMAGE", body.media.single().type)
    }

    @Test
    fun `solo inmobiliarias y desarrolladoras gestionan proyectos y solo la inmobiliaria coordina con ARIZ`() {
        assertFalse(ProfessionalType.INDEPENDENT_AGENT.canManageProjects)
        assertTrue(ProfessionalType.REAL_ESTATE_AGENCY.canManageProjects)
        assertTrue(ProfessionalType.DEVELOPER.canManageProjects)
        assertTrue(ProfessionalType.REAL_ESTATE_AGENCY.isAgency)
        assertFalse(ProfessionalType.DEVELOPER.isAgency)
    }
}
