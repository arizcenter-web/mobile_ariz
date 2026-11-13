package com.tecsup.app_ariz.feature.listings.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.core.ui.format.publishedFullLabel
import com.tecsup.app_ariz.feature.listings.R
import com.tecsup.app_ariz.feature.listings.domain.model.AgentType
import com.tecsup.app_ariz.feature.listings.domain.model.ConservationStatus
import com.tecsup.app_ariz.feature.listings.domain.model.PropertyDetail
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private const val DescriptionMaxWords = 100
private val SectionTitle: @Composable () -> androidx.compose.ui.text.TextStyle = {
    MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Black)
}

/** Título, ubicación y fecha de publicación (`<header>` del web). */
@Composable
fun PropertyHeader(property: PropertyDetail, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = property.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Ink,
        )
        IconLine(Icons.Outlined.Place, property.location.line)
        publishedFullLabel(property.publishedAtMillis)?.let { IconLine(Icons.Outlined.AccessTime, it) }
    }
}

@Composable
private fun IconLine(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(16.dp), tint = ArizColors.Brand)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = ArizColors.Muted,
        )
    }
}

/** Precio grande y, si aplica, la cuota de mantenimiento. */
@Composable
fun PropertyPrice(property: PropertyDetail, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatPrice(property.price, property.currency),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            if (property.isRent) {
                Text(
                    text = " " + stringResource(R.string.listings_per_month),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ArizColors.Muted,
                )
            }
        }
        property.maintenanceFee?.let {
            Text(
                text = stringResource(R.string.listings_maintenance, formatPrice(it, property.currency)),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ArizColors.Muted,
            )
        }
    }
}

private data class Spec(val icon: ImageVector, val text: String)

/** Franja de iconos entre dos líneas: área, dormitorios, baños, estacionamientos y antigüedad. */
@Composable
fun PropertySpecStrip(property: PropertyDetail, modifier: Modifier = Modifier) {
    val area = NumberFormat.getIntegerInstance(Locale("es", "PE"))
    val specs = buildList {
        property.totalArea?.let { add(Spec(Icons.Outlined.SquareFoot, "${area.format(it)} ${stringResource(R.string.listings_spec_total_area)}")) }
        property.constructedArea?.let { add(Spec(Icons.Outlined.Home, "${area.format(it)} ${stringResource(R.string.listings_spec_built_area)}")) }
        if (property.bedrooms > 0) add(Spec(Icons.Outlined.Hotel, "${property.bedrooms} ${stringResource(R.string.listings_spec_bedrooms)}"))
        if (property.bathrooms > 0) {
            val unit = stringResource(if (property.bathrooms == 1) R.string.listings_spec_bathroom else R.string.listings_spec_bathrooms)
            add(Spec(Icons.Outlined.Bathtub, "${property.bathrooms} $unit"))
        }
        if (property.halfBathrooms > 0) {
            val unit = stringResource(if (property.halfBathrooms == 1) R.string.listings_spec_half_bathroom else R.string.listings_spec_half_bathrooms)
            add(Spec(Icons.Outlined.Bathtub, "${property.halfBathrooms} $unit"))
        }
        if (property.parkingSpaces > 0) add(Spec(Icons.Outlined.DirectionsCar, "${property.parkingSpaces} ${stringResource(R.string.listings_spec_parking)}"))
        ageText(property.constructionYear)?.let { add(Spec(Icons.Outlined.CalendarMonth, it)) }
    }
    if (specs.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge))
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            specs.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth()) {
                    pair.forEach { spec ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(spec.icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = ArizColors.Ink)
                            Text(
                                text = spec.text,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ArizColors.Ink,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    if (pair.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge))
    }
}

@Composable
private fun ageText(constructionYear: Int?): String? {
    if (constructionYear == null || constructionYear <= 1800) return null
    val age = maxOf(0, Calendar.getInstance().get(Calendar.YEAR) - constructionYear)
    return when (age) {
        0 -> stringResource(R.string.listings_spec_brand_new)
        1 -> stringResource(R.string.listings_spec_age_one)
        else -> stringResource(R.string.listings_spec_age_many, age)
    }
}

/** Descripción con "Ver más" a partir de 100 palabras (`ExpandableText` del web). */
@Composable
fun PropertyDescription(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val normalized = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
    val words = normalized.split(Regex("\\s+"))
    val truncatable = words.size > DescriptionMaxWords
    val shown = if (truncatable && !expanded) words.take(DescriptionMaxWords).joinToString(" ") + "..." else normalized

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.listings_description),
            style = SectionTitle(),
            color = ArizColors.Brand,
        )
        Text(
            text = shown,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 24.sp),
            color = ArizColors.Muted,
        )
        if (truncatable) {
            Text(
                text = stringResource(if (expanded) R.string.listings_read_less else R.string.listings_read_more),
                modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Brand,
            )
        }
    }
}

/** "Características completas": generales, servicios y exteriores (los servicios se reparten por mitades, como en el web). */
@Composable
fun PropertyFeatures(property: PropertyDetail, modifier: Modifier = Modifier) {
    val area = NumberFormat.getIntegerInstance(Locale("es", "PE"))
    val general = buildList {
        property.totalArea?.let { add(R.string.listings_feature_total_area to area.format(it)) }
        property.constructedArea?.let { add(R.string.listings_feature_built_area to area.format(it)) }
        if (property.bedrooms > 0) add(R.string.listings_feature_bedrooms to property.bedrooms.toString())
        if (property.bathrooms > 0) add(R.string.listings_feature_bathrooms to property.bathrooms.toString())
        if (property.halfBathrooms > 0) add(R.string.listings_feature_half_bathrooms to property.halfBathrooms.toString())
        if (property.parkingSpaces > 0) add(R.string.listings_feature_parking to property.parkingSpaces.toString())
        property.constructionYear?.let { add(R.string.listings_feature_year to it.toString()) }
    }
    val half = (property.amenities.size + 1) / 2
    val services = property.amenities.take(half)
    val outdoors = property.amenities.drop(half)
    if (general.isEmpty() && property.conservation == null && property.amenities.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(stringResource(R.string.listings_features), style = SectionTitle(), color = ArizColors.Brand)

            FeatureGroup(title = stringResource(R.string.listings_features_general)) {
                general.forEach { (label, value) -> FeatureRow(stringResource(label), value) }
                property.conservation?.let { FeatureRow(stringResource(R.string.listings_feature_condition), stringResource(it.labelRes())) }
            }
            FeatureGroup(title = stringResource(R.string.listings_features_services)) {
                if (services.isEmpty()) EmptyNote(R.string.listings_features_no_services) else services.forEach { AmenityRow(it) }
            }
            FeatureGroup(title = stringResource(R.string.listings_features_outdoors)) {
                if (outdoors.isEmpty()) EmptyNote(R.string.listings_features_no_outdoors) else outdoors.forEach { AmenityRow(it) }
            }
        }
    }
}

@Composable
private fun FeatureGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp),
            color = ArizColors.Accent,
        )
        content()
    }
}

@Composable
private fun FeatureRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge.copy(alpha = 0.7f)))
    }
}

@Composable
private fun AmenityRow(name: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(14.dp), tint = ArizColors.Success)
        Text(name, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
    }
}

@Composable
private fun EmptyNote(@StringRes message: Int) {
    Text(stringResource(message), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
}

@StringRes
private fun ConservationStatus.labelRes(): Int = when (this) {
    ConservationStatus.NEW -> R.string.listings_condition_new
    ConservationStatus.EXCELLENT -> R.string.listings_condition_excellent
    ConservationStatus.GOOD -> R.string.listings_condition_good
    ConservationStatus.REMODELED -> R.string.listings_condition_remodeled
    ConservationStatus.TO_REMODEL -> R.string.listings_condition_to_remodel
    ConservationStatus.UNDER_CONSTRUCTION -> R.string.listings_condition_under_construction
}

/** Ubicación: dirección, botón "Abrir en Google Maps" y la nota de ubicación aproximada. */
@Composable
fun PropertyLocationSection(
    property: PropertyDetail,
    onOpenMapsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.listings_location), style = SectionTitle(), color = ArizColors.Brand)
            IconLine(Icons.Outlined.Place, property.location.line)
            if (property.location.hasCoordinates) {
                Surface(
                    onClick = onOpenMapsClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ArizColors.Edge),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.listings_open_maps),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ArizColors.Brand,
                        )
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp).size(14.dp),
                            tint = ArizColors.Brand,
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.listings_approximate_location),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ArizColors.Muted,
                )
            }
            if (property.location.address == null) {
                Text(
                    text = stringResource(R.string.listings_approximate_location_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArizColors.Muted,
                )
            }
        }
    }
}

/** Tarjeta del anunciante: inicial, nombre, tipo y sello "Verificado por ARIZ". */
@Composable
fun AdvertiserCard(property: PropertyDetail, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val advertiser = property.advertiser
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(56.dp)
                    .background(ArizColors.Brand, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = advertiser.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Accent,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = advertiser.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ArizColors.Ink,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(advertiser.type.labelRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArizColors.Muted,
                )
                if (advertiser.isVerified) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArizColors.Success)
                        Text(
                            text = stringResource(R.string.listings_advertiser_verified),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ArizColors.Success,
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.listings_advertiser_view_profile),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Ink,
            )
        }
    }
}

@StringRes
private fun AgentType.labelRes(): Int = when (this) {
    AgentType.REAL_ESTATE -> R.string.listings_agent_real_estate
    AgentType.AGENT, AgentType.OTHER -> R.string.listings_agent_agent
    AgentType.INDEPENDENT -> R.string.listings_agent_independent
    AgentType.INDIVIDUAL_OWNER -> R.string.listings_agent_owner
    AgentType.DEVELOPER -> R.string.listings_agent_developer
    AgentType.ARCHITECT -> R.string.listings_agent_architect
}
