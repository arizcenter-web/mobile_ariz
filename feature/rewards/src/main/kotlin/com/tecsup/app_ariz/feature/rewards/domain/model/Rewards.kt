package com.tecsup.app_ariz.feature.rewards.domain.model

/** Quien accede al club con su código de referente (`POST /redemptions/access`). */
data class Referrer(
    val id: String,
    val code: String,
    val name: String,
    val email: String,
    val phone: String,
    val totalPoints: Int,
)

/** Premio del catálogo (`GET /redemptions/catalog`): solo los activos y con stock. */
data class Incentive(
    val id: String,
    val name: String,
    val description: String,
    val promotion: String?,
    val imageUrl: String?,
    val pointsRequired: Int,
    val stock: Int,
)

enum class RedemptionStatus { PENDING, APPROVED, REJECTED, DELIVERED, CANCELLED }

/** Canje solicitado por el referente (`GET /redemptions/my`). */
data class Redemption(
    val id: String,
    val incentiveName: String,
    val imageUrl: String?,
    val pointsSpent: Int,
    val status: RedemptionStatus,
    val requestedAtMillis: Long?,
    val rejectionReason: String?,
)

/** Puntos que el referente ganó en una campaña, con el desglose por referido. */
data class CampaignPoints(
    val campaignName: String,
    val totalPoints: Int,
    val referrals: List<CampaignReferral>,
)

data class CampaignReferral(val name: String, val status: String, val points: Int)

enum class ReferralInterest { VENTA, COMPRA, ALQUILER, COMPRA_LOTE, INVERSION, CORRETAJE }

enum class ReferralAvailability { INMEDIATA, AVERIGUAR, POR_CONFIRMAR }

/**
 * Datos para referir a alguien (`POST /referrals`). Con [referrerCode] el referente ya existe; sin él
 * se registra uno nuevo con [referrerName], [referrerEmail] y [referrerPhone] (se hace miembro del club).
 */
data class ReferralRequest(
    val referrerCode: String?,
    val referrerName: String,
    val referrerEmail: String,
    val referrerPhone: String,
    val name: String,
    val email: String,
    val phone: String,
    val interest: ReferralInterest,
    val availability: ReferralAvailability?,
)

/** Resultado de validar un código de referente: válido (con su nombre) o no. */
sealed interface CodeAccess {
    data class Valid(val referrer: Referrer) : CodeAccess

    data class Invalid(val message: String?) : CodeAccess
}
