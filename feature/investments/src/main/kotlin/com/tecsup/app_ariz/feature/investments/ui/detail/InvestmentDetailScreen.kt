package com.tecsup.app_ariz.feature.investments.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.common.ArizContact
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.component.PhotoGallery
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.feature.investments.R
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.ui.labelRes

@Composable
fun InvestmentDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvestmentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LightStatusBarIcons()
    InvestmentDetailContent(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, modifier = modifier)
}

/** Contenido sin estado: se previsualiza y se prueba pasándole un [InvestmentDetailUiState]. */
@Composable
fun InvestmentDetailContent(
    state: InvestmentDetailUiState,
    onAction: (InvestmentDetailAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(ArizColors.SectionCanvas)) {
        val opportunity = state.opportunity
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArizColors.Brand)
            }

            opportunity == null -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            ) {
                Text(state.error?.message.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
                ArizPrimaryButton(text = stringResource(R.string.investments_retry), onClick = { onAction(InvestmentDetailAction.Retry) })
            }

            else -> Body(opportunity)
        }
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.statusBarsPadding().padding(start = 8.dp, top = 8.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.investments_back), tint = Color.White)
        }
    }
}

@Composable
private fun Body(opportunity: InvestmentOpportunity) {
    val uriHandler = LocalUriHandler.current
    val whatsappMessage = stringResource(R.string.investments_detail_whatsapp, opportunity.name)
    val images = listOfNotNull(opportunity.coverImageUrl) + opportunity.gallery

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PhotoGallery(images = images, emptyLabel = "")
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.investments_detail_eyebrow, stringResource(opportunity.goal.labelRes())).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp),
                    color = ArizColors.Accent,
                )
                Text(
                    text = opportunity.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(opportunity.typeName, ArizColors.Brand.copy(alpha = 0.1f), ArizColors.Brand)
                    Tag(stringResource(opportunity.status.labelRes()), ArizColors.Accent, ArizColors.OnAccent)
                    opportunity.risk?.let {
                        Tag(stringResource(R.string.investments_risk_label, stringResource(it.labelRes())), ArizColors.SurfaceMuted, ArizColors.Muted)
                    }
                }
                Text(opportunity.location, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Muted)
                Text(opportunity.summary, style = MaterialTheme.typography.bodyLarge, color = ArizColors.Ink)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metric(stringResource(R.string.investments_metric_initial_investment), formatPrice(opportunity.initialInvestment, Currency.PEN), Modifier.weight(1f))
                Metric(stringResource(R.string.investments_metric_roi), "${opportunity.roiPercent.toInt()}%", Modifier.weight(1f))
            }
            Metric(stringResource(R.string.investments_metric_area), opportunity.areaLabel, Modifier.fillMaxWidth())

            if (opportunity.description.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.investments_detail_about),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    Text(opportunity.description, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp), color = ArizColors.Muted)
                }
            }

            if (opportunity.highlights.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ArizColors.Edge),
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.investments_detail_highlights),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = ArizColors.Brand,
                        )
                        opportunity.highlights.forEach { highlight ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.padding(top = 8.dp).size(8.dp).background(ArizColors.Accent, CircleShape))
                                Text(highlight, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Ink)
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ArizColors.Edge),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.investments_detail_location),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    Text("${opportunity.district}, ${opportunity.province}, ${opportunity.department}", style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
                    if (opportunity.hasCoordinates) {
                        Text(
                            text = stringResource(R.string.investments_detail_open_maps),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { uriHandler.openUri("https://www.google.com/maps?q=${opportunity.latitude},${opportunity.longitude}") },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ArizColors.Brand,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().background(ArizColors.Brand, RoundedCornerShape(16.dp)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.investments_detail_more),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.75f),
                )
                Surface(
                    onClick = { uriHandler.openUri(ArizContact.whatsappUrl(whatsappMessage)) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFF25D366),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = opportunity.ctaLabel.ifBlank { stringResource(R.string.investments_detail_cta_default) },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = Color.White,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.investments_detail_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
            Box(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun Tag(text: String, container: Color, content: Color) {
    Text(
        text = text,
        modifier = Modifier.background(container, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
        color = content,
    )
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Muted)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
        }
    }
}


