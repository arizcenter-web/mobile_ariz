package com.tecsup.app_ariz.feature.agents.domain.model

import com.tecsup.app_ariz.core.model.property.PropertySummary

/** Perfil público de un anunciante (`GET /agents/:slugOrId`). */
data class AgentProfile(
    val id: String,
    val name: String,
    val isVerified: Boolean,
    /** WhatsApp del anunciante; si no tiene, el web usa su teléfono. */
    val whatsapp: String?,
    val phone: String?,
    val email: String?,
    val city: String,
    val districts: String?,
    val website: String?,
    val properties: List<PropertySummary>,
) {
    /** Dígitos del número de contacto (WhatsApp o, si falta, teléfono), listos para `wa.me`. */
    val whatsappDigits: String?
        get() = (whatsapp ?: phone)?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }

    /** "+51 999 888 777": el web quita el prefijo 51 y agrupa de a 3. */
    val whatsappDisplay: String?
        get() = whatsappDigits?.removePrefix("51")?.chunked(3)?.joinToString(" ")?.let { "+51 $it" }

    /** "Zarumilla, Tumbes": distritos de atención y ciudad, sin partes vacías. */
    val locationLine: String
        get() = listOfNotNull(districts, city).filter { it.isNotBlank() }.joinToString(", ")

    /** Zona de atención: distritos o, si no hay, la ciudad. */
    val serviceZone: String get() = districts?.takeIf { it.isNotBlank() } ?: city

    val firstName: String get() = name.trim().substringBefore(' ').ifEmpty { name }

    val initials: String
        get() = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }

    /** Enlace de la web con esquema, como en el web (`https://` si no lo trae). */
    val websiteUrl: String?
        get() = website?.takeIf { it.isNotBlank() }?.let { if (it.startsWith("http")) it else "https://$it" }
}
