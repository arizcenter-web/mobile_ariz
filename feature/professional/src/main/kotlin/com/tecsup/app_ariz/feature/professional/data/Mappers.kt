package com.tecsup.app_ariz.feature.professional.data

import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.feature.professional.domain.ApprovalState
import com.tecsup.app_ariz.feature.professional.domain.NotificationSettings
import com.tecsup.app_ariz.feature.professional.domain.OwnerLead
import com.tecsup.app_ariz.feature.professional.domain.OwnerProject
import com.tecsup.app_ariz.feature.professional.domain.OwnerProperty
import com.tecsup.app_ariz.feature.professional.domain.Plan
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalProfile
import com.tecsup.app_ariz.feature.professional.domain.LotDraft
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalType
import com.tecsup.app_ariz.feature.professional.domain.ProjectDraft
import com.tecsup.app_ariz.feature.professional.domain.ProfileUpdate
import com.tecsup.app_ariz.feature.professional.domain.ProjectStatus
import com.tecsup.app_ariz.feature.professional.domain.PropertyDraft
import com.tecsup.app_ariz.feature.professional.domain.PropertyStatus
import com.tecsup.app_ariz.feature.professional.domain.Subscription
import com.tecsup.app_ariz.feature.professional.domain.SubscriptionStatus
import kotlinx.serialization.json.contentOrNull

private fun String.toCurrency() = runCatching { Currency.valueOf(this) }.getOrDefault(Currency.PEN)

internal fun ProfileDto.toDomain() = ProfessionalProfile(
    id = id,
    type = runCatching { ProfessionalType.valueOf(type) }.getOrDefault(ProfessionalType.INDEPENDENT_AGENT),
    status = runCatching { ApprovalState.valueOf(status) }.getOrDefault(ApprovalState.PENDING),
    firstName = user.firstName,
    lastName = user.lastName.orEmpty(),
    email = user.email,
    phone = user.phone.orEmpty(),
    avatarUrl = user.avatarUrl.orEmpty(),
    businessName = businessName.orEmpty(),
    ruc = ruc.orEmpty(),
    whatsapp = whatsapp.orEmpty(),
    city = city.orEmpty(),
    districts = districts.orEmpty(),
    address = address.orEmpty(),
    website = website.orEmpty(),
    socialLinks = socialLinks.orEmpty(),
)

internal fun ProfileUpdate.toBody() = ProfileUpdateRequest(
    firstName = firstName.trim(),
    businessName = businessName.trim(),
    phone = phone.trim(),
    whatsapp = whatsapp.trim(),
    city = city.trim(),
    districts = districts.trim(),
    address = address.trim(),
    website = website.trim(),
    socialLinks = socialLinks.trim(),
    ruc = ruc.trim(),
)

internal fun SettingsDto.toDomain() = NotificationSettings(emailLeads, whatsappLeads, membershipReminders, weeklyStats)

internal fun NotificationSettings.toDto() = SettingsDto(emailLeads, whatsappLeads, membershipReminders, weeklyStats)

internal fun OwnerPropertyDto.toDomain() = OwnerProperty(
    id = id,
    slug = slug,
    title = title,
    status = runCatching { PropertyStatus.valueOf(status) }.getOrDefault(PropertyStatus.DRAFT),
    isRent = operation == "rent",
    price = price.amount,
    currency = price.currency.toCurrency(),
    district = location.district,
    department = location.department,
    coverImageUrl = coverImage ?: images.firstOrNull(),
    publishedAtMillis = publishedAt.isoToEpochMillis(),
)

internal fun OwnerProjectDto.toDomain() = OwnerProject(
    id = id,
    slug = slug,
    name = name,
    status = runCatching { ProjectStatus.valueOf(status) }.getOrDefault(ProjectStatus.DRAFT),
    priceFrom = priceFrom,
    currency = currency.toCurrency(),
    lotsCount = lotsCount,
    district = location?.district.orEmpty(),
    coverImageUrl = coverImage ?: images.firstOrNull(),
    publishedAtMillis = publishedAt.isoToEpochMillis(),
)

internal fun LeadDto.toDomain() = OwnerLead(
    id = id,
    name = name,
    email = email,
    phone = phone.orEmpty(),
    message = message.orEmpty(),
    source = source,
    createdAtMillis = createdAt.isoToEpochMillis(),
    targetId = property?.id ?: project?.id,
    targetTitle = property?.title ?: project?.name ?: "",
)

internal fun PlanDto.toDomain() = Plan(
    id = id,
    name = name,
    price = price.contentOrNull?.toDoubleOrNull() ?: 0.0,
    currency = currency.toCurrency(),
    durationDays = durationDays,
    propertyLimit = propertyLimit,
    projectLimit = projectLimit,
    features = features,
    isFree = isFree,
    highlight = highlight,
)

internal fun SubscriptionDto.toDomain() = Subscription(
    id = id,
    status = runCatching { SubscriptionStatus.valueOf(status) }.getOrDefault(SubscriptionStatus.PENDING_PAYMENT),
    planName = plan?.name.orEmpty(),
    rejectionReason = rejectionReason?.takeIf { it.isNotBlank() },
    expiresAtMillis = expiresAt.isoToEpochMillis(),
)

internal fun PropertyEditDto.toDraft() = PropertyDraft(
    operation = operationType,
    type = propertyType,
    title = title,
    description = description,
    price = price.toPlain(),
    currency = currency.toCurrency(),
    negotiable = negotiable,
    maintenanceFee = maintenanceFee?.toPlain().orEmpty(),
    department = location?.department.orEmpty(),
    province = location?.province.orEmpty(),
    district = location?.district.orEmpty(),
    address = location?.address.orEmpty(),
    reference = location?.reference.orEmpty(),
    approximateLocation = location?.approximateLocation ?: true,
    totalArea = details?.totalArea?.toPlain().orEmpty(),
    constructedArea = details?.constructedArea?.toPlain().orEmpty(),
    bedrooms = details?.bedrooms?.toString().orEmpty(),
    bathrooms = details?.bathrooms?.toString().orEmpty(),
    parkingSpaces = details?.parkingSpaces?.toString().orEmpty(),
    amenities = amenityCodes.toSet(),
    photos = media.map { it.url },
    contactName = contactName.orEmpty(),
    contactEmail = contactEmail.orEmpty(),
    contactPhone = contactPhone.orEmpty(),
)

/** 120.0 -> "120"; 120.5 -> "120.5" (lo que el usuario escribiría). */
internal fun Double.toPlain(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()

/**
 * Cuerpo para crear o editar. Las fotos nuevas ya son URLs de Cloudinary (la primera es la portada).
 * El `publicId` de las fotos existentes no se conserva: el backend solo exige la URL.
 */
internal fun PropertyDraft.toBody() = PropertyBody(
    operationType = operation,
    propertyType = type,
    title = title.trim(),
    description = description.trim(),
    price = price.toDoubleOrNull() ?: 0.0,
    currency = currency.name,
    negotiable = negotiable,
    maintenanceFee = maintenanceFee.toDoubleOrNull()?.takeIf { it > 0 },
    location = PropertyLocationBody(
        department = department.trim(),
        province = province.trim(),
        district = district.trim(),
        address = address.trim(),
        reference = reference.trim().ifEmpty { null },
        approximateLocation = approximateLocation,
    ),
    details = PropertyDetailsBody(
        totalArea = totalArea.toDoubleOrNull()?.takeIf { it >= 1 },
        constructedArea = constructedArea.toDoubleOrNull()?.takeIf { it >= 1 },
        bedrooms = bedrooms.toIntOrNull(),
        bathrooms = bathrooms.toIntOrNull(),
        parkingSpaces = parkingSpaces.toIntOrNull(),
    ),
    amenityCodes = amenities.toList(),
    media = photos.mapIndexed { index, url -> PropertyMediaBody(url = url, position = index, isCover = index == 0) },
    contactName = contactName.trim().ifEmpty { null },
    contactEmail = contactEmail.trim().ifEmpty { null },
    contactPhone = contactPhone.trim().ifEmpty { null },
)

internal fun ProjectDraft.toBody() = ProjectBody(
    category = category,
    stage = stage,
    name = name.trim(),
    description = description.trim(),
    currency = currency,
    priceFrom = priceFrom.toDouble(),
    priceTo = priceTo.toDouble(),
    location = ProjectLocationBody(
        department = department.trim(),
        province = province.trim(),
        district = district.trim(),
        zone = zone.trim().ifBlank { null },
        street = street.trim().ifBlank { null },
        latitude = latitude.toDoubleOrNull(),
        longitude = longitude.toDoubleOrNull(),
    ),
    lotMode = "INDIVIDUAL",
    lots = if (managedByPlan || category == "DEPARTAMENTOS") emptyList() else lots.filter { it != LotDraft() }.map {
        ProjectLotBody(
            lotNumber = it.number.trim(),
            block = it.block.trim().ifBlank { null },
            areaM2 = it.area.toDouble(),
            price = it.price.toDouble(),
            status = it.status.ifBlank { "AVAILABLE" },
            frontM = it.frontM.toDoubleOrNull(),
            planLocation = it.planLocation.trim().ifBlank { null },
        )
    },
    estimatedDelivery = estimatedDelivery.trim(),
    media = photos.mapIndexed { index, url -> ProjectMediaBody(type = "IMAGE", url = url, position = index, isCover = index == 0) },
    contactName = contactName.trim().ifBlank { null },
    contactEmail = contactEmail.trim().ifBlank { null },
    contactPhone = contactPhone.trim().ifBlank { null },
    contactWhatsapp = contactWhatsapp.trim().ifBlank { null },
)
