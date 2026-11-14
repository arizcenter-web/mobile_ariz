package com.tecsup.app_ariz.feature.admin

import com.tecsup.app_ariz.feature.admin.domain.PlanMode
import com.tecsup.app_ariz.feature.admin.domain.SitePlans
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SitePlanTest {
    private val json = Json

    private fun plan(text: String) = json.parseToJsonElement(text) as JsonObject

    @Test
    fun `editar un lote conserva su dibujo y demas datos`() {
        val base = plan("""{"logoUrl":"","zones":[],"image":{"url":"https://x"},"lots":[
            {"id":"l1","code":"A-1","block":"A","areaM2":120,"price":50000,"status":"AVAILABLE","cashAllowed":true,"financingAllowed":false,
             "path":[{"lat":1,"lng":1},{"lat":1,"lng":2},{"lat":2,"lng":2}]}]}""")
        val lot = SitePlans.lots(base).first()
        val values = SitePlans.valuesOf(lot, SitePlans.LotSpec) + mapOf("status" to "SOLD", "price" to "55000.5", "notes" to "")
        val (edited, invalid) = SitePlans.applyValues(lot, SitePlans.LotSpec, values)
        assertTrue(invalid.isEmpty())
        val next = SitePlans.upsertLot(base, edited)
        val saved = SitePlans.lots(next).single()
        assertEquals("SOLD", saved["status"]!!.jsonPrimitive.content)
        assertEquals(55000.5, saved["price"]!!.jsonPrimitive.content.toDouble(), 0.0)
        assertEquals(3, saved["path"]!!.jsonArray.size) // el polígono no se toca
        assertEquals("https://x", next["image"]!!.jsonObject["url"]!!.jsonPrimitive.content) // ni la imagen del plano
    }

    @Test
    fun `un lote nuevo exige codigo y area y se agrega sin dibujo`() {
        val (_, invalid) = SitePlans.applyValues(SitePlans.newLot(), SitePlans.LotSpec, SitePlans.valuesOf(SitePlans.newLot(), SitePlans.LotSpec))
        assertEquals(setOf("code", "areaM2"), invalid)
        val lot = SitePlans.applyValues(SitePlans.newLot(), SitePlans.LotSpec, SitePlans.valuesOf(SitePlans.newLot(), SitePlans.LotSpec) + mapOf("code" to "B-2", "areaM2" to "90")).first
        val next = SitePlans.upsertLot(SitePlans.ensure(JsonObject(emptyMap()), PlanMode.LOTS), lot)
        assertEquals(1, SitePlans.summary(next).lots)
        assertEquals(0, SitePlans.lots(next).single()["path"]!!.jsonArray.size)
        assertEquals(0, SitePlans.summary(SitePlans.removeLot(next, lot["id"]!!.jsonPrimitive.content)).lots)
    }

    @Test
    fun `un edificio se genera como rectangulo valido alrededor del punto`() {
        var plan = SitePlans.ensure(JsonObject(emptyMap()), PlanMode.APARTMENTS)
        assertEquals("APARTMENTS", plan["mode"]!!.jsonPrimitive.content)
        plan = SitePlans.addBuilding(plan, "Torre A", 20.0, 30.0, -12.05, -77.0)
        val building = SitePlans.buildings(plan).single()
        val path = building["path"]!!.jsonArray.map { it.jsonObject["lat"]!!.jsonPrimitive.content.toDouble() to it.jsonObject["lng"]!!.jsonPrimitive.content.toDouble() }
        assertEquals(4, path.size)
        assertTrue(SitePlans.isSimplePolygon(path))
        // ~30 m de largo = ~0.00027 grados de latitud
        assertEquals(30.0, (path[0].first - path[3].first) * 111_320.0, 0.5)
    }

    @Test
    fun `pisos y departamentos se agregan editan y quitan en su lugar`() {
        var plan = SitePlans.addBuilding(SitePlans.ensure(JsonObject(emptyMap()), PlanMode.APARTMENTS), "Torre A", 20.0, 20.0, -12.0, -77.0)
        val b = SitePlans.buildings(plan).single()["id"]!!.jsonPrimitive.content
        plan = SitePlans.addFloor(plan, b, 1, "Piso 1")
        val f = SitePlans.floors(SitePlans.buildings(plan).single()).single()["id"]!!.jsonPrimitive.content
        plan = SitePlans.setFloorImage(plan, b, f, "https://res.cloudinary.com/x/image/upload/p.jpg", 800, 600)
        val apt = SitePlans.applyValues(
            SitePlans.newApartment(listOf(0.1 to 0.1, 0.9 to 0.1, 0.9 to 0.9)), SitePlans.ApartmentSpec,
            SitePlans.valuesOf(SitePlans.newApartment(emptyList()), SitePlans.ApartmentSpec) + mapOf("code" to "101", "areaM2" to "65.5", "price" to "200000"),
            optionalNumbersWhenBlank = true,
        ).first
        plan = SitePlans.upsertApartment(plan, b, f, apt)
        val summary = SitePlans.summary(plan)
        assertEquals(1, summary.buildings); assertEquals(1, summary.floors); assertEquals(1, summary.apartments); assertEquals(1, summary.apartmentsAvailable)
        val floor = SitePlans.floors(SitePlans.buildings(plan).single()).single()
        assertEquals(800, floor["image"]!!.jsonObject["width"]!!.jsonPrimitive.content.toInt())
        assertFalse("financedPrice" in SitePlans.apartments(floor).single()) // opcional vacío: se omite
        assertEquals(3, SitePlans.apartments(floor).single()["path"]!!.jsonArray.size)
        plan = SitePlans.removeApartment(plan, b, f, apt["id"]!!.jsonPrimitive.content)
        assertEquals(0, SitePlans.summary(plan).apartments)
    }

    @Test
    fun `la validacion de poligonos rechaza cruces, puntos repetidos y areas nulas`() {
        assertTrue(SitePlans.isSimplePolygon(listOf(0.1 to 0.1, 0.9 to 0.1, 0.9 to 0.9, 0.1 to 0.9)))
        assertFalse(SitePlans.isSimplePolygon(listOf(0.1 to 0.1, 0.9 to 0.9, 0.9 to 0.1, 0.1 to 0.9))) // forma de "8"
        assertFalse(SitePlans.isSimplePolygon(listOf(0.1 to 0.1, 0.5 to 0.5, 0.9 to 0.9))) // colineales
        assertFalse(SitePlans.isSimplePolygon(listOf(0.1 to 0.1, 0.9 to 0.1)))
    }

    @Test
    fun `el modo sale del plano o de la categoria del proyecto`() {
        assertEquals(PlanMode.APARTMENTS, SitePlans.mode(JsonObject(emptyMap()), "DEPARTAMENTOS"))
        assertEquals(PlanMode.APARTMENTS, SitePlans.mode(JsonObject(mapOf("mode" to JsonPrimitive("APARTMENTS"))), null))
        assertEquals(PlanMode.LOTS, SitePlans.mode(JsonObject(emptyMap()), "LOTIZACION"))
    }

    @Test
    fun `las filas del Excel se vuelven lotes sin dibujo con las reglas del web`() {
        val rows = listOf(
            mapOf("codigo" to "1", "manzana" to "A", "area_m2" to "120", "precio_contado" to "60000", "estado" to "Vendido", "financiado" to "si", "precio_financiado" to "70000", "inicial" to "10000", "cuotas" to "24", "cuota_mensual" to "2800"),
            mapOf("codigo" to "2", "manzana" to "A", "area_m2" to "100,5", "precio_contado" to "50000"),
        )
        val (lots, problem) = SitePlans.lotsFromExcelRows(rows, emptyList())
        assertEquals(null, problem)
        assertEquals(2, lots.size)
        assertEquals("SOLD", lots[0]["status"]!!.jsonPrimitive.content)
        assertEquals(true, lots[0]["financingAllowed"]!!.jsonPrimitive.content.toBoolean())
        assertEquals(100.5, lots[1]["areaM2"]!!.jsonPrimitive.content.toDouble(), 0.0)
        assertEquals(0, lots[0]["path"]!!.jsonArray.size)
        assertEquals(2, SitePlans.summary(SitePlans.addLots(SitePlans.ensure(JsonObject(emptyMap()), PlanMode.LOTS), lots)).lots)
    }

    @Test
    fun `el Excel con codigos repetidos o cuotas invalidas se rechaza con el mensaje`() {
        val dup = listOf(mapOf("codigo" to "1", "manzana" to "A", "area_m2" to "10"), mapOf("codigo" to "1", "manzana" to "A", "area_m2" to "10"))
        assertEquals("El Excel repite 1 en A.", SitePlans.lotsFromExcelRows(dup, emptyList()).second)
        val existing = SitePlans.lotsFromExcelRows(listOf(mapOf("codigo" to "9", "manzana" to "B", "area_m2" to "10")), emptyList()).first
        assertEquals("El Excel repite 9 en B.", SitePlans.lotsFromExcelRows(listOf(mapOf("codigo" to "9", "manzana" to "B", "area_m2" to "10")), existing).second)
        val bad = listOf(mapOf("codigo" to "3", "area_m2" to "10", "financiado" to "si", "inicial" to "500", "precio_financiado" to "100", "cuotas" to "12"))
        assertEquals("3: revisa inicial y cuotas.", SitePlans.lotsFromExcelRows(bad, emptyList()).second)
    }

    // Rectángulo de ~20 x 30 m cerca de Lima.
    private val lat0 = -12.0
    private val lng0 = -77.0
    private val dLat = 30.0 / 111_320.0
    private val dLng = 20.0 / (111_320.0 * kotlin.math.cos(Math.toRadians(-12.0)))
    private val rect = listOf(lat0 to lng0, lat0 to lng0 + dLng, lat0 + dLat to lng0 + dLng, lat0 + dLat to lng0)

    @Test
    fun `las medidas de un poligono sobre el mapa salen en metros`() {
        assertEquals(600.0, SitePlans.geoAreaM2(rect), 1.0)
        assertEquals(100.0, SitePlans.geoPerimeterM(rect), 0.5)
        assertEquals(20.0, SitePlans.geoFrontM(rect), 0.3)
        assertTrue(SitePlans.isValidGeoPolygon(rect))
        assertFalse(SitePlans.isValidGeoPolygon(listOf(rect[0], rect[2], rect[1], rect[3]))) // cruzado
        assertFalse(SitePlans.isValidGeoPolygon(rect.take(2)))
    }

    @Test
    fun `la imagen del plano se coloca y se mueve conservando el recorte`() {
        val base = plan("""{"image":{"url":"https://a/1.jpg","center":{"lat":-12.0,"lng":-77.0},"widthM":100,"heightM":80,"rotation":0,"opacity":0.7,"crop":{"left":0.1,"top":0.1,"right":0.9,"bottom":0.9}}}""")
        val image = SitePlans.image(base)!!
        assertEquals(100.0, image.widthM, 0.0)
        val moved = SitePlans.withImage(base, image.copy(latitude = -12.5, widthM = 150.0, rotation = 400.0, opacity = 2.0))
        val next = SitePlans.image(moved)!!
        assertEquals(-12.5, next.latitude, 0.0)
        assertEquals(150.0, next.widthM, 0.0)
        assertEquals(360.0, next.rotation, 0.0) // se limita al rango del backend
        assertEquals(1.0, next.opacity, 0.0)
        assertTrue("crop" in moved["image"]!!.jsonObject)
        assertFalse("crop" in SitePlans.replaceImage(base, next.copy(url = "https://a/2.jpg"))["image"]!!.jsonObject) // imagen nueva: sin recorte viejo
        assertEquals(null, SitePlans.image(JsonObject(emptyMap())))
    }

    @Test
    fun `recortar la hoja acumula el recorte, ajusta el tamano y se puede restaurar`() {
        val base = plan("""{"image":{"url":"https://a/1.jpg","center":{"lat":-12.0,"lng":-77.0},"widthM":200,"heightM":100,"rotation":0,"opacity":0.7}}""")
        val first = SitePlans.cropImage(base, 0.0, 0.0, 0.5, 1.0)!!
        val img = SitePlans.image(first)!!
        assertEquals(100.0, img.widthM, 1e-9)
        assertEquals(100.0, img.heightM, 1e-9)
        assertEquals(0.5, img.crop!!.right, 1e-9)
        assertTrue(img.longitude < -77.0) // el centro pasa a la mitad izquierda
        val second = SitePlans.image(SitePlans.cropImage(first, 0.5, 0.0, 1.0, 1.0)!!)!!
        assertEquals(0.25, second.crop!!.left, 1e-9) // se acumula sobre el recorte previo
        assertEquals(0.5, second.crop!!.right, 1e-9)
        val restored = SitePlans.image(SitePlans.restoreCrop(first)!!)!!
        assertEquals(null, restored.crop)
        assertEquals(200.0, restored.widthM, 1e-9)
        assertEquals(-77.0, restored.longitude, 1e-9)
        assertEquals(null, SitePlans.cropImage(base, 0.5, 0.0, 0.5, 1.0)) // área vacía
        assertEquals(null, SitePlans.cropImage(base, 0.0, 0.0, 0.001, 1.0)) // menos de 1 m
        assertEquals(null, SitePlans.restoreCrop(base)) // nada que restaurar
    }

    @Test
    fun `el PDF de origen se guarda con la imagen y se descarta al cambiar de imagen`() {
        val base = plan("""{"image":{"url":"https://a/1.jpg","center":{"lat":-12.0,"lng":-77.0},"widthM":100,"heightM":80,"rotation":0,"opacity":0.7}}""")
        val withPdf = SitePlans.withSourcePdf(base, "https://res.cloudinary.com/x/raw/upload/p.pdf", 3, "plano.pdf")
        val image = withPdf["image"]!!.jsonObject
        assertEquals("https://res.cloudinary.com/x/raw/upload/p.pdf", image["sourcePdfUrl"]!!.jsonPrimitive.content)
        assertEquals("3", image["sourcePdfPage"]!!.jsonPrimitive.content)
        assertTrue("sourcePdfUrl" in SitePlans.withImage(withPdf, SitePlans.image(withPdf)!!.copy(latitude = -13.0))["image"]!!.jsonObject)
        assertFalse("sourcePdfUrl" in SitePlans.replaceImage(withPdf, SitePlans.image(withPdf)!!)["image"]!!.jsonObject)
        val noImage = JsonObject(emptyMap())
        assertEquals(noImage, SitePlans.withSourcePdf(noImage, "u", 1, "n")) // sin imagen no hay nada que anotar
    }

    @Test
    fun `dibujar un lote en el mapa lo asigna a uno importado sin dibujo`() {
        val imported = SitePlans.lotsFromExcelRows(listOf(mapOf("codigo" to "1", "manzana" to "A", "area_m2" to "100"), mapOf("codigo" to "2", "manzana" to "A", "area_m2" to "100")), emptyList()).first
        var plan = SitePlans.addLots(SitePlans.ensure(JsonObject(emptyMap()), PlanMode.LOTS), imported)
        assertEquals(2, SitePlans.lotsWithoutPath(plan).size)
        val target = SitePlans.lotsWithoutPath(plan).first()
        plan = SitePlans.upsertLot(plan, SitePlans.withGeoPath(target, rect))
        assertEquals(1, SitePlans.lotsWithoutPath(plan).size)
        assertEquals(4, SitePlans.geoPath(SitePlans.lots(plan).first { it["id"] == target["id"] }).size)
    }

    @Test
    fun `las zonas se agregan con su tipo y se quitan`() {
        var plan = SitePlans.ensure(JsonObject(emptyMap()), PlanMode.LOTS)
        val zone = SitePlans.newZone(com.tecsup.app_ariz.feature.admin.domain.ZoneKind.PARK, " Parque 1 ", rect)
        plan = SitePlans.upsertZone(plan, zone)
        assertEquals("PARK", SitePlans.zones(plan).single()["kind"]!!.jsonPrimitive.content)
        assertEquals("Parque 1", SitePlans.zones(plan).single()["name"]!!.jsonPrimitive.content)
        assertEquals(1, SitePlans.summary(plan).zones)
        assertEquals(0, SitePlans.zones(SitePlans.removeZone(plan, zone["id"]!!.jsonPrimitive.content)).size)
    }
}
