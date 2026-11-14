package com.tecsup.app_ariz.feature.admin

import com.tecsup.app_ariz.feature.admin.data.buildFormBody
import com.tecsup.app_ariz.feature.admin.data.parseRows
import com.tecsup.app_ariz.feature.admin.data.toAdminItem
import com.tecsup.app_ariz.feature.admin.domain.AdminSections
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminFormTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `el plan envia numeros, lista y banderas con su tipo`() {
        val spec = AdminSections.byKey("plans")!!.form!!
        val values = mapOf(
            "name" to "Pro", "price" to "49.9", "currency" to "PEN", "durationDays" to "30", "propertyLimit" to "5",
            "features" to "Destacado\n\n  Soporte  ", "isFree" to "false", "highlight" to "true", "isActive" to "true",
        )
        val (body, invalid) = buildFormBody(spec, values, editing = false)
        assertTrue(invalid.isEmpty())
        assertEquals(49.9, body["price"]!!.jsonPrimitive.content.toDouble(), 0.0)
        assertEquals(30, body["durationDays"]!!.jsonPrimitive.int)
        assertEquals(listOf("Destacado", "Soporte"), body["features"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertTrue(body["highlight"]!!.jsonPrimitive.boolean)
        assertFalse(body["isFree"]!!.jsonPrimitive.boolean)
        assertFalse("projectLimit" in body) // opcional vacío se omite
    }

    @Test
    fun `los obligatorios vacios y los numeros mal escritos quedan marcados`() {
        val spec = AdminSections.byKey("plans")!!.form!!
        val (_, invalid) = buildFormBody(spec, mapOf("name" to "", "price" to "abc", "durationDays" to "30", "propertyLimit" to "1"), editing = false)
        assertEquals(setOf("name", "price"), invalid)
    }

    @Test
    fun `la campana anida los puntos y manda projectIds vacio solo al crear`() {
        val spec = AdminSections.byKey("campaigns")!!.form!!
        val values = mapOf(
            "name" to "Verano", "startDate" to "2026-01-01", "endDate" to "2026-02-01",
            "points.CONTACTED" to "1", "points.VISIT" to "5", "points.OPERATION_COMPLETED" to "20",
        )
        val (created, invalidCreate) = buildFormBody(spec, values, editing = false)
        assertTrue(invalidCreate.isEmpty())
        val points: JsonObject = created["points"]!!.jsonObject
        assertEquals(5, points["VISIT"]!!.jsonPrimitive.int)
        assertEquals(JsonArray(emptyList()), created["projectIds"])

        val (edited, _) = buildFormBody(spec, values, editing = true)
        assertFalse("points" in edited)
        assertFalse("projectIds" in edited)
    }

    @Test
    fun `una fecha con otro formato es invalida`() {
        val spec = AdminSections.byKey("promotions")!!.form!!
        val (_, invalid) = buildFormBody(spec, mapOf("title" to "x", "imageUrl" to "https://i/x.png", "startsAt" to "01/02/2026", "endsAt" to "2026-03-01"), editing = false)
        assertEquals(setOf("startsAt"), invalid)
    }

    @Test
    fun `al editar se precargan los valores y las listas se unen por linea`() {
        val section = AdminSections.byKey("plans")!!
        val item = parseRows(
            """[{"id":"p","name":"Pro","price":"49.90","isActive":true,"features":["A","B"],"durationDays":30}]""", json,
        ).first().toAdminItem(section)!!
        assertEquals("A\nB", item.values["features"])
        assertEquals("true", item.values["isActive"])
        assertEquals("49.90", item.values["price"])
    }

    @Test
    fun `los registros de referidos ofrecen acciones segun su estado`() {
        val section = AdminSections.byKey("referral-records")!!
        val pending = parseRows("""[{"id":"r","name":"Ana","status":"PENDING_VALIDATION","referrer":{"name":"Luis"}}]""", json).first().toAdminItem(section)!!
        assertEquals(listOf("Aprobar", "Rechazar", "Eliminar"), pending.actions.filter { it.isAvailableFor(pending.status) }.map { it.label })
        val visit = parseRows("""[{"id":"r","name":"Ana","status":"VISIT"}]""", json).first().toAdminItem(section)!!
        assertEquals(listOf("Operación concretada", "Eliminar"), visit.actions.filter { it.isAvailableFor(visit.status) }.map { it.label })
    }

    @Test
    fun `los puntos por proyecto precargan y envian el objeto anidado`() {
        val section = AdminSections.byKey("project-points")!!
        val item = parseRows("""[{"id":"p1","name":"Lomas","slug":"lomas","points":{"CONTACTED":5,"VISIT":10,"OPERATION_COMPLETED":50}}]""", json)
            .first().toAdminItem(section)!!
        assertEquals("10", item.values["points.VISIT"])
        val (body, invalid) = buildFormBody(section.form!!, item.values, editing = true)
        assertTrue(invalid.isEmpty())
        assertEquals(50, body["points"]!!.jsonObject["OPERATION_COMPLETED"]!!.jsonPrimitive.int)
    }

    @Test
    fun `los codigos de acceso solo se revocan si estan configurados`() {
        val section = AdminSections.byKey("access-codes")!!
        val on = parseRows("""[{"id":"a","name":"Lomas","configured":true}]""", json).first().toAdminItem(section)!!
        val off = parseRows("""[{"id":"b","name":"Cerro","configured":false}]""", json).first().toAdminItem(section)!!
        // Generar (1, 7 y 30 días) siempre; revocar solo si hay código vigente.
        assertEquals(4, on.actions.count { it.isAvailableFor(on.status) })
        assertEquals(3, off.actions.count { it.isAvailableFor(off.status) })
        assertEquals("code", on.actions.first().resultKey)
    }

    @Test
    fun `los puntos por estado se arman como una sola fila editable`() {
        val section = AdminSections.byKey("status-points")!!
        val item = parseRows("""[{"id":"config","CONTACTED":5,"VISIT":10,"OPERATION_COMPLETED":50}]""", json).first().toAdminItem(section)!!
        val (body, invalid) = buildFormBody(section.form!!, item.values, editing = true)
        assertTrue(invalid.isEmpty())
        assertEquals(10, body["VISIT"]!!.jsonPrimitive.int)
        assertTrue(section.formIdless)
    }

    @Test
    fun `la campana envia los proyectos elegidos y no los pisa con una lista vacia`() {
        val spec = AdminSections.byKey("campaigns")!!.form!!
        val values = mapOf(
            "name" to "Verano", "startDate" to "2026-12-01", "endDate" to "2027-01-01",
            "projectIds" to "p1\np2", "points.CONTACTED" to "1", "points.VISIT" to "2", "points.OPERATION_COMPLETED" to "3",
        )
        val (body, invalid) = buildFormBody(spec, values, editing = false)
        assertTrue(invalid.isEmpty())
        assertEquals(listOf("p1", "p2"), body["projectIds"]!!.jsonArray.map { it.jsonPrimitive.content })
        val (none, _) = buildFormBody(spec, values - "projectIds", editing = false)
        assertEquals(0, none["projectIds"]!!.jsonArray.size)
    }

    @Test
    fun `la oportunidad de inversion arma numeros, galeria y destacados`() {
        val spec = AdminSections.byKey("investments")!!.form!!
        val values = spec.fields.associate { it.key to it.default } + mapOf(
            "name" to "Zorritos", "location" to "Tumbes", "summary" to "s", "areaLabel" to "120 m2", "initialInvestment" to "50000",
            "department" to "Tumbes", "province" to "Contralmirante", "district" to "Zorritos", "description" to "d",
            "coverImageUrl" to "https://i/c.jpg", "galleryImages" to "https://i/1.jpg\nhttps://i/2.jpg", "highlights" to "Playa\nVista",
        )
        val (body, invalid) = buildFormBody(spec, values, editing = false)
        assertTrue(invalid.toString(), invalid.isEmpty())
        assertEquals(2, body["galleryImages"]!!.jsonArray.size)
        assertEquals(50000.0, body["initialInvestment"]!!.jsonPrimitive.content.toDouble(), 0.0)
        assertEquals(-3.5669, body["latitude"]!!.jsonPrimitive.content.toDouble(), 0.0)
        assertFalse("riskLevel" in body && body["riskLevel"]!!.jsonPrimitive.content.isNotEmpty()) // "No mostrar" se omite
        assertFalse("investmentTypeId" in body)
    }

    @Test
    fun `las desarrolladoras ofrecen marca oficial, aviso y baja de cuenta`() {
        val labels = AdminSections.byKey("developers")!!.actions.map { it.label }
        assertEquals(listOf("Marcar como oficial", "Quitar marca oficial", "Enviar aviso", "Eliminar cuenta"), labels)
        assertEquals(listOf("Enviar aviso", "Eliminar cuenta"), AdminSections.byKey("agencies")!!.actions.map { it.label })
    }
}
