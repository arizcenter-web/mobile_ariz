package com.tecsup.app_ariz.feature.projects.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.Straighten
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.ui.format.formatMonthYear
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.core.ui.format.publishedFullLabel
import com.tecsup.app_ariz.feature.projects.R
import com.tecsup.app_ariz.feature.projects.domain.model.LotMode
import com.tecsup.app_ariz.feature.projects.domain.model.LotStatus
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.ui.util.humanize
import java.text.NumberFormat
import java.util.Locale

private const val DescriptionMaxWords = 100
private const val LotsCollapsedCount = 8

@Composable
private fun sectionTitleStyle(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Black)

/** Etiqueta de etapa y, si aplica, "Destacado" (chips sobre la galería del web). */
@Composable
fun ProjectBadges(project: ProjectDetail, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(project.stage.fullLabelRes()),
            modifier = Modifier.clip(RoundedCornerShape(50)).background(ArizColors.Brand).padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
            color = ArizColors.Accent,
        )
        if (project.featured) {
            Text(
                text = stringResource(R.string.projects_card_featured),
                modifier = Modifier.clip(RoundedCornerShape(50)).background(ArizColors.Accent).padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                color = ArizColors.Brand,
            )
        }
    }
}

@Composable
fun ProjectHeader(project: ProjectDetail, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = project.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Ink,
        )
        IconLine(Icons.Outlined.Place, project.location.line)
        publishedFullLabel(project.publishedAtMillis)?.let { IconLine(Icons.Outlined.AccessTime, it) }
    }
}

@Composable
private fun IconLine(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(16.dp), tint = ArizColors.Brand)
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Muted)
    }
}

/** Rango de precio ("S/ 50,000 - S/ 80,000") y cuotas, si las hay. */
@Composable
fun ProjectPrice(project: ProjectDetail, modifier: Modifier = Modifier) {
    val from = formatPrice(project.priceFrom, project.currency)
    val range = if (project.priceTo > 0 && project.priceTo != project.priceFrom) {
        "$from - ${formatPrice(project.priceTo, project.currency)}"
    } else {
        from
    }
    val installments = listOfNotNull(
        project.downPayment?.let { stringResource(R.string.projects_detail_down_payment, formatPrice(it, project.currency)) },
        project.monthlyPayment?.let { stringResource(R.string.projects_detail_monthly_payment, formatPrice(it, project.currency)) },
    ).joinToString(" · ")

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = range,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.Black),
            color = ArizColors.Brand,
        )
        if (installments.isNotEmpty()) {
            Text(installments, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Muted)
        }
    }
}

private data class Spec(val icon: ImageVector, val text: String)

/** Franja de iconos: área, lotes o departamentos, edificios o pavimentos, inicio y entrega. */
@Composable
fun ProjectSpecStrip(project: ProjectDetail, modifier: Modifier = Modifier) {
    val area = NumberFormat.getIntegerInstance(Locale("es", "PE"))
    val specs = buildList {
        if (project.minAreaM2 > 0) {
            val min = area.format(project.minAreaM2)
            val text = if (project.maxAreaM2 > 0 && project.maxAreaM2 != project.minAreaM2) {
                "$min - ${area.format(project.maxAreaM2)} m²"
            } else {
                "$min m²"
            }
            add(Spec(Icons.Outlined.SquareFoot, text))
        }
        project.totalLots?.let {
            val res = when {
                project.isApartments && it == 1 -> R.string.projects_spec_apartments_one
                project.isApartments -> R.string.projects_spec_apartments_many
                it == 1 -> R.string.projects_spec_lots_one
                else -> R.string.projects_spec_lots_many
            }
            add(Spec(Icons.Outlined.Straighten, if (it == 1) stringResource(res) else stringResource(res, it)))
        }
        project.totalBlocks?.let {
            val res = when {
                project.isApartments && it == 1 -> R.string.projects_spec_buildings_one
                project.isApartments -> R.string.projects_spec_buildings_many
                it == 1 -> R.string.projects_spec_floors_one
                else -> R.string.projects_spec_floors_many
            }
            add(Spec(Icons.Outlined.Apartment, if (it == 1) stringResource(res) else stringResource(res, it)))
        }
        formatMonthYear(project.constructionStartMillis)?.let {
            add(Spec(Icons.Outlined.CalendarMonth, stringResource(R.string.projects_spec_start, it)))
        }
        formatMonthYear(project.estimatedDeliveryMillis)?.let {
            add(Spec(Icons.Outlined.CheckCircle, stringResource(R.string.projects_spec_delivery, it)))
        }
    }
    if (specs.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge))
        Column(modifier = Modifier.padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
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
fun ProjectDescription(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val normalized = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
    val words = normalized.split(Regex("\\s+"))
    val truncatable = words.size > DescriptionMaxWords
    val shown = if (truncatable && !expanded) words.take(DescriptionMaxWords).joinToString(" ") + "..." else normalized

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.projects_description), style = sectionTitleStyle(), color = ArizColors.Brand)
        Text(shown, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 24.sp), color = ArizColors.Muted)
        if (truncatable) {
            Text(
                text = stringResource(if (expanded) R.string.projects_read_less else R.string.projects_read_more),
                modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Brand,
            )
        }
    }
}

/** "Características completas": datos generales, amenidades, servicios, certificaciones y financiamiento. */
@Composable
fun ProjectFeatures(project: ProjectDetail, modifier: Modifier = Modifier) {
    val rows = buildList {
        add(
            R.string.projects_row_type to stringResource(
                if (project.lotMode == LotMode.GROUP) R.string.projects_lot_mode_group else R.string.projects_lot_mode_individual,
            ),
        )
        add(R.string.projects_row_stage to stringResource(project.stage.fullLabelRes()))
        if (project.developer.isNotBlank()) add(R.string.projects_row_developer to project.developer)
        project.totalBlocks?.let {
            add((if (project.isApartments) R.string.projects_row_buildings else R.string.projects_row_floors) to it.toString())
        }
        project.totalLots?.let {
            add((if (project.isApartments) R.string.projects_row_total_apartments else R.string.projects_row_total_lots) to it.toString())
        }
        formatMonthYear(project.constructionStartMillis)?.let { add(R.string.projects_row_start to it) }
        formatMonthYear(project.estimatedDeliveryMillis)?.let { add(R.string.projects_row_delivery to it) }
        project.downPayment?.let { add(R.string.projects_row_down_payment to formatPrice(it, project.currency)) }
        project.monthlyPayment?.let { add(R.string.projects_row_monthly_payment to formatPrice(it, project.currency)) }
        project.urbanizationName?.let { add(R.string.projects_row_urbanization to it) }
        project.registryEntry?.let { add(R.string.projects_row_registry to it) }
        project.ruc?.let { add(R.string.projects_row_ruc to it) }
    }
    // Como en el web: las amenidades se reparten por mitades entre "Amenidades" y "Servicios".
    val half = (project.amenities.size + 1) / 2
    val amenitiesA = project.amenities.take(half)
    val servicesList = project.amenities.drop(half) + project.services

    Card(modifier) {
        Text(stringResource(R.string.projects_features), style = sectionTitleStyle(), color = ArizColors.Brand)
        FeatureGroup(stringResource(R.string.projects_features_general)) {
            rows.forEach { (label, value) -> FeatureRow(stringResource(label), value) }
        }
        FeatureGroup(stringResource(R.string.projects_features_amenities)) {
            if (amenitiesA.isEmpty()) {
                Text(stringResource(R.string.projects_features_no_amenities), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
            } else {
                amenitiesA.forEach { CheckRow(it.humanize()) }
            }
        }
        if (servicesList.isNotEmpty()) {
            FeatureGroup(stringResource(R.string.projects_features_services)) { servicesList.forEach { CheckRow(it.humanize()) } }
        }
        if (project.certifications.isNotEmpty()) {
            FeatureGroup(stringResource(R.string.projects_features_certifications)) { project.certifications.forEach { CheckRow(it.humanize()) } }
        }
        if (project.financing.isNotEmpty()) {
            FeatureGroup(stringResource(R.string.projects_features_financing)) { project.financing.forEach { CheckRow(it.humanize()) } }
        }
    }
}

@Composable
private fun Card(modifier: Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) { content() }
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
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink, textAlign = TextAlign.End)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge.copy(alpha = 0.7f)))
    }
}

@Composable
private fun CheckRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(14.dp), tint = ArizColors.Success)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
    }
}

/** Lotes (o departamentos) del proyecto con su estado; se muestran los primeros y el resto con "Ver todos". */
@Composable
fun ProjectLots(project: ProjectDetail, modifier: Modifier = Modifier) {
    if (project.lots.isEmpty()) return
    var showAll by rememberSaveable { mutableStateOf(false) }
    val shown = if (showAll) project.lots else project.lots.take(LotsCollapsedCount)

    Card(modifier) {
        Text(
            text = stringResource(if (project.isApartments) R.string.projects_apartments_title else R.string.projects_lots_title),
            style = sectionTitleStyle(),
            color = ArizColors.Brand,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            shown.forEach { lot ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = buildString {
                                    append(stringResource(R.string.projects_lot_number, lot.number))
                                    lot.block?.let { append(" · ").append(stringResource(R.string.projects_lot_block, it)) }
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ArizColors.Ink,
                            )
                            Text(
                                text = "${NumberFormat.getIntegerInstance(Locale("es", "PE")).format(lot.areaM2)} m² · " +
                                    formatPrice(lot.price, project.currency),
                                style = MaterialTheme.typography.bodySmall,
                                color = ArizColors.Muted,
                            )
                        }
                        LotStatusChip(lot.status)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge.copy(alpha = 0.6f)))
                }
            }
        }
        if (project.lots.size > LotsCollapsedCount) {
            Text(
                text = if (showAll) stringResource(R.string.projects_lots_show_less) else stringResource(R.string.projects_lots_show_all, project.lots.size),
                modifier = Modifier.clickable { showAll = !showAll }.padding(vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Brand,
            )
        }
    }
}

@Composable
private fun LotStatusChip(status: LotStatus) {
    val (label, color) = when (status) {
        LotStatus.AVAILABLE -> R.string.projects_lot_available to ArizColors.Success
        LotStatus.RESERVED -> R.string.projects_lot_reserved to ArizColors.Warning
        LotStatus.SOLD -> R.string.projects_lot_sold to ArizColors.Muted
        LotStatus.BLOCKED -> R.string.projects_lot_blocked to ArizColors.Muted
    }
    Text(
        text = stringResource(label),
        modifier = Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        color = color,
    )
}

@Composable
fun ProjectMilestones(project: ProjectDetail, modifier: Modifier = Modifier) {
    if (project.milestones.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.projects_milestones), style = sectionTitleStyle(), color = ArizColors.Brand)
        project.milestones.forEach { milestone ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ArizColors.Edge),
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(16.dp), tint = ArizColors.Brand)
                    Column {
                        Text(milestone.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                        formatMonthYear(milestone.dateMillis)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted) }
                        milestone.note?.let { Text(it, modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted) }
                    }
                }
            }
        }
    }
}

/** Planos (enlaces a abrir) y renders (fotos deslizables). */
@Composable
fun ProjectMedia(project: ProjectDetail, onOpenUrl: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (project.planUrls.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.projects_plans), style = sectionTitleStyle(), color = ArizColors.Brand)
                project.planUrls.forEach { url ->
                    Surface(
                        onClick = { onOpenUrl(url) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, ArizColors.Edge),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(stringResource(R.string.projects_plan_view), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArizColors.Brand)
                        }
                    }
                }
            }
        }
        if (project.renderUrls.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.projects_renders), style = sectionTitleStyle(), color = ArizColors.Brand)
                LazyRow(contentPadding = PaddingValues(end = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(project.renderUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.width(260.dp).height(170.dp).clip(RoundedCornerShape(12.dp)).background(ArizColors.SurfaceMuted),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectLocationSection(project: ProjectDetail, onOpenMapsClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier) {
        Text(stringResource(R.string.projects_location), style = sectionTitleStyle(), color = ArizColors.Brand)
        IconLine(Icons.Outlined.Place, project.location.line)
        if (project.location.hasCoordinates) {
            Surface(
                onClick = onOpenMapsClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ArizColors.Edge),
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.projects_open_maps), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand)
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp).size(14.dp), tint = ArizColors.Brand)
                }
            }
        } else {
            Text(
                stringResource(R.string.projects_approximate_location),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ArizColors.Muted,
            )
        }
    }
}

@Composable
fun ProjectAdvertiserCard(project: ProjectDetail, modifier: Modifier = Modifier) {
    val advertiser = project.advertiser
    val typeRes = when {
        advertiser.isArizOfficial -> R.string.projects_advertiser_ariz
        advertiser.typeCode.contains("REAL_ESTATE_AGENCY") -> R.string.projects_advertiser_real_estate
        advertiser.typeCode == "DEVELOPER" -> R.string.projects_advertiser_developer
        advertiser.typeCode == "ARCHITECT" -> R.string.projects_advertiser_architect
        else -> R.string.projects_advertiser_agent
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.width(72.dp).height(56.dp).background(ArizColors.Brand, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = advertiser.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Accent,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(advertiser.name.uppercase(), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink, maxLines = 1)
                Text(stringResource(typeRes), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                if (advertiser.isVerified) {
                    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArizColors.Success)
                        Text(
                            stringResource(R.string.projects_advertiser_verified),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ArizColors.Success,
                        )
                    }
                }
            }
        }
    }
}

@StringRes
internal fun ProjectStage.fullLabelRes(): Int = when (this) {
    ProjectStage.PRE_SALE -> R.string.projects_stage_full_pre_sale
    ProjectStage.UNDER_CONSTRUCTION -> R.string.projects_stage_full_under_construction
    ProjectStage.DELIVERED -> R.string.projects_stage_full_delivered
}
