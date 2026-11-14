package com.tecsup.app_ariz.feature.professional.domain

import com.tecsup.app_ariz.core.model.common.Currency

enum class PropertyStatus { DRAFT, PENDING_REVIEW, PUBLISHED, PAUSED, SOLD, RENTED, REJECTED, ARCHIVED }

enum class ProjectStatus { DRAFT, PENDING_REVIEW, PUBLISHED, PAUSED, SOLD, REJECTED, ARCHIVED }

enum class SubscriptionStatus { PENDING_PAYMENT, ACTIVE, REJECTED, EXPIRED }

enum class ProfessionalType { INDEPENDENT_AGENT, REAL_ESTATE_AGENCY, DEVELOPER }

enum class ApprovalState { PENDING, APPROVED, REJECTED }

/** Perfil del profesional autenticado (`GET /professionals/me`). */
data class ProfessionalProfile(
    val id: String,
    val type: ProfessionalType,
    val status: ApprovalState,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val avatarUrl: String,
    val businessName: String,
    val ruc: String,
    val whatsapp: String,
    val city: String,
    val districts: String,
    val address: String,
    val website: String,
    val socialLinks: String,
) {
    val displayName: String get() = businessName.ifBlank { "$firstName $lastName".trim() }
}

/** Datos editables del perfil: solo se envía lo que cambió respecto del servidor. */
data class ProfileUpdate(
    val firstName: String,
    val businessName: String,
    val phone: String,
    val whatsapp: String,
    val city: String,
    val districts: String,
    val address: String,
    val website: String,
    val socialLinks: String,
    val ruc: String,
)

data class NotificationSettings(
    val emailLeads: Boolean,
    val whatsappLeads: Boolean,
    val membershipReminders: Boolean,
    val weeklyStats: Boolean,
)

/** Propiedad propia en la lista del panel (`GET /properties/owner/me`). */
data class OwnerProperty(
    val id: String,
    val slug: String,
    val title: String,
    val status: PropertyStatus,
    val isRent: Boolean,
    val price: Double,
    val currency: Currency,
    val district: String,
    val department: String,
    val coverImageUrl: String?,
    val publishedAtMillis: Long?,
)

/** Proyecto propio en la lista del panel (`GET /projects/owner/me`). */
data class OwnerProject(
    val id: String,
    val slug: String,
    val name: String,
    val status: ProjectStatus,
    val priceFrom: Double,
    val currency: Currency,
    val lotsCount: Int,
    val district: String,
    val coverImageUrl: String?,
    val publishedAtMillis: Long?,
)

/** Consulta recibida por un anuncio (`GET /leads/mine`). */
data class OwnerLead(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val message: String,
    val source: String,
    val createdAtMillis: Long?,
    val targetId: String?,
    val targetTitle: String,
)

data class Plan(
    val id: String,
    val name: String,
    val price: Double,
    val currency: Currency,
    val durationDays: Int,
    val propertyLimit: Int,
    val projectLimit: Int,
    val features: List<String>,
    val isFree: Boolean,
    val highlight: Boolean,
)

data class Subscription(
    val id: String,
    val status: SubscriptionStatus,
    val planName: String,
    val rejectionReason: String?,
    val expiresAtMillis: Long?,
) {
    val isActive: Boolean get() = status == SubscriptionStatus.ACTIVE
}

/** Datos de una propiedad para crearla o editarla (`POST/PATCH /properties`). */
data class PropertyDraft(
    val operation: String = "SALE",
    val type: String = "APARTMENT",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val currency: Currency = Currency.PEN,
    val negotiable: Boolean = false,
    val maintenanceFee: String = "",
    val department: String = "Tumbes",
    val province: String = "Tumbes",
    val district: String = "",
    val address: String = "",
    val reference: String = "",
    val approximateLocation: Boolean = true,
    val totalArea: String = "",
    val constructedArea: String = "",
    val bedrooms: String = "",
    val bathrooms: String = "",
    val parkingSpaces: String = "",
    val amenities: Set<String> = emptySet(),
    val photos: List<String> = emptyList(),
    val contactName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
)

/** Foto ya subida a Cloudinary. */
data class UploadedPhoto(val url: String, val publicId: String)
