package com.tecsup.app_ariz.feature.admin

import com.tecsup.app_ariz.feature.admin.data.parseRows
import com.tecsup.app_ariz.feature.admin.data.toAdminItem
import com.tecsup.app_ariz.feature.admin.data.toJson
import com.tecsup.app_ariz.feature.admin.domain.ActionBody
import com.tecsup.app_ariz.feature.admin.domain.AdminSections
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseRows acepta lista directa y objeto con items`() {
        assertEquals(2, parseRows("""[{"id":"1"},{"id":"2"}]""", json).size)
        assertEquals(1, parseRows("""{"items":[{"id":"1"}],"total":1}""", json).size)
        assertEquals(0, parseRows("", json).size)
    }

    @Test
    fun `profesional usa nombre comercial y cae al nombre del usuario`() {
        val section = AdminSections.byKey("professionals")!!
        val withBusiness = parseRows(
            """[{"id":"a","businessName":"Casas SAC","status":"PENDING","user":{"firstName":"Ana","lastName":"Paz","email":"a@a.com"}}]""", json,
        ).first().toAdminItem(section)!!
        assertEquals("Casas SAC", withBusiness.title)
        assertEquals("a@a.com", withBusiness.subtitle)
        assertEquals("PENDING", withBusiness.status)

        val withoutBusiness = parseRows(
            """[{"id":"b","businessName":null,"status":"APPROVED","user":{"firstName":"Ana","lastName":"Paz"}}]""", json,
        ).first().toAdminItem(section)!!
        assertEquals("Ana Paz", withoutBusiness.title)
    }

    @Test
    fun `los booleanos se normalizan a ACTIVE e INACTIVE`() {
        val section = AdminSections.byKey("plans")!!
        val active = parseRows("""[{"id":"p","name":"Pro","isActive":true,"price":"49.90","currency":"PEN"}]""", json).first().toAdminItem(section)!!
        assertEquals("ACTIVE", active.status)
        assertEquals("49.90 PEN", active.subtitle)
        // Solo se ofrece "Desactivar" a un plan activo.
        assertEquals(listOf("Desactivar"), active.actions.filter { it.isAvailableFor(active.status) }.map { it.label })
    }

    @Test
    fun `las fechas ISO se acortan y los hijos se agrupan`() {
        val section = AdminSections.byKey("agencies")!!
        val item = parseRows(
            """[{"id":"g","businessName":"Inmo","user":{"email":"i@i.com"},"properties":[{"id":"p1","title":"Casa","status":"PENDING_REVIEW"}],"projects":[]}]""", json,
        ).first().toAdminItem(section)!!
        assertEquals(1, item.groups.size)
        assertEquals("Propiedades", item.groups.first().label)
        val child = item.groups.first().items.first()
        assertEquals("Casa", child.title)
        assertEquals(listOf("Publicar", "Rechazar"), child.actions.filter { it.isAvailableFor(child.status) }.map { it.label })

        val claims = AdminSections.byKey("claims")!!
        val claim = parseRows("""[{"id":"c","claimCode":"R-1","fullName":"Luis","createdAt":"2026-03-05T10:20:30.000Z"}]""", json).first().toAdminItem(claims)!!
        assertTrue(claim.details.any { it.label == "Fecha" && it.value == "2026-03-05" })
    }

    @Test
    fun `solo se acepta como enlace una URL https`() {
        val section = AdminSections.byKey("subscriptions")!!
        val ok = parseRows("""[{"id":"s","status":"PENDING_PAYMENT","receiptUrl":"https://x/y.jpg","plan":{"name":"Pro"}}]""", json).first().toAdminItem(section)!!
        assertEquals("https://x/y.jpg", ok.link)
        val bad = parseRows("""[{"id":"s","status":"PENDING_PAYMENT","receiptUrl":"javascript:alert(1)"}]""", json).first().toAdminItem(section)!!
        assertNull(bad.link)
    }

    @Test
    fun `un registro sin id se descarta`() {
        assertNull(parseRows("""[{"name":"x"}]""", json).first().toAdminItem(AdminSections.byKey("plans")!!))
    }

    @Test
    fun `el cuerpo de la accion incluye el motivo bajo su clave`() {
        val body = ActionBody(strings = mapOf("status" to "REJECTED")).toJson("rejectionReason", " Foto borrosa ")
        assertEquals("REJECTED", body["status"].toString().trim('"'))
        assertEquals("Foto borrosa", body["rejectionReason"].toString().trim('"'))
        assertNull(ActionBody().toJson("reason", "  ")["reason"])
    }

    @Test
    fun `el catalogo tiene claves unicas y rutas relativas`() {
        val keys = AdminSections.All.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        AdminSections.All.forEach { section ->
            assertTrue(section.key, !section.path.startsWith("/"))
            section.actions.forEach { assertTrue(it.path, !it.path.startsWith("/")) }
        }
        assertNotNull(AdminSections.byKey("redemptions"))
    }

    @Test
    fun `ver detalle muestra todos los campos aplanados y sin ids ni hashes`() {
        val section = AdminSections.byKey("claims")!!
        val item = parseRows(
            """[{"id":"c1","claimCode":"R-1","fullName":"Ana","createdAt":"2026-05-01T10:00:00Z","agent":{"id":"a","businessName":"Lomas SAC"},"tags":["x","y"],"passwordHash":"zzz"}]""",
            json,
        ).first().toAdminItem(section)!!
        val byLabel = item.fields.associate { it.label to it.value }
        assertEquals("R-1", byLabel["Código de reclamo"])
        assertEquals("2026-05-01", byLabel["Creado"])
        assertEquals("Lomas SAC", byLabel["Agent · Razón social"])
        assertEquals("x, y", byLabel["Tags"])
        assertTrue(item.fields.none { it.value == "zzz" || it.value == "c1" })
    }

    @Test
    fun `el detalle del referidor trae sus referidos con acciones y los puntos por campana`() {
        val section = AdminSections.byKey("referrals")!!
        val body = """{"id":"r1","name":"Ana","totalPoints":30,
            "referrals":[{"id":"x1","name":"Luis","status":"CONTACTED","points":10},{"id":"x2","name":"Eva","status":"VISIT","points":20}],
            "campaignPoints":[{"campaign":{"id":"c","name":"Verano"},"totalPoints":15,"referrals":[]}]}"""
        val item = (json.parseToJsonElement(body) as kotlinx.serialization.json.JsonObject).toAdminItem(section, section.detailChildren)!!
        val referrals = item.groups.first { it.label == "Referidos" }.items
        assertEquals(listOf("Marcar visita"), referrals[0].actions.filter { it.isAvailableFor(referrals[0].status) }.map { it.label })
        assertEquals(listOf("Operación concretada"), referrals[1].actions.filter { it.isAvailableFor(referrals[1].status) }.map { it.label })
        assertEquals("Puntos: 10", referrals[0].details.first().let { it.label + ": " + it.value })
        assertEquals("Verano", item.groups.first { it.label == "Puntos por campaña" }.items.first().title)
    }
}
