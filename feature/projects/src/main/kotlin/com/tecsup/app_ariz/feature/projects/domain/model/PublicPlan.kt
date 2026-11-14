package com.tecsup.app_ariz.feature.projects.domain.model

import com.tecsup.app_ariz.core.model.common.Currency

data class PlanPoint(val lat: Double, val lng: Double)

/** Lote del plano publicado (`GET /projects/:slug/site-plan`), con su financiamiento y su dibujo en el mapa. */
data class PublicPlanLot(
    val id: String,
    val code: String,
    val block: String,
    val address: String,
    val areaM2: Double,
    val price: Double,
    val status: LotStatus,
    val cashAllowed: Boolean,
    val financingAllowed: Boolean,
    val financedPrice: Double,
    val downPayment: Double,
    val installments: Int,
    val monthlyPayment: Double,
    val notes: String,
    val path: List<PlanPoint>,
)

data class PublicPlanApartment(
    val id: String,
    val code: String,
    val building: String,
    val floor: String,
    val areaM2: Double,
    val price: Double,
    /** `SALE` o `RENT`. */
    val operation: String,
    /** `AVAILABLE`, `RESERVED`, `SOLD`, `RENTED` o `BLOCKED`. */
    val status: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val maintenanceFee: Double,
    val financingAllowed: Boolean,
    val financedPrice: Double,
    val downPayment: Double,
    val installments: Int,
    val monthlyPayment: Double,
    val notes: String,
)

/** Imagen del plano sobre el mapa. */
data class PlanOverlay(val url: String, val lat: Double, val lng: Double, val widthM: Double, val heightM: Double, val rotation: Double, val opacity: Double)

data class PlanZoneView(val kind: String, val name: String, val path: List<PlanPoint>)

data class PublicPlan(
    val isApartments: Boolean,
    val currency: Currency,
    val lots: List<PublicPlanLot>,
    val apartments: List<PublicPlanApartment>,
    val zones: List<PlanZoneView>,
    val overlay: PlanOverlay?,
) {
    val hasMap: Boolean get() = !isApartments && lots.any { it.path.size >= 3 }
}
