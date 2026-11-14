package com.tecsup.app_ariz.feature.admin.data

import com.tecsup.app_ariz.feature.admin.domain.ActionBody
import com.tecsup.app_ariz.feature.admin.domain.AdminChildSpec
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsProject
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsRow
import com.tecsup.app_ariz.feature.admin.domain.ProfileSlice
import com.tecsup.app_ariz.feature.admin.domain.AdminDetail
import com.tecsup.app_ariz.feature.admin.domain.AdminFormSpec
import com.tecsup.app_ariz.feature.admin.domain.FieldKind
import com.tecsup.app_ariz.feature.admin.domain.AdminGroup
import com.tecsup.app_ariz.feature.admin.domain.AdminItem
import com.tecsup.app_ariz.feature.admin.domain.AdminSection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val IsoDate = Regex("""^(\d{4}-\d{2}-\d{2})T.*""")

/** El backend responde con una lista directa o con `{ items, total, ... }`; este lector acepta ambas. */
internal fun parseRows(body: String, json: Json): List<JsonObject> {
    if (body.isBlank()) return emptyList()
    val root = json.parseToJsonElement(body)
    val array = when (root) {
        is JsonArray -> root
        is JsonObject -> root["items"] as? JsonArray ?: JsonArray(emptyList())
        else -> JsonArray(emptyList())
    }
    return array.filterIsInstance<JsonObject>()
}

private fun JsonElement?.at(path: String): JsonElement? {
    var current: JsonElement? = this
    for (segment in path.split('.')) {
        current = (current as? JsonObject)?.get(segment) ?: return null
    }
    return current
}

/** Texto de un valor primitivo (fechas ISO se acortan a AAAA-MM-DD; booleanos a Sí/No). */
private fun JsonElement?.text(): String? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive is JsonNull) return null
    primitive.booleanOrNull?.let { return if (it) "Sí" else "No" }
    val raw = primitive.content.trim()
    if (raw.isEmpty()) return null
    return IsoDate.matchEntire(raw)?.groupValues?.get(1) ?: raw
}

/** Primera candidata con valor; `a+b` junta ambas con un espacio (si alguna falta, se usan las que existan). */
internal fun JsonObject.firstText(candidates: List<String>): String? {
    for (candidate in candidates) {
        val joined = candidate.split('+').mapNotNull { at(it).text() }.joinToString(" ")
        if (joined.isNotBlank()) return joined
    }
    return null
}

/** Estado en bruto: los booleanos (`isActive`, `isPublished`) se normalizan a `ACTIVE` / `INACTIVE`. */
internal fun JsonObject.statusAt(path: String?): String? {
    if (path == null) return null
    val primitive = at(path) as? JsonPrimitive ?: return null
    primitive.booleanOrNull?.let { return if (it) "ACTIVE" else "INACTIVE" }
    return primitive.content.takeIf { it.isNotBlank() && primitive !is JsonNull }
}

internal fun JsonObject.toAdminItem(section: AdminSection, children: List<AdminChildSpec> = section.children): AdminItem? {
    val id = at("id").text() ?: return null
    val status = statusAt(section.statusPath)
    return AdminItem(
        id = id,
        title = firstText(section.titlePaths) ?: id,
        subtitle = firstText(section.subtitlePaths),
        status = status,
        details = section.details.mapNotNull { spec -> firstText(spec.paths)?.let { AdminDetail(spec.label, it) } },
        actions = section.actions,
        groups = children.mapNotNull { toGroup(it) },
        link = section.linkPath?.let { at(it).text() }?.takeIf { it.startsWith("https://") },
        values = if (section.form != null) formValues() else emptyMap(),
        fields = flatFields(),
    )
}

private fun JsonObject.toGroup(spec: AdminChildSpec): AdminGroup? {
    val rows = (get(spec.jsonKey) as? JsonArray)?.filterIsInstance<JsonObject>().orEmpty()
    val items = rows.mapNotNull { row ->
        val id = row.at(spec.idPath).text() ?: return@mapNotNull null
        AdminItem(
            id = id,
            title = row.firstText(spec.titlePaths) ?: id,
            subtitle = null,
            status = row.statusAt(spec.statusPath),
            details = spec.details.mapNotNull { d -> row.firstText(d.paths)?.let { AdminDetail(d.label, it) } },
            actions = spec.actions,
        )
    }
    return if (items.isEmpty()) null else AdminGroup(spec.label, items)
}

/** Valores en bruto de los campos de primer nivel (fechas ISO acortadas, listas una por línea). */
internal fun JsonObject.formValues(): Map<String, String> = buildMap {
    for ((key, element) in this@formValues) {
        when (element) {
            is JsonPrimitive -> if (element !is JsonNull) {
                val raw = element.content
                put(key, IsoDate.matchEntire(raw)?.groupValues?.get(1) ?: raw)
            }
            is JsonArray -> put(key, element.filterIsInstance<JsonPrimitive>().joinToString("\n") { it.content })
            // Objetos anidados de un nivel (p. ej. `points.VISIT`).
            is JsonObject -> for ((child, value) in element) {
                if (value is JsonPrimitive && value !is JsonNull) put("$key.$child", value.content)
            }
            else -> Unit
        }
    }
}

/**
 * Arma el cuerpo JSON de un formulario. Los vacíos opcionales se omiten; las banderas siempre se envían; una clave con
 * punto (`points.VISIT`) se anida. Devuelve el cuerpo y las claves con valor inválido o faltante.
 */
internal fun buildFormBody(spec: AdminFormSpec, values: Map<String, String>, editing: Boolean): Pair<JsonObject, Set<String>> {
    val invalid = mutableSetOf<String>()
    val flat = linkedMapOf<String, JsonElement>()
    for (field in spec.fields) {
        if (editing && field.createOnly) continue
        val raw = values[field.key].orEmpty().trim()
        if (field.kind == FieldKind.BOOL) {
            flat[field.key] = JsonPrimitive(raw == "true")
            continue
        }
        if (raw.isEmpty()) {
            if (field.required) invalid += field.key
            continue
        }
        val element: JsonElement? = when (field.kind) {
            FieldKind.NUMBER -> raw.toDoubleOrNull()?.let { JsonPrimitive(it) }
            FieldKind.INT -> raw.toIntOrNull()?.let { JsonPrimitive(it) }
            FieldKind.LIST, FieldKind.IMAGES, FieldKind.MULTI -> JsonArray(raw.lines().map { it.trim() }.filter { it.isNotEmpty() }.map { JsonPrimitive(it) })
            FieldKind.DATE -> raw.takeIf { Regex("""^\d{4}-\d{2}-\d{2}$""").matches(it) }?.let { JsonPrimitive(it) }
            else -> JsonPrimitive(raw)
        }
        if (element == null) invalid += field.key else flat[field.key] = element
    }
    if (!editing) spec.emptyListsOnCreate.forEach { if (it !in flat) flat[it] = JsonArray(emptyList()) }

    val nested = linkedMapOf<String, MutableMap<String, JsonElement>>()
    val body = buildJsonObject {
        for ((key, element) in flat) {
            if ('.' in key) nested.getOrPut(key.substringBefore('.')) { linkedMapOf() }[key.substringAfter('.')] = element
            else put(key, element)
        }
        nested.forEach { (parent, children) -> put(parent, JsonObject(children)) }
    }
    return body to invalid
}

/** Cuerpo JSON de una acción; el motivo (si lo hay) se agrega bajo `reasonKey`. */
internal fun ActionBody.toJson(reasonKey: String?, reason: String?): JsonObject = buildJsonObject {
    strings.forEach { (key, value) -> put(key, value) }
    numbers.forEach { (key, value) -> put(key, value) }
    flags.forEach { (key, value) -> put(key, value) }
    if (reasonKey != null && !reason.isNullOrBlank()) put(reasonKey, reason.trim())
}

private val DateKeys = listOf("publishedAt", "startedAt", "createdAt", "updatedAt")

internal fun JsonObject.toAnalyticsRow(typePaths: List<String>) = AnalyticsRow(firstText(DateKeys), firstText(typePaths))

internal fun JsonObject.toAnalyticsProject() = AnalyticsProject(
    row = toAnalyticsRow(listOf("agent.type")),
    name = firstText(listOf("name")) ?: "Proyecto",
    lots = firstText(listOf("lotsCount", "totalLots"))?.toDoubleOrNull()?.toInt() ?: 0,
)

/** Perfiles por tipo: acepta filas ya agregadas (`{type, count}`) o una fila por perfil (`{type}`). */
internal fun parseDistribution(body: String, json: Json): List<ProfileSlice> {
    val rows = parseRows(body, json)
    val aggregated = rows.any { it.firstText(listOf("count")) != null }
    return listOf("REAL_ESTATE_AGENCY", "INDEPENDENT_AGENT", "DEVELOPER").map { type ->
        val ofType = rows.filter { it.firstText(listOf("type")) == type }
        ProfileSlice(type, if (aggregated) ofType.sumOf { it.firstText(listOf("count"))?.toDoubleOrNull()?.toInt() ?: 0 } else ofType.size)
    }
}

/** Etiquetas en español de los campos más comunes; el resto se muestra con su nombre separado en palabras. */
private val FieldLabels = mapOf(
    "name" to "Nombre", "firstName" to "Nombres", "lastName" to "Apellidos", "fullName" to "Nombre completo",
    "email" to "Correo", "phone" to "Teléfono", "whatsapp" to "WhatsApp", "description" to "Descripción",
    "status" to "Estado", "type" to "Tipo", "title" to "Título", "code" to "Código", "slug" to "Enlace",
    "createdAt" to "Creado", "updatedAt" to "Actualizado", "publishedAt" to "Publicado", "startedAt" to "Inicio",
    "expiresAt" to "Vence", "startDate" to "Inicio", "endDate" to "Fin", "startsAt" to "Inicio", "endsAt" to "Fin",
    "price" to "Precio", "amount" to "Monto", "currency" to "Moneda", "points" to "Puntos", "stock" to "Stock",
    "businessName" to "Razón social", "ruc" to "RUC", "city" to "Ciudad", "address" to "Dirección",
    "district" to "Distrito", "province" to "Provincia", "department" to "Departamento", "website" to "Sitio web",
    "message" to "Mensaje", "reason" to "Motivo", "rejectionReason" to "Motivo de rechazo", "isActive" to "Activo",
    "claimCode" to "Código de reclamo", "claimDetails" to "Detalle", "claimedAmount" to "Monto reclamado",
    "bankAccount" to "Cuenta bancaria", "document" to "Documento", "documentNumber" to "N.º de documento",
)

private fun fieldLabel(path: String): String {
    val last = path.substringAfterLast('.')
    FieldLabels[last]?.let { label -> return if ('.' in path) path.substringBeforeLast('.').split('.').joinToString(" ") { FieldLabels[it] ?: humanize(it) } + " · " + label else label }
    return path.split('.').joinToString(" · ") { humanize(it) }
}

private fun humanize(key: String): String =
    key.replace(Regex("([a-z0-9])([A-Z])"), "$1 $2").lowercase().replaceFirstChar { it.uppercase() }

/** Aplana el registro (hasta 3 niveles) a pares etiqueta/valor; omite ids, hashes y vacíos. */
internal fun JsonObject.flatFields(): List<AdminDetail> {
    val out = mutableListOf<AdminDetail>()
    fun walk(obj: JsonObject, prefix: String, depth: Int) {
        for ((key, element) in obj) {
            if (key == "id" || key.endsWith("Id") || key.endsWith("Hash") || key == "password") continue
            val path = if (prefix.isEmpty()) key else "$prefix.$key"
            when (element) {
                is JsonPrimitive -> element.text()?.let { out += AdminDetail(fieldLabel(path), it) }
                is JsonArray -> {
                    val primitives = element.filterIsInstance<JsonPrimitive>().mapNotNull { it.text() }
                    if (primitives.isNotEmpty()) out += AdminDetail(fieldLabel(path), primitives.joinToString(", "))
                    else if (element.isNotEmpty()) out += AdminDetail(fieldLabel(path), "${element.size}")
                }
                is JsonObject -> if (depth < 2) walk(element, path, depth + 1)
                else -> Unit
            }
        }
    }
    walk(this, "", 0)
    return out
}
