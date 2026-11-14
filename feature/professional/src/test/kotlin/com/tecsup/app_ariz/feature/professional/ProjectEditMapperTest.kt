package com.tecsup.app_ariz.feature.professional

import com.tecsup.app_ariz.feature.professional.data.notEditableReason
import com.tecsup.app_ariz.feature.professional.data.toProjectDraft
import com.tecsup.app_ariz.feature.professional.data.toUpdateBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ProjectEditMapperTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private val detail = json.parseToJsonElement(
        """{"id":"p1","category":"LOTIZACION","lotMode":"INDIVIDUAL","hasSitePlan":false,"name":"Lomas","description":"Desc",
        "stage":"IN_CONSTRUCTION","currency":"USD","priceFrom":1000.5,"priceTo":2000,"estimatedDelivery":"2027-05-01T00:00:00.000Z",
        "financing":["cash","bank"],"amenities":["Parque"],"tourUrl":"https://v/1","ruc":null,
        "location":{"department":"Lima","province":"Lima","district":"Ate","latitude":-12.05,"longitude":-77.0},
        "lots":[{"lotNumber":"1","block":"A","areaM2":120,"price":50000,"status":"SOLD","frontM":6,"planLocation":"x"}],
        "milestones":[{"title":"Inicio","date":"2026-01-01T00:00:00.000Z","note":"ok"}],
        "media":[{"type":"IMAGE","url":"https://i/1.jpg","position":1,"isCover":false},{"type":"IMAGE","url":"https://i/0.jpg","position":0,"isCover":true},{"type":"VIDEO","url":"https://v/1.mp4","position":2}],
        "agent":{"name":"Ana","email":"a@x.com","phone":"999","whatsapp":"51999"}}""",
    ) as JsonObject

    @Test
    fun `un proyecto de lotizacion sin plano se puede editar`() {
        assertNull(detail.notEditableReason())
        // Departamentos o con plano: se edita la información, los lotes los define el plano.
        val apts = JsonObject(detail + ("category" to kotlinx.serialization.json.JsonPrimitive("DEPARTAMENTOS")))
        assertNull(apts.notEditableReason())
        assertEquals(true, apts.toProjectDraft().managedByPlan)
        assertEquals("DEPARTAMENTOS", apts.toProjectDraft().category)
        assertEquals(emptyList<Any>(), apts.toProjectDraft().toUpdateBody(json)["lots"]!!.jsonArray.toList())
        assertEquals(true, JsonObject(detail + ("hasSitePlan" to kotlinx.serialization.json.JsonPrimitive(true))).toProjectDraft().managedByPlan)
        assertNotNull(JsonObject(detail + ("lotMode" to kotlinx.serialization.json.JsonPrimitive("BULK"))).notEditableReason())
    }

    @Test
    fun `el borrador conserva estado de lotes, portada primero y numeros limpios`() {
        val draft = detail.toProjectDraft()
        assertEquals("2027-05-01", draft.estimatedDelivery)
        assertEquals("1000.5", draft.priceFrom)
        assertEquals("2000", draft.priceTo)
        assertEquals(listOf("https://i/0.jpg", "https://i/1.jpg"), draft.photos)
        assertEquals("SOLD", draft.lots.first().status)
        assertEquals("120", draft.lots.first().area)
    }

    @Test
    fun `al guardar se reenvian los datos que la app no edita`() {
        val body = detail.toProjectDraft().toUpdateBody(json)
        assertEquals(listOf("cash", "bank"), body["financing"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("https://v/1", body["tourUrl"]!!.jsonPrimitive.content)
        assertEquals("Inicio", body["milestones"]!!.jsonArray.first().jsonObject["title"]!!.jsonPrimitive.content)
        val media = body["media"]!!.jsonArray.map { it.jsonObject }
        assertEquals(listOf("IMAGE", "IMAGE", "VIDEO"), media.map { it["type"]!!.jsonPrimitive.content })
        assertEquals("SOLD", body["lots"]!!.jsonArray.first().jsonObject["status"]!!.jsonPrimitive.content)
        assertEquals("6.0", body["lots"]!!.jsonArray.first().jsonObject["frontM"]!!.jsonPrimitive.content)
    }
}
