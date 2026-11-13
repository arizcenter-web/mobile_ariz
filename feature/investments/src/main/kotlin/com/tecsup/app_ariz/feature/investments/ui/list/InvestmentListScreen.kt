package com.tecsup.app_ariz.feature.investments.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.common.ArizContact
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.feature.investments.R
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentGoal
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.ui.labelRes

@Composable
fun InvestmentListScreen(
    onBackClick: () -> Unit,
    onOpportunityClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvestmentListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LightStatusBarIcons()
    InvestmentListContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onOpportunityClick = onOpportunityClick,
        modifier = modifier,
    )
}

/** Contenido sin estado: se previsualiza y se prueba pasándole un [InvestmentListUiState]. */
@Composable
fun InvestmentListContent(
    state: InvestmentListUiState,
    onAction: (InvestmentListAction) -> Unit,
    onBackClick: () -> Unit,
    onOpportunityClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val advisorUrl = ArizContact.whatsappUrl(stringResource(R.string.investments_whatsapp_general))

    LazyColumn(
        modifier = modifier.fillMaxSize().background(ArizColors.SectionCanvas),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") { Hero(onBackClick = onBackClick, onAdvisorClick = { uriHandler.openUri(advisorUrl) }) }
        item(key = "simulator") {
            Simulator(state = state, onAction = onAction, modifier = Modifier.padding(horizontal = 16.dp))
        }
        item(key = "catalog-title") {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.investments_catalog_eyebrow),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp),
                    color = ArizColors.Accent,
                )
                Text(
                    text = stringResource(R.string.investments_catalog_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
            }
        }
        if (state.types.isNotEmpty()) item(key = "types") { TypeChips(state = state, onAction = onAction) }

        when {
            state.isLoading -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }
            }

            state.error != null -> item(key = "error") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.error.message, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
                    ArizPrimaryButton(text = stringResource(R.string.investments_retry), onClick = { onAction(InvestmentListAction.Retry) })
                }
            }

            state.visible.isEmpty() -> item(key = "empty") {
                Text(
                    text = stringResource(R.string.investments_empty),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArizColors.Muted,
                    textAlign = TextAlign.Center,
                )
            }

            else -> items(state.visible, key = { it.id }) { opportunity ->
                OpportunityCard(
                    opportunity = opportunity,
                    compatible = state.simulation.compatible.any { it.id == opportunity.id },
                    onClick = { onOpportunityClick(opportunity.slug) },
                    onContactClick = {
                        uriHandler.openUri(
                            ArizContact.whatsappUrl(
                                "Hola ARIZ Proyectos, quiero información de la oportunidad de inversión ${opportunity.name}",
                            ),
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        item(key = "nav-space") { Box(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun Hero(onBackClick: () -> Unit, onAdvisorClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(ArizColors.Brand).statusBarsPadding().padding(bottom = 28.dp)) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp).background(Color.White.copy(alpha = 0.15f), CircleShape),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.investments_back), tint = Color.White)
        }
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.investments_hero_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, lineHeight = 34.sp),
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.investments_hero_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            Surface(
                onClick = onAdvisorClick,
                modifier = Modifier.padding(top = 4.dp).height(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.investments_hero_advisor),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(4.dp).background(ArizColors.Accent))
    }
}

/** Calculadora de inversión del web: capital, plazo, objetivo y retorno estimado. */
@Composable
private fun Simulator(state: InvestmentListUiState, onAction: (InvestmentListAction) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(
                    text = stringResource(R.string.investments_sim_eyebrow),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp),
                    color = ArizColors.Accent,
                )
                Text(
                    text = stringResource(R.string.investments_sim_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
            }
            SimulatorSlider(
                label = stringResource(R.string.investments_sim_capital),
                value = formatPrice(state.capital.toDouble(), Currency.PEN),
                raw = state.capital,
                min = SimulatorDefaults.CAPITAL_MIN,
                max = SimulatorDefaults.CAPITAL_MAX,
                step = SimulatorDefaults.CAPITAL_STEP,
                onChange = { onAction(InvestmentListAction.CapitalChanged(it)) },
            )
            SimulatorSlider(
                label = stringResource(R.string.investments_sim_term),
                value = stringResource(R.string.investments_sim_months, state.termMonths),
                raw = state.termMonths,
                min = SimulatorDefaults.TERM_MIN,
                max = SimulatorDefaults.TERM_MAX,
                step = SimulatorDefaults.TERM_STEP,
                onChange = { onAction(InvestmentListAction.TermChanged(it)) },
            )
            Text(
                text = stringResource(R.string.investments_sim_goal),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InvestmentGoal.entries.forEach { goal ->
                    val selected = goal == state.goal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (selected) ArizColors.Accent else ArizColors.Canvas)
                            .clickable { onAction(InvestmentListAction.GoalChanged(goal)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(goal.labelRes()),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = if (selected) ArizColors.OnAccent else ArizColors.Brand,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().background(ArizColors.Brand, MaterialTheme.shapes.medium).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.investments_sim_result),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp),
                    color = ArizColors.Accent,
                )
                Text(
                    text = formatPrice(state.simulation.estimatedReturn.toDouble(), Currency.PEN),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.investments_sim_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun SimulatorSlider(label: String, value: String, raw: Int, min: Int, max: Int, step: Int, onChange: (Int) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
        Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
        Slider(
            value = raw.toFloat(),
            onValueChange = { onChange(((it / step).toInt() * step).coerceIn(min, max)) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min) / step - 1,
            colors = SliderDefaults.colors(thumbColor = ArizColors.Accent, activeTrackColor = ArizColors.Accent),
        )
    }
}

@Composable
private fun TypeChips(state: InvestmentListUiState, onAction: (InvestmentListAction) -> Unit) {
    val options: List<Pair<String?, String>> =
        listOf(null to stringResource(R.string.investments_types_all)) + state.types.map { it.id to it.name }
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options, key = { it.first ?: "all" }) { (id, label) ->
            val selected = id == state.selectedTypeId
            Text(
                text = label,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) ArizColors.Brand else MaterialTheme.colorScheme.surface)
                    .clickable { onAction(InvestmentListAction.TypeSelected(id)) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else ArizColors.Brand,
            )
        }
    }
}

@Composable
private fun OpportunityCard(
    opportunity: InvestmentOpportunity,
    compatible: Boolean,
    onClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF2A3B85), ArizColors.Brand))),
            ) {
                if (opportunity.coverImageUrl != null) {
                    AsyncImage(
                        model = opportunity.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.4f to Color.Transparent, 1f to ArizColors.Brand.copy(alpha = 0.75f)),
                    ),
                )
                Pill(opportunity.returnLabel, ArizColors.Accent, ArizColors.OnAccent, Modifier.align(Alignment.TopStart).padding(12.dp))
                Pill(
                    stringResource(opportunity.status.labelRes()),
                    Color.White.copy(alpha = 0.92f),
                    ArizColors.Brand,
                    Modifier.align(Alignment.TopEnd).padding(12.dp),
                )
                if (compatible) {
                    Pill(
                        stringResource(R.string.investments_compatible),
                        Color.White.copy(alpha = 0.92f),
                        ArizColors.Brand,
                        Modifier.align(Alignment.BottomStart).padding(12.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = opportunity.typeName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp),
                    color = ArizColors.Accent,
                )
                Text(
                    text = opportunity.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                    maxLines = 2,
                )
                Text(opportunity.location, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Muted, maxLines = 1)
                Text(opportunity.summary, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted, maxLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Metric(stringResource(R.string.investments_metric_area), opportunity.areaLabel, Modifier.weight(1f))
                    Metric(
                        stringResource(R.string.investments_metric_initial),
                        formatPrice(opportunity.initialInvestment, Currency.PEN),
                        Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Metric(
                        stringResource(R.string.investments_metric_term),
                        opportunity.termMonths?.let { stringResource(R.string.investments_term_months, it) } ?: stringResource(opportunity.goal.labelRes()),
                        Modifier.weight(1f),
                    )
                    Metric(
                        stringResource(if (opportunity.risk != null) R.string.investments_metric_risk else R.string.investments_metric_asset),
                        opportunity.risk?.let { stringResource(it.labelRes()) } ?: opportunity.assetType,
                        Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    ArizPrimaryButton(text = stringResource(R.string.investments_card_view), onClick = onClick, modifier = Modifier.weight(1f))
                    Surface(
                        onClick = onContactClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, ArizColors.Edge),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = opportunity.ctaLabel.ifBlank { stringResource(R.string.investments_card_contact) },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = ArizColors.Brand,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.clip(RoundedCornerShape(50)).background(container).padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
        color = content,
    )
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(ArizColors.Canvas, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = ArizColors.Muted)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand, maxLines = 1)
    }
}
