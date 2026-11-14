package com.tecsup.app_ariz.feature.professional.data

import com.tecsup.app_ariz.feature.professional.domain.LotDraft
import com.tecsup.app_ariz.feature.professional.domain.ProjectDraft
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

/** Campos del proyecto que la app no edita y que el backend borraría si no se reenvían en el PATCH. */
private val PreservedKeys = listOf(
    "ruc", "businessName", "downPayment", "monthlyPayment", "financing", "totalBlocks", "totalLots", "social",
    "services", "urbanizationName", "registryEntry", "amenities", "constructionStart", "certifications", "tourUrl",
)

/** Por qué un proyecto no se puede editar desde el móvil (null si se puede). */
internal fun JsonObject.notEditableReason(): String? = when {
    // Con plano o de departamentos se edita solo la información: los lotes/unidades los define el plano.
    isManagedByPlan() -> null
    text("lotMode") != null && text("lotMode") != "INDIVIDUAL" -> "Este proyecto se edita en el panel web."
    else -> null
}

internal fun JsonObject.isManagedByPlan(): Boolean =
    text("category") == "DEPARTAMENTOS" || (get("hasSitePlan") as? JsonPrimitive)?.booleanOrNull == true

private fun JsonObject.text(key: String): String? = (get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content

private fun JsonElement?.number(): String {
    val p = this as? JsonPrimitive ?: return ""
    if (p is JsonNull) return ""
    val d = p.doubleOrNull ?: return p.content
    return if (d % 1.0 == 0.0) d.toLong().toString() else d.toString()
}

internal fun JsonObject.toProjectDraft(): ProjectDraft {
    val loc = get("location") as? JsonObject
    val agent = get("agent") as? JsonObject
    val media = (get("media") as? JsonArray).orEmpty().filterIsInstance<JsonObject>()
    val images = media.filter { it.text("type") == "IMAGE" }.sortedWith(compareByDescending<JsonObject> { (it["isCover"] as? JsonPrimitive)?.booleanOrNull == true }.thenBy { (it["position"] as? JsonPrimitive)?.intOrNull ?: 0 })
    val others = media.filter { it.text("type") != "IMAGE" }
    val milestones = (get("milestones") as? JsonArray).orEmpty().filterIsInstance<JsonObject>()
    val extras = buildJsonObject {
        PreservedKeys.forEach { key -> get(key)?.takeIf { it !is JsonNull }?.let { put(key, it) } }
        if (milestones.isNotEmpty()) {
            put("milestones", buildJsonArray {
                milestones.forEach { m ->
                    add(buildJsonObject {
                        m.text("title")?.let { put("title", it) }
                        m.text("date")?.let { put("date", it) }
                        m.text("note")?.let { put("note", it) }
                    })
                }
            })
        }
        if (others.isNotEmpty()) {
            put("otherMedia", buildJsonArray {
                others.forEach { m ->
                    add(buildJsonObject {
                        m.text("type")?.let { put("type", it) }
                        m.text("url")?.let { put("url", it) }
                        m.text("publicId")?.let { put("publicId", it) }
                    })
                }
            })
        }
    }
    return ProjectDraft(
        category = text("category") ?: "LOTIZACION",
        managedByPlan = isManagedByPlan(),
        name = text("name").orEmpty(),
        description = text("description").orEmpty(),
        stage = text("stage") ?: "PRE_SALE",
        currency = text("currency") ?: "PEN",
        priceFrom = get("priceFrom").number(),
        priceTo = get("priceTo").number(),
        department = loc?.text("department").orEmpty(),
        province = loc?.text("province").orEmpty(),
        district = loc?.text("district").orEmpty(),
        zone = loc?.text("zone").orEmpty(),
        street = loc?.text("street").orEmpty(),
        latitude = loc?.get("latitude").number(),
        longitude = loc?.get("longitude").number(),
        estimatedDelivery = text("estimatedDelivery").orEmpty().take(10),
        photos = images.mapNotNull { it.text("url") },
        lots = (get("lots") as? JsonArray).orEmpty().filterIsInstance<JsonObject>().map {
            LotDraft(
                number = it.text("lotNumber").orEmpty(),
                block = it.text("block").orEmpty(),
                area = it["areaM2"].number(),
                price = it["price"].number(),
                status = it.text("status") ?: "AVAILABLE",
                frontM = it["frontM"].number(),
                planLocation = it.text("planLocation").orEmpty(),
            )
        }.ifEmpty { listOf(LotDraft()) },
        contactName = agent?.text("name").orEmpty(),
        contactEmail = agent?.text("email").orEmpty(),
        contactPhone = agent?.text("phone").orEmpty(),
        contactWhatsapp = agent?.text("whatsapp").orEmpty(),
        extras = extras.toString(),
    )
}

/** Cuerpo del PATCH: lo que edita la app + lo preservado (hitos, videos, financiamiento...). */
internal fun ProjectDraft.toUpdateBody(json: Json): JsonObject {
    val base = json.encodeToJsonElement(ProjectBody.serializer(), toBody()) as JsonObject
    val saved = extras?.let { runCatching { json.parseToJsonElement(it) as? JsonObject }.getOrNull() } ?: return base
    return buildJsonObject {
        base.forEach { (key, value) ->
            if (key == "media") {
                val extraMedia = (saved["otherMedia"] as? JsonArray).orEmpty().filterIsInstance<JsonObject>()
                val images = (value as? JsonArray).orEmpty()
                put("media", buildJsonArray {
                    images.forEach { add(it) }
                    extraMedia.forEachIndexed { i, m ->
                        add(buildJsonObject {
                            m.forEach { (k, v) -> put(k, v) }
                            put("position", images.size + i)
                            put("isCover", false)
                        })
                    }
                })
            } else put(key, value)
        }
        saved.forEach { (key, value) -> if (key != "otherMedia" && key !in base) put(key, value) }
    }
}
