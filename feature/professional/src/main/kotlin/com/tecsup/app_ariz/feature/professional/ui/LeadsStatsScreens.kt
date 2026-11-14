package com.tecsup.app_ariz.feature.professional.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelEmpty
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.core.ui.format.publishedLabel
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.PropertyStatus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun LeadsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: LeadsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    PanelPage(
        eyebrow = stringResource(R.string.pro_panel_eyebrow),
        title = stringResource(R.string.pro_leads_title),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.pro_back),
        modifier = modifier,
    ) {
        when {
            state.isLoading -> PanelLoading()
            state.error != null && state.items.isEmpty() -> PanelError(state.error!!.message, stringResource(R.string.pro_retry), onRetry = viewModel::load)
            state.items.isEmpty() -> PanelEmpty(stringResource(R.string.pro_leads_empty), stringResource(R.string.pro_leads_empty_text))
            else -> state.items.forEach { lead ->
                PanelCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(lead.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink, modifier = Modifier.weight(1f))
                        StatusPill(stringResource(sourceLabelRes(lead.source)), ArizColors.Brand)
                    }
                    if (lead.targetTitle.isNotBlank()) Text(lead.targetTitle, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Brand)
                    if (lead.message.isNotBlank()) Text(lead.message, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (lead.email.isNotBlank()) {
                            Text(
                                lead.email,
                                modifier = Modifier.clickable { uriHandler.openUri("mailto:${lead.email}") },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = ArizColors.Brand,
                            )
                        }
                        if (lead.phone.isNotBlank()) {
                            Text(
                                lead.phone,
                                modifier = Modifier.clickable { uriHandler.openUri("tel:${lead.phone}") },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = ArizColors.Brand,
                            )
                        }
                    }
                    publishedLabel(lead.createdAtMillis)?.let { Text(it.relative, style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted) }
                }
            }
        }
    }
}

/** Estadísticas del web: tendencia de 14 días, propiedades por estado, origen de consultas y actividad. */
@Composable
internal fun StatsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: PanelViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats = state.data?.stats
    PanelPage(
        eyebrow = stringResource(R.string.pro_stats_eyebrow),
        title = stringResource(R.string.pro_stats_title),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.pro_back),
        modifier = modifier,
    ) {
        when {
            state.isLoading -> PanelLoading()
            stats == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.pro_retry), onRetry = viewModel::load)
            else -> {
                PanelCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.pro_stats_trend), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                        StatusPill(stringResource(R.string.pro_stats_leads_in_trend, stats.leadsInTrend), ArizColors.Brand)
                    }
                    Bars(stats.leadsLast14Days.map { it.count }, stats.leadsLast14Days.map { dayLabel(it.dayStartMillis) })
                }
                PanelCard {
                    Text(stringResource(R.string.pro_stats_status), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                    if (stats.publishedCount + stats.pausedCount + stats.otherCount == 0) {
                        Text(stringResource(R.string.pro_stats_none), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                    } else {
                        Meter(stringResource(R.string.pro_status_published), stats.publishedCount, stats.publishedCount + stats.pausedCount + stats.otherCount, PropertyStatus.PUBLISHED.color())
                        Meter(stringResource(R.string.pro_status_paused), stats.pausedCount, stats.publishedCount + stats.pausedCount + stats.otherCount, PropertyStatus.PAUSED.color())
                        Meter("Otras", stats.otherCount, stats.publishedCount + stats.pausedCount + stats.otherCount, ArizColors.Muted)
                    }
                }
                PanelCard {
                    Text(stringResource(R.string.pro_stats_sources), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                    if (stats.leadsBySource.isEmpty()) {
                        Text(stringResource(R.string.pro_stats_none), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                    } else {
                        stats.leadsBySource.forEach { Meter(stringResource(sourceLabelRes(it.source)), it.count, stats.totalLeads, ArizColors.Brand) }
                    }
                }
                PanelCard {
                    Text(stringResource(R.string.pro_stats_top), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                    val top = stats.topProperties.take(5).filter { it.leads > 0 }
                    if (top.isEmpty()) {
                        Text(stringResource(R.string.pro_stats_none), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                    } else {
                        top.forEach { Meter(it.title, it.leads, top.first().leads, ArizColors.Accent) }
                    }
                }
            }
        }
    }
}

private fun dayLabel(millis: Long): String = SimpleDateFormat("d", Locale("es", "PE")).format(millis)

/** Barras verticales simples: una por día, con su número de día debajo. */
@Composable
private fun Bars(values: List<Int>, labels: List<String>) {
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { index, value ->
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(maxOf(2f, 80f * value / max).dp)
                        .background(if (value > 0) ArizColors.Brand else ArizColors.Edge, RoundedCornerShape(3.dp)),
                )
                Text(labels[index], style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = ArizColors.Muted)
            }
        }
    }
}

/** Barra horizontal con etiqueta y cantidad (donut y ranking del web, en versión móvil). */
@Composable
private fun Meter(label: String, value: Int, total: Int, color: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Ink, maxLines = 1, modifier = Modifier.weight(1f))
            Text(value.toString(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
        }
        Box(Modifier.fillMaxWidth().height(8.dp).background(ArizColors.Edge, RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxWidth(if (total == 0) 0f else (value.toFloat() / total).coerceIn(0f, 1f)).height(8.dp).background(color, RoundedCornerShape(4.dp)))
        }
    }
}
