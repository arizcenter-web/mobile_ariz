package com.tecsup.app_ariz.feature.professional.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

// ---- Perfil, ajustes y contraseña ----

@Serializable
internal data class UserDto(
    val firstName: String = "",
    val lastName: String? = null,
    val email: String = "",
    val phone: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
internal data class ProfileDto(
    val id: String = "",
    val type: String = "INDEPENDENT_AGENT",
    val status: String = "PENDING",
    val businessName: String? = null,
    val ruc: String? = null,
    val whatsapp: String? = null,
    val city: String? = null,
    val districts: String? = null,
    val address: String? = null,
    val website: String? = null,
    val socialLinks: String? = null,
    val user: UserDto = UserDto(),
)

@Serializable
internal data class ProfileUpdateRequest(
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

@Serializable
internal data class PasswordChangeRequest(val currentPassword: String, val newPassword: String)

@Serializable
internal data class SettingsDto(
    val emailLeads: Boolean = true,
    val whatsappLeads: Boolean = true,
    val membershipReminders: Boolean = true,
    val weeklyStats: Boolean = true,
)

// ---- Propiedades y proyectos propios ----

@Serializable
internal data class PriceDto(val amount: Double = 0.0, val currency: String = "PEN")

@Serializable
internal data class LocationDto(val district: String = "", val department: String = "")

@Serializable
internal data class OwnerPropertyDto(
    val id: String,
    val slug: String = "",
    val title: String = "",
    val status: String = "DRAFT",
    val operation: String = "buy",
    val price: PriceDto = PriceDto(),
    val location: LocationDto = LocationDto(),
    val images: List<String> = emptyList(),
    val coverImage: String? = null,
    val publishedAt: String? = null,
)

@Serializable
internal data class OwnerProjectDto(
    val id: String,
    val slug: String = "",
    val name: String = "",
    val status: String = "DRAFT",
    val currency: String = "PEN",
    val priceFrom: Double = 0.0,
    val lotsCount: Int = 0,
    val location: LocationDto? = null,
    val coverImage: String? = null,
    val images: List<String> = emptyList(),
    val publishedAt: String? = null,
)

// ---- Edición de una propiedad ----

@Serializable
internal data class EditLocationDto(
    val department: String = "",
    val province: String = "",
    val district: String = "",
    val address: String = "",
    val reference: String? = null,
    val approximateLocation: Boolean = true,
)

@Serializable
internal data class EditDetailsDto(
    val totalArea: Double? = null,
    val constructedArea: Double? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val parkingSpaces: Int? = null,
)

@Serializable
internal data class EditMediaDto(val url: String = "", val publicId: String? = null)

@Serializable
internal data class PropertyEditDto(
    val id: String,
    val operationType: String = "SALE",
    val propertyType: String = "APARTMENT",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = "PEN",
    val negotiable: Boolean = false,
    val maintenanceFee: Double? = null,
    val contactName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val location: EditLocationDto? = null,
    val details: EditDetailsDto? = null,
    val amenityCodes: List<String> = emptyList(),
    val media: List<EditMediaDto> = emptyList(),
)

// ---- Crear / editar propiedad (cuerpo de POST y PATCH /properties) ----

@Serializable
internal data class PropertyLocationBody(
    val department: String,
    val province: String,
    val district: String,
    val address: String,
    val reference: String? = null,
    val approximateLocation: Boolean = true,
)

@Serializable
internal data class PropertyDetailsBody(
    val totalArea: Double? = null,
    val constructedArea: Double? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val parkingSpaces: Int? = null,
)

@Serializable
internal data class PropertyMediaBody(
    val url: String,
    val publicId: String? = null,
    val position: Int,
    val isCover: Boolean,
)

@Serializable
internal data class PropertyBody(
    val operationType: String,
    val propertyType: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String,
    val negotiable: Boolean,
    val maintenanceFee: Double? = null,
    val location: PropertyLocationBody,
    val details: PropertyDetailsBody,
    val amenityCodes: List<String> = emptyList(),
    val media: List<PropertyMediaBody>,
    val contactName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
)

// ---- Consultas ----

@Serializable
internal data class LeadTargetDto(val id: String = "", val title: String? = null, val name: String? = null)

@Serializable
internal data class LeadDto(
    val id: String,
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val message: String? = null,
    val source: String = "",
    val createdAt: String? = null,
    val property: LeadTargetDto? = null,
    val project: LeadTargetDto? = null,
)

// ---- Plan ----

@Serializable
internal data class PlanDto(
    val id: String,
    val name: String = "",
    // Decimal de Prisma: llega como texto ("49.90") o número.
    val price: JsonPrimitive = JsonPrimitive(0),
    val currency: String = "PEN",
    val durationDays: Int = 0,
    val propertyLimit: Int = 0,
    val projectLimit: Int = 0,
    val features: List<String> = emptyList(),
    val isFree: Boolean = false,
    val highlight: Boolean = false,
)

@Serializable
internal data class SubscriptionDto(
    val id: String,
    val status: String = "PENDING_PAYMENT",
    val rejectionReason: String? = null,
    val expiresAt: String? = null,
    val plan: PlanDto? = null,
)

@Serializable
internal data class PlanRequestBody(val receiptUrl: String? = null)

/** Se usa para respuestas que la app no necesita leer. */
internal typealias Ignored = JsonElement

// ---- Crear proyecto (cuerpo de POST /projects) ----

@Serializable
internal data class ProjectLocationBody(
    val department: String,
    val province: String,
    val district: String,
    val zone: String? = null,
    val street: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
internal data class ProjectLotBody(
    val lotNumber: String,
    val block: String? = null,
    val areaM2: Double,
    val price: Double,
    val status: String,
    val frontM: Double? = null,
    val planLocation: String? = null,
)

@Serializable
internal data class ProjectMediaBody(
    val type: String,
    val url: String,
    val position: Int,
    val isCover: Boolean,
)

// Sin valores por defecto en category/lotMode: kotlinx no serializa los valores por defecto y el backend los exige.
@Serializable
internal data class ProjectBody(
    val category: String,
    val stage: String,
    val name: String,
    val description: String,
    val currency: String,
    val priceFrom: Double,
    val priceTo: Double,
    val location: ProjectLocationBody,
    val lotMode: String,
    val lots: List<ProjectLotBody>,
    val estimatedDelivery: String,
    val media: List<ProjectMediaBody>,
    val contactName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val contactWhatsapp: String? = null,
)
