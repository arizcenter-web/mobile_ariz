package com.tecsup.app_ariz.feature.admin.domain

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos

enum class PlanMode { LOTS, APARTMENTS }

/** Plano de un proyecto tal como lo entrega `GET admin/projects/{id}/site-plan`. */
data class SitePlanDoc(
    val projectId: String,
    val projectName: String,
    val category: String?,
    val projectStatus: String?,
    val currency: String,
    val latitude: Double?,
    val longitude: Double?,
    val revision: Int,
    val publishedAt: String?,
    /** El plano completo (con toda su geometría): solo se tocan los campos que la app edita. */
    val plan: JsonObject,
)

/** Imagen del plano colocada sobre el mapa (centro, tamaño real en metros, giro y opacidad). */
data class PlanImage(
    val url: String,
    val latitude: Double,
    val longitude: Double,
    val widthM: Double,
    val heightM: Double,
    val rotation: Double,
    val opacity: Double,
    /** Parte visible de la hoja (0..1); si falta se ve completa. Es solo de lectura: se cambia con [SitePlans.cropImage]. */
    val crop: PlanCrop? = null,
)

/** Recorte de la hoja del plano, en fracciones (0..1) de la imagen original. */
data class PlanCrop(val left: Double, val top: Double, val right: Double, val bottom: Double)

enum class ZoneKind(val label: String) { BLOCK("Manzana"), STREET("Calle"), PARK("Parque"), CONTOUR("Contorno") }

/** Página de PDF ya convertida: `uri` de la imagen y cuántas páginas tiene el documento. */
data class RenderedPage(val uri: String, val pageCount: Int)

data class SizedImage(val url: String, val width: Int, val height: Int)

enum class SavePlanOutcome { SAVED, PUBLISHED }

data class SitePlanSaved(val revision: Int, val publishedAt: String?)

data class PlanSummary(
    val lots: Int,
    val lotsAvailable: Int,
    val zones: Int,
    val buildings: Int,
    val floors: Int,
    val apartments: Int,
    val apartmentsAvailable: Int,
)

/**
 * Operaciones sobre el JSON del plano. Se edita sobre el JSON original y no sobre un modelo propio para no perder
 * nada que la app no conoce (polígonos de lotes y zonas, imagen del plano, recortes, etc.): el backend reemplaza el
 * plano completo al guardar.
 */
object SitePlans {
    // ---------- especificaciones de los formularios ----------

    val LotSpec = AdminFormSpec(
        title = "Lote",
        fields = listOf(
            AdminField("code", "Código del lote", required = true),
            AdminField("block", "Manzana"),
            AdminField("street", "Calle"),
            AdminField("address", "Dirección"),
            AdminField("type", "Tipo"),
            AdminField("dimensions", "Dimensiones (ej. 8 x 15)"),
            AdminField("areaM2", "Área (m²)", FieldKind.NUMBER, required = true),
            AdminField("perimeterM", "Perímetro (m)", FieldKind.NUMBER, default = "0"),
            AdminField("frontM", "Frente (m)", FieldKind.NUMBER, default = "0"),
            AdminField("price", "Precio al contado", FieldKind.NUMBER, default = "0"),
            AdminField("priceM2", "Precio por m²", FieldKind.NUMBER, default = "0"),
            AdminField("status", "Estado", FieldKind.CHOICE, options = listOf("AVAILABLE", "RESERVED", "SOLD", "BLOCKED"), optionLabels = LotStatusLabels, default = "AVAILABLE"),
            AdminField("cashAllowed", "Acepta contado", FieldKind.BOOL, default = "true"),
            AdminField("financingAllowed", "Acepta financiamiento", FieldKind.BOOL),
            AdminField("financedPrice", "Precio financiado", FieldKind.NUMBER, default = "0"),
            AdminField("downPayment", "Inicial", FieldKind.NUMBER, default = "0"),
            AdminField("installments", "Cuotas", FieldKind.INT, default = "0"),
            AdminField("monthlyPayment", "Cuota mensual", FieldKind.NUMBER, default = "0"),
            AdminField("notes", "Notas", FieldKind.MULTILINE),
        ),
    )

    val ApartmentSpec = AdminFormSpec(
        title = "Departamento",
        fields = listOf(
            AdminField("code", "Número del departamento", required = true),
            AdminField("areaM2", "Área (m²)", FieldKind.NUMBER, required = true),
            AdminField("operation", "Operación", FieldKind.CHOICE, options = listOf("SALE", "RENT"), optionLabels = mapOf("SALE" to "Venta", "RENT" to "Alquiler"), default = "SALE"),
            AdminField("status", "Estado", FieldKind.CHOICE, options = listOf("AVAILABLE", "RESERVED", "SOLD", "RENTED", "BLOCKED"), optionLabels = ApartmentStatusLabels, default = "AVAILABLE"),
            AdminField("price", "Precio", FieldKind.NUMBER, default = "0"),
            AdminField("maintenanceFee", "Mantenimiento mensual", FieldKind.NUMBER, default = "0"),
            AdminField("bedrooms", "Dormitorios", FieldKind.INT, default = "0"),
            AdminField("bathrooms", "Baños", FieldKind.INT, default = "0"),
            AdminField("notes", "Notas", FieldKind.MULTILINE),
            AdminField("financingAllowed", "Acepta financiamiento", FieldKind.BOOL),
            AdminField("financedPrice", "Precio al crédito", FieldKind.NUMBER),
            AdminField("downPayment", "Inicial", FieldKind.NUMBER),
            AdminField("installments", "Cuotas", FieldKind.INT),
            AdminField("interestRateMonthly", "Interés mensual (%)", FieldKind.NUMBER),
        ),
    )

    val FloorSpec = AdminFormSpec(
        title = "Piso",
        fields = listOf(
            AdminField("number", "Número de piso", FieldKind.INT, required = true),
            AdminField("name", "Nombre del piso", required = true),
        ),
    )

    // ---------- lectura ----------

    fun mode(plan: JsonObject, category: String?): PlanMode =
        if (plan.str("mode") == "APARTMENTS" || category == "DEPARTAMENTOS") PlanMode.APARTMENTS else PlanMode.LOTS

    fun lots(plan: JsonObject): List<JsonObject> = plan.objects("lots")

    fun buildings(plan: JsonObject): List<JsonObject> = plan.objects("buildings")

    fun floors(building: JsonObject): List<JsonObject> = building.objects("floors")

    fun apartments(floor: JsonObject): List<JsonObject> = floor.objects("apartments")

    fun summary(plan: JsonObject): PlanSummary {
        val lots = lots(plan)
        val floors = buildings(plan).flatMap { floors(it) }
        val apartments = floors.flatMap { apartments(it) }
        return PlanSummary(
            lots = lots.size,
            lotsAvailable = lots.count { it.str("status") == "AVAILABLE" },
            zones = plan.objects("zones").size,
            buildings = buildings(plan).size,
            floors = floors.size,
            apartments = apartments.size,
            apartmentsAvailable = apartments.count { it.str("status") == "AVAILABLE" },
        )
    }

    /** Valores de un registro para precargar su formulario (booleanos como `true`/`false`, listas ignoradas). */
    fun valuesOf(obj: JsonObject, spec: AdminFormSpec): Map<String, String> = buildMap {
        spec.fields.forEach { field ->
            val element = obj[field.key]
            if (element is JsonPrimitive && element !is JsonNull) put(field.key, numberText(element))
            else put(field.key, field.default)
        }
    }

    // ---------- edición de campos ----------

    /**
     * Aplica los valores del formulario sobre [base] conservando todo lo demás (por ejemplo, el `path`). Los textos
     * vacíos se guardan como cadena vacía; los números vacíos, como 0 (el backend los exige) salvo los opcionales
     * del departamento, que se quitan. Devuelve el objeto y las claves inválidas.
     */
    fun applyValues(base: JsonObject, spec: AdminFormSpec, values: Map<String, String>, optionalNumbersWhenBlank: Boolean = false): Pair<JsonObject, Set<String>> {
        val invalid = mutableSetOf<String>()
        val result = base.toMutableMap()
        for (field in spec.fields) {
            val raw = values[field.key].orEmpty().trim()
            when (field.kind) {
                FieldKind.BOOL -> result[field.key] = JsonPrimitive(raw == "true")
                FieldKind.NUMBER, FieldKind.INT -> {
                    if (raw.isEmpty()) {
                        if (field.required) invalid += field.key
                        else if (optionalNumbersWhenBlank && field.default.isEmpty()) result.remove(field.key)
                        else result[field.key] = JsonPrimitive(0)
                    } else {
                        val element = if (field.kind == FieldKind.INT) raw.toIntOrNull()?.let { JsonPrimitive(it) } else raw.toDoubleOrNull()?.let { JsonPrimitive(it) }
                        // Un número obligatorio (área) debe ser positivo: el backend rechaza 0.
                        val positive = field.kind == FieldKind.INT || !field.required || (raw.toDoubleOrNull() ?: 0.0) > 0.0
                        if (element == null || !positive) invalid += field.key else result[field.key] = element
                    }
                }
                else -> if (raw.isEmpty() && field.required) invalid += field.key else result[field.key] = JsonPrimitive(raw)
            }
        }
        return JsonObject(result) to invalid
    }

    // ---------- lotes ----------

    /** Lote nuevo sin dibujo en el mapa (`path` vacío): cuenta en el inventario pero no se pinta en el plano. */
    fun newLot(): JsonObject = buildJsonObject {
        put("id", UUID.randomUUID().toString())
        put("code", "")
        put("block", "")
        put("street", "")
        put("address", "")
        put("type", "")
        put("dimensions", "")
        put("areaM2", 0.0)
        put("perimeterM", 0)
        put("frontM", 0)
        put("price", 0)
        put("priceM2", 0)
        put("status", "AVAILABLE")
        put("cashAllowed", true)
        put("financingAllowed", false)
        put("financedPrice", 0)
        put("downPayment", 0)
        put("installments", 0)
        put("monthlyPayment", 0)
        put("notes", "")
        put("path", JsonArray(emptyList()))
    }

    fun upsertLot(plan: JsonObject, lot: JsonObject): JsonObject = upsert(plan, "lots", lot)

    fun removeLot(plan: JsonObject, id: String): JsonObject = remove(plan, "lots", id)

    /**
     * Convierte las filas que devuelve el backend al leer el Excel (columnas de la plantilla) en lotes sin dibujo,
     * con las mismas reglas del web. Devuelve los lotes o el mensaje del primer problema.
     */
    fun lotsFromExcelRows(rows: List<Map<String, String>>, existing: List<JsonObject>): Pair<List<JsonObject>, String?> {
        val status = mapOf(
            "disponible" to "AVAILABLE", "reservado" to "RESERVED", "separado" to "RESERVED", "vendido" to "SOLD", "bloqueado" to "BLOCKED",
            "available" to "AVAILABLE", "reserved" to "RESERVED", "sold" to "SOLD", "blocked" to "BLOCKED",
        )
        fun yes(value: String) = value.trim().lowercase() in setOf("si", "sí", "true", "1")
        fun num(value: String?) = value?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
        fun key(block: String, code: String) = block.trim().lowercase() + "|" + code.trim().lowercase()
        val seen = existing.map { key(text(it, "block"), text(it, "code")) }.toMutableSet()
        val lots = mutableListOf<JsonObject>()
        for (row in rows) {
            val code = row["codigo"].orEmpty()
            val block = row["manzana"].orEmpty()
            val area = num(row["area_m2"])
            val cash = if (row["contado"].isNullOrBlank()) true else yes(row["contado"].orEmpty())
            val financing = yes(row["financiado"].orEmpty())
            val installments = num(row["cuotas"]).toInt()
            val financed = num(row["precio_financiado"])
            val down = num(row["inicial"])
            if (!seen.add(key(block, code))) return lots to "El Excel repite $code en $block."
            if (!cash && !financing) return lots to "$code: habilita al contado o financiamiento."
            if (financing && (installments < 1 || down > financed)) return lots to "$code: revisa inicial y cuotas."
            val price = num(row["precio_contado"])
            val lot = JsonObject(
                newLot() + mapOf(
                    "fromExcel" to JsonPrimitive(true),
                    "code" to JsonPrimitive(code),
                    "block" to JsonPrimitive(block),
                    "street" to JsonPrimitive(row["calle"].orEmpty()),
                    "address" to JsonPrimitive(row["direccion"].orEmpty()),
                    "type" to JsonPrimitive(row["tipo"].orEmpty()),
                    "dimensions" to JsonPrimitive(row["dimensiones"].orEmpty()),
                    "areaM2" to JsonPrimitive(area),
                    "perimeterM" to JsonPrimitive(num(row["perimetro_m"])),
                    "frontM" to JsonPrimitive(num(row["frente_m"])),
                    "price" to JsonPrimitive(price),
                    "priceM2" to JsonPrimitive(num(row["precio_m2"]).takeIf { it > 0 } ?: if (area > 0) price / area else 0.0),
                    "status" to JsonPrimitive(status[row["estado"].orEmpty().trim().lowercase()] ?: "AVAILABLE"),
                    "cashAllowed" to JsonPrimitive(cash),
                    "financingAllowed" to JsonPrimitive(financing),
                    "financedPrice" to JsonPrimitive(financed),
                    "downPayment" to JsonPrimitive(down),
                    "installments" to JsonPrimitive(installments),
                    "monthlyPayment" to JsonPrimitive(num(row["cuota_mensual"])),
                    "notes" to JsonPrimitive(row["notas"].orEmpty()),
                ),
            )
            lots += lot
        }
        return lots to null
    }

    fun addLots(plan: JsonObject, lots: List<JsonObject>): JsonObject = lots.fold(plan) { acc, lot -> upsertLot(acc, lot) }

    // ---------- edificios, pisos y departamentos ----------

    /** Plano de departamentos vacío (o completa el existente con lo que el backend exige). */
    fun ensure(plan: JsonObject, mode: PlanMode): JsonObject {
        val result = plan.toMutableMap()
        result.putIfAbsent("logoUrl", JsonPrimitive(""))
        result.putIfAbsent("zones", JsonArray(emptyList()))
        result.putIfAbsent("lots", JsonArray(emptyList()))
        if (mode == PlanMode.APARTMENTS) {
            result["mode"] = JsonPrimitive("APARTMENTS")
            result.putIfAbsent("buildings", JsonArray(emptyList()))
        }
        return JsonObject(result)
    }

    /**
     * Edificio como un rectángulo de [widthM] x [lengthM] metros centrado en el punto del proyecto. En el web se dibuja
     * sobre el mapa; aquí se genera a partir del punto y las medidas.
     */
    fun addBuilding(plan: JsonObject, name: String, widthM: Double, lengthM: Double, centerLat: Double, centerLng: Double): JsonObject {
        val dLat = lengthM / 2 / METERS_PER_DEGREE
        val dLng = widthM / 2 / (METERS_PER_DEGREE * cos(Math.toRadians(centerLat)))
        val corners = listOf(
            centerLat + dLat to centerLng - dLng,
            centerLat + dLat to centerLng + dLng,
            centerLat - dLat to centerLng + dLng,
            centerLat - dLat to centerLng - dLng,
        )
        val building = buildJsonObject {
            put("id", UUID.randomUUID().toString())
            put("name", name.trim())
            put("path", buildJsonArray { corners.forEach { (lat, lng) -> add(buildJsonObject { put("lat", lat); put("lng", lng) }) } })
            put("floors", JsonArray(emptyList()))
        }
        return upsert(plan, "buildings", building)
    }

    fun removeBuilding(plan: JsonObject, id: String): JsonObject = remove(plan, "buildings", id)

    fun addFloor(plan: JsonObject, buildingId: String, number: Int, name: String): JsonObject =
        mapBuilding(plan, buildingId) { building ->
            val floor = buildJsonObject {
                put("id", UUID.randomUUID().toString())
                put("number", number)
                put("name", name.trim())
                put("apartments", JsonArray(emptyList()))
            }
            upsert(building, "floors", floor)
        }

    fun removeFloor(plan: JsonObject, buildingId: String, floorId: String): JsonObject =
        mapBuilding(plan, buildingId) { remove(it, "floors", floorId) }

    fun setFloorImage(plan: JsonObject, buildingId: String, floorId: String, url: String, width: Int, height: Int): JsonObject =
        mapFloor(plan, buildingId, floorId) { floor ->
            JsonObject(floor + ("image" to buildJsonObject { put("url", url); put("width", width); put("height", height) }) + ("imageSource" to JsonPrimitive("CUSTOM")))
        }

    /** Departamento nuevo con su dibujo: puntos normalizados (0..1) sobre el plano del piso. */
    fun newApartment(path: List<Pair<Double, Double>>): JsonObject = buildJsonObject {
        put("id", UUID.randomUUID().toString())
        put("code", "")
        put("areaM2", 0.0)
        put("price", 0)
        put("operation", "SALE")
        put("status", "AVAILABLE")
        put("maintenanceFee", 0)
        put("bedrooms", 0)
        put("bathrooms", 0)
        put("notes", "")
        put("path", buildJsonArray { path.forEach { (x, y) -> add(buildJsonObject { put("x", x); put("y", y) }) } })
    }

    fun upsertApartment(plan: JsonObject, buildingId: String, floorId: String, apartment: JsonObject): JsonObject =
        mapFloor(plan, buildingId, floorId) { upsert(it, "apartments", apartment) }

    fun removeApartment(plan: JsonObject, buildingId: String, floorId: String, apartmentId: String): JsonObject =
        mapFloor(plan, buildingId, floorId) { remove(it, "apartments", apartmentId) }

    // ---------- imagen del plano sobre el mapa ----------

    fun image(plan: JsonObject): PlanImage? {
        val image = plan["image"] as? JsonObject ?: return null
        val center = image["center"] as? JsonObject
        fun num(obj: JsonObject?, key: String) = (obj?.get(key) as? JsonPrimitive)?.content?.toDoubleOrNull()
        return PlanImage(
            url = image.str("url").orEmpty(),
            latitude = num(center, "lat") ?: return null,
            longitude = num(center, "lng") ?: return null,
            widthM = num(image, "widthM") ?: return null,
            heightM = num(image, "heightM") ?: return null,
            rotation = num(image, "rotation") ?: 0.0,
            opacity = num(image, "opacity") ?: 0.7,
            crop = (image["crop"] as? JsonObject)?.let { c ->
                PlanCrop(num(c, "left") ?: return@let null, num(c, "top") ?: return@let null, num(c, "right") ?: return@let null, num(c, "bottom") ?: return@let null)
            },
        )
    }

    /** Punto normalizado de la hoja (x a la derecha, y hacia abajo) → coordenadas del mapa, igual que el web. */
    private fun imageToGeo(x: Double, y: Double, image: PlanImage): Pair<Double, Double> {
        val dx = (x - 0.5) * image.widthM
        val dy = (0.5 - y) * image.heightM
        val angle = Math.toRadians(image.rotation)
        val east = dx * Math.cos(angle) + dy * Math.sin(angle)
        val north = -dx * Math.sin(angle) + dy * Math.cos(angle)
        return (image.latitude + north / 111320.0) to (image.longitude + east / (111320.0 * Math.cos(Math.toRadians(image.latitude))))
    }

    private fun putCrop(plan: JsonObject, image: PlanImage, crop: PlanCrop?, widthM: Double, heightM: Double, center: Pair<Double, Double>): JsonObject {
        val current = plan["image"] as? JsonObject ?: return plan
        val base = if (crop == null) JsonObject(current - "crop") else JsonObject(
            current + ("crop" to buildJsonObject { put("left", crop.left); put("top", crop.top); put("right", crop.right); put("bottom", crop.bottom) }),
        )
        val next = JsonObject(
            base + mapOf(
                "widthM" to JsonPrimitive(widthM),
                "heightM" to JsonPrimitive(heightM),
                "center" to buildJsonObject { put("lat", center.first); put("lng", center.second) },
            ),
        )
        return JsonObject(plan + ("image" to next))
    }

    /**
     * Recorta la hoja a la parte [left]..[right] × [top]..[bottom] de lo que hoy se ve (fracciones 0..1), como el web:
     * el tamaño en metros pasa a ser el del área recortada y el centro se mueve para que el dibujo no se desplace.
     * Devuelve null si el área no es válida o queda de menos de 1 m.
     */
    fun cropImage(plan: JsonObject, left: Double, top: Double, right: Double, bottom: Double): JsonObject? {
        val image = image(plan) ?: return null
        if (listOf(left, top, right, bottom).any { !it.isFinite() || it < 0.0 || it > 1.0 } || right <= left || bottom <= top) return null
        val widthM = image.widthM * (right - left)
        val heightM = image.heightM * (bottom - top)
        if (widthM < 1.0 || heightM < 1.0) return null
        val prev = image.crop ?: PlanCrop(0.0, 0.0, 1.0, 1.0)
        val w = prev.right - prev.left
        val h = prev.bottom - prev.top
        val crop = PlanCrop(prev.left + left * w, prev.top + top * h, prev.left + right * w, prev.top + bottom * h)
        return putCrop(plan, image, crop, widthM, heightM, imageToGeo((left + right) / 2, (top + bottom) / 2, image))
    }

    /** Vuelve a mostrar la hoja completa; null si no hay recorte o la hoja completa se pasaría de 100 km. */
    fun restoreCrop(plan: JsonObject): JsonObject? {
        val image = image(plan) ?: return null
        val crop = image.crop ?: return null
        val w = crop.right - crop.left
        val h = crop.bottom - crop.top
        if (w <= 0.0 || h <= 0.0) return null
        val widthM = image.widthM / w
        val heightM = image.heightM / h
        if (widthM > 100_000.0 || heightM > 100_000.0) return null
        return putCrop(plan, image, null, widthM, heightM, imageToGeo((0.5 - crop.left) / w, (0.5 - crop.top) / h, image))
    }

    /** Coloca o mueve la imagen conservando lo que ya traía (recorte, PDF de origen). */
    fun withImage(plan: JsonObject, image: PlanImage): JsonObject {
        val current = plan["image"] as? JsonObject ?: JsonObject(emptyMap())
        val next = JsonObject(
            current + mapOf(
                "url" to JsonPrimitive(image.url),
                "center" to buildJsonObject { put("lat", image.latitude); put("lng", image.longitude) },
                "widthM" to JsonPrimitive(image.widthM.coerceIn(1.0, 100_000.0)),
                "heightM" to JsonPrimitive(image.heightM.coerceIn(1.0, 100_000.0)),
                "rotation" to JsonPrimitive(image.rotation.coerceIn(-360.0, 360.0)),
                "opacity" to JsonPrimitive(image.opacity.coerceIn(0.0, 1.0)),
            ),
        )
        return JsonObject(plan + ("image" to next))
    }

    /** Guarda de qué PDF (y página) salió la imagen del plano, como hace el web. */
    fun withSourcePdf(plan: JsonObject, url: String, page: Int, name: String): JsonObject {
        val current = plan["image"] as? JsonObject ?: return plan
        val next = JsonObject(
            current + mapOf(
                "sourcePdfUrl" to JsonPrimitive(url),
                "sourcePdfPage" to JsonPrimitive(page),
                "sourcePdfName" to JsonPrimitive(name.take(255)),
            ),
        )
        return JsonObject(plan + ("image" to next))
    }

    /** Una imagen nueva (otra URL) descarta el recorte y el PDF de origen de la anterior. */
    fun replaceImage(plan: JsonObject, image: PlanImage): JsonObject = withImage(JsonObject(plan - "image"), image)

    // ---------- zonas y dibujo sobre el mapa ----------

    fun zones(plan: JsonObject): List<JsonObject> = plan.objects("zones")

    fun newZone(kind: ZoneKind, name: String, path: List<Pair<Double, Double>>): JsonObject = buildJsonObject {
        put("id", UUID.randomUUID().toString())
        put("kind", kind.name)
        put("name", name.trim())
        put("path", geoJson(path))
    }

    fun upsertZone(plan: JsonObject, zone: JsonObject): JsonObject = upsert(plan, "zones", zone)

    fun removeZone(plan: JsonObject, id: String): JsonObject = remove(plan, "zones", id)

    /** Puntos (lat, lng) del dibujo de un lote, zona o edificio. */
    fun geoPath(obj: JsonObject): List<Pair<Double, Double>> = obj.objects("path").mapNotNull { p ->
        val lat = (p["lat"] as? JsonPrimitive)?.content?.toDoubleOrNull()
        val lng = (p["lng"] as? JsonPrimitive)?.content?.toDoubleOrNull()
        if (lat != null && lng != null) lat to lng else null
    }

    fun withGeoPath(obj: JsonObject, path: List<Pair<Double, Double>>): JsonObject = JsonObject(obj + ("path" to geoJson(path)))

    /** Lotes del inventario (p. ej. importados de Excel) que aún no se dibujaron en el mapa. */
    fun lotsWithoutPath(plan: JsonObject): List<JsonObject> = lots(plan).filter { geoPath(it).size < 3 }

    private fun geoJson(path: List<Pair<Double, Double>>) = buildJsonArray { path.forEach { (lat, lng) -> add(buildJsonObject { put("lat", lat); put("lng", lng) }) } }

    // ---------- medidas ----------

    /** Proyección plana local en metros (misma que usa el backend para validar polígonos). */
    private fun toMeters(path: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
        val origin = path.firstOrNull() ?: return emptyList()
        val k = METERS_PER_DEGREE * cos(Math.toRadians(origin.first))
        return path.map { (lat, lng) -> (lng - origin.second) * k to (lat - origin.first) * METERS_PER_DEGREE }
    }

    fun geoAreaM2(path: List<Pair<Double, Double>>): Double {
        val p = toMeters(path)
        if (p.size < 3) return 0.0
        var area = 0.0
        for (i in p.indices) {
            val a = p[i]
            val b = p[(i + 1) % p.size]
            area += a.first * b.second - b.first * a.second
        }
        return abs(area) / 2
    }

    fun geoPerimeterM(path: List<Pair<Double, Double>>): Double {
        val p = toMeters(path)
        if (p.size < 2) return 0.0
        return p.indices.sumOf { i -> val a = p[i]; val b = p[(i + 1) % p.size]; hypot(b.first - a.first, b.second - a.second) }
    }

    /** Frente aproximado: el lado más corto del polígono (los lotes suelen ser rectangulares). */
    fun geoFrontM(path: List<Pair<Double, Double>>): Double {
        val p = toMeters(path)
        if (p.size < 3) return 0.0
        return p.indices.minOf { i -> val a = p[i]; val b = p[(i + 1) % p.size]; hypot(b.first - a.first, b.second - a.second) }
    }

    /** Polígono válido sobre el mapa: simple (sin cruces), con lados y área mínimos del backend. */
    fun isValidGeoPolygon(path: List<Pair<Double, Double>>): Boolean = isSimplePolygon(toMeters(path), minEdge = 0.001, minArea = 0.01)

    /** Puntos (x, y) normalizados del dibujo de un departamento. */
    fun pathOf(apartment: JsonObject): List<Pair<Double, Double>> = apartment.objects("path").mapNotNull { p ->
        val x = (p["x"] as? JsonPrimitive)?.content?.toDoubleOrNull()
        val y = (p["y"] as? JsonPrimitive)?.content?.toDoubleOrNull()
        if (x != null && y != null) x to y else null
    }

    fun withPath(apartment: JsonObject, path: List<Pair<Double, Double>>): JsonObject =
        JsonObject(apartment + ("path" to buildJsonArray { path.forEach { (x, y) -> add(buildJsonObject { put("x", x); put("y", y) }) } }))

    fun id(obj: JsonObject): String = obj.str("id").orEmpty()

    fun text(obj: JsonObject, key: String): String = obj.str(key).orEmpty()

    // ---------- geometría ----------

    /** Polígono simple y con área: mismas reglas que el backend (`validPolygon`) sobre coordenadas planas. */
    fun isSimplePolygon(points: List<Pair<Double, Double>>, minEdge: Double = 1e-7, minArea: Double = 1e-9): Boolean {
        if (points.size < 3) return false
        var area = 0.0
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            if (hypot(b.first - a.first, b.second - a.second) < minEdge) return false
            area += a.first * b.second - b.first * a.second
            for (j in i + 2 until points.size) {
                if (i == 0 && j == points.size - 1) continue
                val c = points[j]
                val d = points[(j + 1) % points.size]
                if (segmentsIntersect(a, b, c, d)) return false
            }
        }
        return abs(area) / 2 > minArea
    }

    private fun hypot(x: Double, y: Double) = kotlin.math.sqrt(x * x + y * y)

    private fun cross(a: Pair<Double, Double>, b: Pair<Double, Double>, c: Pair<Double, Double>) =
        (b.first - a.first) * (c.second - a.second) - (b.second - a.second) * (c.first - a.first)

    private fun segmentsIntersect(a: Pair<Double, Double>, b: Pair<Double, Double>, c: Pair<Double, Double>, d: Pair<Double, Double>): Boolean =
        cross(a, b, c) * cross(a, b, d) <= 0 && cross(c, d, a) * cross(c, d, b) <= 0 &&
            maxOf(minOf(a.first, b.first), minOf(c.first, d.first)) <= minOf(maxOf(a.first, b.first), maxOf(c.first, d.first)) &&
            maxOf(minOf(a.second, b.second), minOf(c.second, d.second)) <= minOf(maxOf(a.second, b.second), maxOf(c.second, d.second))

    // ---------- internos ----------

    private const val METERS_PER_DEGREE = 111_320.0

    private fun JsonObject.str(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.objects(key: String): List<JsonObject> = (get(key) as? JsonArray).orEmpty().filterIsInstance<JsonObject>()

    private fun upsert(parent: JsonObject, key: String, item: JsonObject): JsonObject {
        val id = item.str("id")
        val items = parent.objects(key)
        val next = if (items.any { it.str("id") == id }) items.map { if (it.str("id") == id) item else it } else items + item
        return JsonObject(parent + (key to JsonArray(next)))
    }

    private fun remove(parent: JsonObject, key: String, id: String): JsonObject =
        JsonObject(parent + (key to JsonArray(parent.objects(key).filter { it.str("id") != id })))

    private fun mapBuilding(plan: JsonObject, buildingId: String, change: (JsonObject) -> JsonObject): JsonObject =
        JsonObject(plan + ("buildings" to JsonArray(buildings(plan).map { if (it.str("id") == buildingId) change(it) else it })))

    private fun mapFloor(plan: JsonObject, buildingId: String, floorId: String, change: (JsonObject) -> JsonObject): JsonObject =
        mapBuilding(plan, buildingId) { building ->
            JsonObject(building + ("floors" to JsonArray(floors(building).map { if (it.str("id") == floorId) change(it) else it })))
        }

    private fun numberText(element: JsonPrimitive): String {
        element.booleanOrNull?.let { return it.toString() }
        element.intOrNull?.let { return it.toString() }
        val d = element.content.toDoubleOrNull()
        return if (d != null && d % 1.0 == 0.0 && abs(d) < 1e12) d.toLong().toString() else element.content
    }
}

val LotStatusLabels = mapOf("AVAILABLE" to "Disponible", "RESERVED" to "Reservado", "SOLD" to "Vendido", "BLOCKED" to "Bloqueado")

val ApartmentStatusLabels = LotStatusLabels + ("RENTED" to "Alquilado")
