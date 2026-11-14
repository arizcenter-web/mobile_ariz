package com.tecsup.app_ariz.feature.projects.data.mapper

import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.feature.projects.domain.model.LotStatus
import com.tecsup.app_ariz.feature.projects.domain.model.PlanOverlay
import com.tecsup.app_ariz.feature.projects.domain.model.PlanPoint
import com.tecsup.app_ariz.feature.projects.domain.model.PlanZoneView
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlan
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlanApartment
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlanLot
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

private fun JsonObject.text(key: String): String = (get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content.orEmpty()

private fun JsonObject.num(key: String): Double = (get(key) as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0

private fun JsonObject.flag(key: String, default: Boolean = false): Boolean = (get(key) as? JsonPrimitive)?.booleanOrNull ?: default

private fun JsonObject.objects(key: String): List<JsonObject> = (get(key) as? JsonArray).orEmpty().filterIsInstance<JsonObject>()

private fun JsonObject.points(): List<PlanPoint> = objects("path").mapNotNull { p ->
    val lat = (p["lat"] as? JsonPrimitive)?.content?.toDoubleOrNull()
    val lng = (p["lng"] as? JsonPrimitive)?.content?.toDoubleOrNull()
    if (lat != null && lng != null) PlanPoint(lat, lng) else null
}

/** Plano publicado (lotes o edificios → pisos → departamentos) a un modelo plano para la lista y el mapa. */
internal fun JsonObject.toPublicPlan(): PublicPlan {
    val apartments = text("mode") == "APARTMENTS"
    val image = get("image") as? JsonObject
    val center = image?.get("center") as? JsonObject
    val overlay = if (image != null && center != null && image.text("url").isNotBlank()) {
        PlanOverlay(image.text("url"), center.num("lat"), center.num("lng"), image.num("widthM"), image.num("heightM"), image.num("rotation"), image.num("opacity").takeIf { it > 0 } ?: 0.7)
    } else null
    return PublicPlan(
        isApartments = apartments,
        currency = runCatching { Currency.valueOf(text("currency")) }.getOrDefault(Currency.PEN),
        lots = if (apartments) emptyList() else objects("lots").map { it.toLot() },
        apartments = if (apartments) {
            objects("buildings").flatMap { building ->
                building.objects("floors").flatMap { floor ->
                    floor.objects("apartments").map { it.toApartment(building.text("name"), floor.text("name")) }
                }
            }
        } else emptyList(),
        zones = objects("zones").map { PlanZoneView(it.text("kind"), it.text("name"), it.points()) },
        overlay = overlay,
    )
}

private fun JsonObject.toLot() = PublicPlanLot(
    id = text("id"),
    code = text("code"),
    block = text("block"),
    address = text("address"),
    areaM2 = num("areaM2"),
    price = num("price"),
    status = runCatching { LotStatus.valueOf(text("status")) }.getOrDefault(LotStatus.AVAILABLE),
    cashAllowed = flag("cashAllowed", true),
    financingAllowed = flag("financingAllowed"),
    financedPrice = num("financedPrice"),
    downPayment = num("downPayment"),
    installments = num("installments").toInt(),
    monthlyPayment = num("monthlyPayment"),
    notes = text("notes"),
    path = points(),
)

private fun JsonObject.toApartment(building: String, floor: String) = PublicPlanApartment(
    id = text("id"),
    code = text("code"),
    building = building,
    floor = floor,
    areaM2 = num("areaM2"),
    price = num("price"),
    operation = text("operation").ifBlank { "SALE" },
    status = text("status").ifBlank { "AVAILABLE" },
    bedrooms = num("bedrooms").toInt(),
    bathrooms = num("bathrooms").toInt(),
    maintenanceFee = num("maintenanceFee"),
    financingAllowed = flag("financingAllowed"),
    financedPrice = num("financedPrice"),
    downPayment = num("downPayment"),
    installments = num("installments").toInt(),
    monthlyPayment = num("monthlyPayment"),
    notes = text("notes"),
)
