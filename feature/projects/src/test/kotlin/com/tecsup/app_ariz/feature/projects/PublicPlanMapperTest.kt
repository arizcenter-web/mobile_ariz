package com.tecsup.app_ariz.feature.projects

import com.tecsup.app_ariz.feature.projects.data.mapper.toPublicPlan
import com.tecsup.app_ariz.feature.projects.domain.model.LotStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicPlanMapperTest {
    private fun plan(text: String) = Json.parseToJsonElement(text) as JsonObject

    @Test
    fun `un plano de lotes trae financiamiento, estado y dibujo`() {
        val result = plan(
            """{"currency":"USD","image":{"url":"https://i/p.jpg","center":{"lat":-12.0,"lng":-77.0},"widthM":200,"heightM":150,"rotation":10,"opacity":0.5},
            "zones":[{"kind":"PARK","name":"Parque","path":[{"lat":1,"lng":1}]}],
            "lots":[{"id":"l1","code":"1","block":"A","areaM2":120.5,"price":50000,"status":"SOLD","cashAllowed":false,"financingAllowed":true,
              "financedPrice":60000,"downPayment":6000,"installments":24,"monthlyPayment":2500,"notes":"esquina",
              "path":[{"lat":-12.0,"lng":-77.0},{"lat":-12.0,"lng":-76.99},{"lat":-11.99,"lng":-76.99}]},
             {"id":"l2","code":"2","status":"RARO","path":[]}]}""",
        ).toPublicPlan()
        assertFalse(result.isApartments)
        assertEquals("USD", result.currency.name)
        assertEquals(2, result.lots.size)
        val lot = result.lots.first()
        assertEquals(LotStatus.SOLD, lot.status)
        assertFalse(lot.cashAllowed)
        assertTrue(lot.financingAllowed)
        assertEquals(24, lot.installments)
        assertEquals(3, lot.path.size)
        assertEquals(LotStatus.AVAILABLE, result.lots[1].status) // estado desconocido: disponible
        assertTrue(result.hasMap)
        assertEquals(200.0, result.overlay!!.widthM, 0.0)
        assertEquals(1, result.zones.size)
    }

    @Test
    fun `un plano de departamentos aplana edificios y pisos`() {
        val result = plan(
            """{"mode":"APARTMENTS","buildings":[{"id":"b","name":"Torre A","floors":[
              {"id":"f1","name":"Piso 1","apartments":[{"id":"a1","code":"101","areaM2":65,"price":200000,"operation":"SALE","status":"AVAILABLE","bedrooms":2,"bathrooms":1}]},
              {"id":"f2","name":"Piso 2","apartments":[{"id":"a2","code":"201","areaM2":70,"price":220000,"operation":"RENT","status":"RENTED"}]}]}]}""",
        ).toPublicPlan()
        assertTrue(result.isApartments)
        assertEquals(listOf("101", "201"), result.apartments.map { it.code })
        assertEquals("Torre A", result.apartments[0].building)
        assertEquals("Piso 2", result.apartments[1].floor)
        assertEquals("RENTED", result.apartments[1].status)
        assertFalse(result.hasMap)
    }
}
