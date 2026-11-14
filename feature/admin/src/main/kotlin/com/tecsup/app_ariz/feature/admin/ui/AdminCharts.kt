package com.tecsup.app_ariz.feature.admin.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.feature.admin.R
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsView
import com.tecsup.app_ariz.feature.admin.domain.MonthPoint
import com.tecsup.app_ariz.feature.admin.domain.ProfileFilter
import com.tecsup.app_ariz.feature.admin.domain.ProfileSlice
import com.tecsup.app_ariz.feature.admin.domain.RangeFilter
import com.tecsup.app_ariz.feature.admin.domain.TopProject

private val Navy = Color(0xFF00124F)
private val Blue = Color(0xFF4774BE)
private val Gold = Color(0xFFF2A900)
private val Soft = Color(0xFF94A5C9)

/** Filtros + gráficos del dashboard (mismos que el web): tendencias, evolución, perfiles y top por lotes. */
@Composable
fun AnalyticsSection(
    state: AnalyticsUiState,
    onSelect: (ProfileFilter?, RangeFilter?) -> Unit,
    onRetry: () -> Unit,
) {
    Text(stringResource(R.string.adm_charts_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RangeFilter.entries.forEach { range ->
            FilterChip(selected = state.range == range, onClick = { onSelect(null, range) }, label = { Text(rangeLabel(range)) })
        }
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileFilter.entries.forEach { profile ->
            FilterChip(selected = state.profile == profile, onClick = { onSelect(profile, null) }, label = { Text(profileLabel(profile)) })
        }
    }
    val view = state.view
    when {
        state.isLoading && view == null -> Text(stringResource(R.string.adm_charts_loading), color = ArizColors.Muted, style = MaterialTheme.typography.bodyMedium)
        view == null -> Text(
            stringResource(R.string.adm_charts_error),
            color = ArizColors.Brand,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.clickable(onClick = onRetry),
        )
        else -> Charts(view)
    }
}

@Composable
private fun CardTitle(text: String) =
    Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand)

@Composable
private fun Charts(view: AnalyticsView) {
    PanelCard {
        CardTitle(stringResource(R.string.adm_chart_overview))
        ColumnChart(
            categories = view.accounts.map { it.label },
            series = listOf(
                ChartSeries(stringResource(R.string.adm_series_accounts), Navy, view.accounts.map { it.value }),
                ChartSeries(stringResource(R.string.adm_series_properties), Blue, view.properties.map { it.value }),
                ChartSeries(stringResource(R.string.adm_series_projects), Gold, view.projects.map { it.value }),
                ChartSeries(stringResource(R.string.adm_series_memberships), Soft, view.memberships.map { it.value }),
            ),
        )
    }
    PanelCard {
        CardTitle(stringResource(R.string.adm_chart_publications))
        LineChart(view.publications, Navy)
    }
    PanelCard {
        CardTitle(stringResource(R.string.adm_chart_distribution))
        DistributionDonut(view.distribution)
    }
    PanelCard {
        CardTitle(stringResource(R.string.adm_chart_top_projects))
        TopProjects(view.topProjects)
    }
}

@Composable
private fun rangeLabel(range: RangeFilter) = stringResource(
    when (range) {
        RangeFilter.ONE -> R.string.adm_range_1m
        RangeFilter.SIX -> R.string.adm_range_6m
        RangeFilter.TWELVE -> R.string.adm_range_12m
    },
)

@Composable
private fun profileLabel(profile: ProfileFilter) = stringResource(
    when (profile) {
        ProfileFilter.ALL -> R.string.adm_profile_all
        ProfileFilter.AGENCIES -> R.string.adm_profile_agencies
        ProfileFilter.AGENTS -> R.string.adm_profile_agents
        ProfileFilter.USERS -> R.string.adm_profile_users
    },
)

internal data class ChartSeries(val label: String, val color: Color, val values: List<Int>)

@Composable
private fun Legend(items: List<Pair<String, Color>>) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(10.dp).background(color, CircleShape))
                Text(label, style = MaterialTheme.typography.labelMedium, color = ArizColors.Muted)
            }
        }
    }
}

@Composable
private fun AxisLabels(top: Int, mid: Int?, height: Int) {
    Column(Modifier.height(height.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Text("$top", style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
        if (mid != null) Text("$mid", style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
        Text("0", style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
    }
}

@Composable
private fun CategoryLabels(labels: List<String>) {
    Row(Modifier.fillMaxWidth()) {
        labels.forEach {
            Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

/** Columnas agrupadas por mes. */
@Composable
internal fun ColumnChart(categories: List<String>, series: List<ChartSeries>) {
    val max = maxOf(1, series.maxOfOrNull { it.values.maxOrNull() ?: 0 } ?: 0)
    Legend(series.map { it.label to it.color })
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AxisLabels(max, (max + 1) / 2, 180)
        Column(Modifier.weight(1f)) {
            Canvas(Modifier.fillMaxWidth().height(180.dp)) {
                val grid = Color(0xFFE6E6E6)
                for (i in 0..2) drawLine(grid, Offset(0f, size.height * i / 2f), Offset(size.width, size.height * i / 2f), 1.dp.toPx())
                val slot = size.width / categories.size.coerceAtLeast(1)
                val barW = (slot * 0.8f / series.size.coerceAtLeast(1)).coerceAtMost(14.dp.toPx())
                val groupW = barW * series.size
                categories.indices.forEach { c ->
                    series.forEachIndexed { s, item ->
                        val value = item.values.getOrElse(c) { 0 }
                        val h = if (value == 0) 2.dp.toPx() else (value.toFloat() / max * size.height).coerceAtLeast(6.dp.toPx())
                        val x = c * slot + (slot - groupW) / 2f + s * barW
                        drawRoundRect(item.color, Offset(x, size.height - h), Size(barW * 0.85f, h), CornerRadius(barW / 3f))
                    }
                }
            }
            CategoryLabels(categories)
        }
    }
}

@Composable
internal fun LineChart(points: List<MonthPoint>, color: Color) {
    val max = maxOf(1, points.maxOfOrNull { it.value } ?: 0)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AxisLabels(max, null, 160)
        Column(Modifier.weight(1f)) {
            Canvas(Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 6.dp, vertical = 6.dp)) {
                val step = if (points.size > 1) size.width / (points.size - 1) else 0f
                val xy = points.mapIndexed { i, p ->
                    Offset(if (points.size > 1) i * step else size.width / 2f, size.height - p.value.toFloat() / max * size.height)
                }
                drawLine(Color(0xFFD9D9D9), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                if (xy.size > 1) {
                    val line = Path().apply { xy.forEachIndexed { i, o -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) } }
                    val area = Path().apply {
                        addPath(line)
                        lineTo(xy.last().x, size.height)
                        lineTo(xy.first().x, size.height)
                        close()
                    }
                    drawPath(area, color.copy(alpha = 0.12f))
                    drawPath(line, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
                xy.forEach { drawCircle(color, 4.dp.toPx(), it) }
            }
            CategoryLabels(points.map { it.label })
        }
    }
}

private fun sliceLabelRes(type: String) = when (type) {
    "REAL_ESTATE_AGENCY" -> R.string.adm_profile_agencies
    "INDEPENDENT_AGENT" -> R.string.adm_profile_agents
    else -> R.string.adm_profile_developers
}

private fun sliceColor(type: String) = when (type) {
    "REAL_ESTATE_AGENCY" -> Navy
    "INDEPENDENT_AGENT" -> Blue
    else -> Gold
}

/** Dona de perfiles; tocar un perfil de la leyenda lo oculta o lo muestra. */
@Composable
internal fun DistributionDonut(slices: List<ProfileSlice>) {
    var hidden by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val visible = slices.filter { it.type !in hidden }
    val total = visible.sumOf { it.count }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(120.dp)) {
                val stroke = 22.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                if (total == 0) {
                    drawArc(Color(0xFFE9EDF5), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                } else {
                    var start = -90f
                    visible.filter { it.count > 0 }.forEach { slice ->
                        val sweep = slice.count.toFloat() / total * 360f
                        drawArc(sliceColor(slice.type), start, sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                        start += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$total", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                Text(stringResource(R.string.adm_donut_center), style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.forEach { slice ->
                val active = slice.type !in hidden
                Row(
                    Modifier.fillMaxWidth().clickable { hidden = if (active) hidden + slice.type else hidden - slice.type },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.size(10.dp).background(if (active) sliceColor(slice.type) else Color(0xFFDCE1EB), CircleShape))
                    Text(stringResource(sliceLabelRes(slice.type)), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = if (active) ArizColors.Brand else ArizColors.Muted)
                    Text("${slice.count}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand)
                    Text(if (active && total > 0) "${Math.round(slice.count * 100f / total)}%" else "-", style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
                }
            }
            Text(stringResource(R.string.adm_donut_hint), style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
        }
    }
}

@Composable
internal fun TopProjects(items: List<TopProject>) {
    if (items.isEmpty()) {
        Text(stringResource(R.string.adm_top_empty), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
        return
    }
    val max = maxOf(1, items.maxOf { it.lots })
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(26.dp).background(ArizColors.Brand, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand, maxLines = 1)
                    Box(Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFE6E6E6), CircleShape)) {
                        Box(Modifier.fillMaxWidth((item.lots.toFloat() / max).coerceIn(0.04f, 1f)).height(8.dp).background(if (index % 2 == 0) Navy else Blue, CircleShape))
                    }
                }
                Text(stringResource(R.string.adm_top_lots, item.lots), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
            }
        }
    }
}
