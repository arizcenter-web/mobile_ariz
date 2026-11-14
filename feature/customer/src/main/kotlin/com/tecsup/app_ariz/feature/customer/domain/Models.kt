package com.tecsup.app_ariz.feature.customer.domain

/** Estado de la solicitud de membresía de un propietario particular. */
enum class MembershipStatus { ACTIVE, PENDING_PAYMENT, REJECTED, EXPIRED }

data class Membership(
    val status: MembershipStatus,
    val propertyLimit: Int,
    val expiresAtMillis: Long?,
    val rejectionReason: String?,
)

data class MembershipSummary(
    val membership: Membership?,
    val publishedCount: Int,
    /** Aún no usó la publicación gratuita (30 días para la primera propiedad). */
    val trialAvailable: Boolean = false,
) {
    /** Días que le quedan a la membresía activa (null si no hay). */
    fun daysRemaining(nowMillis: Long): Int? {
        val m = membership?.takeIf { it.status == MembershipStatus.ACTIVE } ?: return null
        val expires = m.expiresAtMillis ?: return null
        return maxOf(0L, (expires - nowMillis) / DAY_MILLIS).toInt()
    }

    private companion object {
        const val DAY_MILLIS = 86_400_000L
    }
}

/** Paquete de membresía (precios del web: `PRICING`). */
data class MembershipPackage(val propertyLimit: Int, val durationDays: Int, val price: Double) {
    companion object {
        val All: List<MembershipPackage> = listOf(
            MembershipPackage(1, 15, 80.0),
            MembershipPackage(1, 30, 150.0),
            MembershipPackage(2, 15, 120.0),
            MembershipPackage(2, 30, 225.0),
            MembershipPackage(5, 15, 200.0),
            MembershipPackage(5, 30, 350.0),
        )
    }
}

/** Conteo de las propiedades del cliente para el dashboard. */
data class PropertyStats(val total: Int, val published: Int, val paused: Int, val rentals: Int)

data class CustomerProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val biography: String,
)

data class CustomerProfileUpdate(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val biography: String,
)
