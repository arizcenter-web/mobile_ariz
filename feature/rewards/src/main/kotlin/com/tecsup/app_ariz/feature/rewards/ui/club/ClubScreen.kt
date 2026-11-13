package com.tecsup.app_ariz.feature.rewards.ui.club

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.format.publishedLabel
import com.tecsup.app_ariz.feature.rewards.R
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignPoints
import com.tecsup.app_ariz.feature.rewards.domain.model.Incentive
import com.tecsup.app_ariz.feature.rewards.domain.model.Redemption
import com.tecsup.app_ariz.feature.rewards.domain.model.RedemptionStatus
import com.tecsup.app_ariz.feature.rewards.domain.model.Referrer
import com.tecsup.app_ariz.feature.rewards.ui.referral.ReferralFormSheet
import java.text.NumberFormat
import java.util.Locale
import com.tecsup.app_ariz.core.designsystem.R as DesignSystemR

private val PointsFormat: NumberFormat get() = NumberFormat.getIntegerInstance(Locale("es", "PE"))

@Composable
fun ClubScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClubViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LightStatusBarIcons()
    ClubContent(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, modifier = modifier)

    if (state.showReferralForm) {
        ReferralFormSheet(referrer = state.referrer, onDismiss = { viewModel.onAction(ClubAction.ReferralFormClosed) })
    }
}

/** Contenido sin estado: se previsualiza y se prueba pasándole un [ClubUiState]. */
@Composable
fun ClubContent(
    state: ClubUiState,
    onAction: (ClubAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val referrer = state.referrer
    if (referrer == null) {
        CodeStep(state = state, onAction = onAction, onBackClick = onBackClick, modifier = modifier)
    } else {
        ClubStep(state = state, referrer = referrer, onAction = onAction, onBackClick = onBackClick, modifier = modifier)
    }
    RedeemDialogs(state = state, onAction = onAction)
}

/** Paso 1: foto de fondo con velo azul, texto del programa y la tarjeta para escribir el código. */
@Composable
private fun CodeStep(state: ClubUiState, onAction: (ClubAction) -> Unit, onBackClick: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(DesignSystemR.drawable.ariz_auth_background),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(modifier = Modifier.matchParentSize().background(ArizColors.HeroShade.copy(alpha = 0.78f)))
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding().verticalScroll(rememberScrollState())) {
            BackArrow(onClick = onBackClick)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArizColors.Accent)
                    Text(
                        text = stringResource(R.string.rewards_code_badge),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp),
                        color = Color.White,
                    )
                }
                Text(
                    text = stringResource(R.string.rewards_code_tagline),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                    color = ArizColors.Accent,
                )
                Text(
                    text = stringResource(R.string.rewards_code_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, lineHeight = 34.sp),
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.rewards_code_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.rewards_code_card_eyebrow),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp),
                        color = ArizColors.Brand,
                    )
                    Text(
                        text = stringResource(R.string.rewards_code_card_title),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = ArizColors.Ink,
                    )
                    Text(
                        text = stringResource(R.string.rewards_code_card_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArizColors.Muted,
                    )
                    ArizTextField(
                        value = state.codeInput,
                        onValueChange = { onAction(ClubAction.CodeChanged(it)) },
                        label = stringResource(R.string.rewards_code_label),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = stringResource(R.string.rewards_code_placeholder),
                        errorText = state.codeError,
                        enabled = !state.isValidating,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        onImeAction = { onAction(ClubAction.ValidateCode) },
                    )
                    ArizPrimaryButton(
                        text = stringResource(R.string.rewards_code_submit),
                        onClick = { onAction(ClubAction.ValidateCode) },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = state.isValidating,
                    )
                    Text(
                        text = stringResource(R.string.rewards_code_join),
                        modifier = Modifier.fillMaxWidth().clickable { onAction(ClubAction.ReferralFormOpened) }.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ArizColors.Brand,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.rewards_code_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = ArizColors.Muted,
                    )
                }
            }
            Box(Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

@Composable
private fun BackArrow(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp).background(Color.White.copy(alpha = 0.15f), CircleShape),
    ) {
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.rewards_back), tint = Color.White)
    }
}

/** Paso 2: puntos del referente, puntos de campaña, catálogo de premios y sus canjes. */
@Composable
private fun ClubStep(
    state: ClubUiState,
    referrer: Referrer,
    onAction: (ClubAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val shareText = stringResource(R.string.rewards_share_text, referrer.name, referrer.code)
    val shareTitle = stringResource(R.string.rewards_share_title)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(ArizColors.SectionCanvas),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") {
            Hero(
                referrer = referrer,
                points = state.points,
                campaignPoints = state.campaignPointsTotal,
                onBackClick = onBackClick,
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, shareTitle))
                },
                onRefer = { onAction(ClubAction.ReferralFormOpened) },
                onLeave = { onAction(ClubAction.LeaveClub) },
            )
        }
        item(key = "banner") { Banner(points = state.points, code = referrer.code, modifier = Modifier.padding(horizontal = 16.dp)) }

        when {
            state.isLoadingClub && state.incentives.isEmpty() -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }
            }

            state.clubError != null && state.incentives.isEmpty() -> item(key = "error") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ArizAlert(message = state.clubError.message)
                    ArizPrimaryButton(text = stringResource(R.string.rewards_retry), onClick = { onAction(ClubAction.Refresh) })
                }
            }

            else -> {
                item(key = "campaigns") { Campaigns(state.campaigns, Modifier.padding(horizontal = 16.dp)) }
                item(key = "catalog-title") {
                    SectionTitle(stringResource(R.string.rewards_catalog_title), Modifier.padding(horizontal = 16.dp))
                }
                if (state.incentives.isEmpty()) {
                    item(key = "catalog-empty") { EmptyNote(stringResource(R.string.rewards_catalog_empty)) }
                } else {
                    items(state.incentives, key = { it.id }) { incentive ->
                        IncentiveCard(
                            incentive = incentive,
                            points = state.points,
                            onRedeem = { onAction(ClubAction.RedeemRequested(incentive)) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                item(key = "redemptions-title") {
                    SectionTitle(stringResource(R.string.rewards_redemptions_title), Modifier.padding(horizontal = 16.dp))
                }
                if (state.redemptions.isEmpty()) {
                    item(key = "redemptions-empty") { EmptyNote(stringResource(R.string.rewards_redemptions_empty)) }
                } else {
                    items(state.redemptions, key = { it.id }) { redemption ->
                        RedemptionRow(redemption, Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(
    referrer: Referrer,
    points: Int,
    campaignPoints: Int,
    onBackClick: () -> Unit,
    onShare: () -> Unit,
    onRefer: () -> Unit,
    onLeave: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(ArizColors.Brand).statusBarsPadding().padding(bottom = 24.dp)) {
        BackArrow(onClick = onBackClick)
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.rewards_hero_greeting, referrer.name),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                text = stringResource(R.string.rewards_hero_points_label).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp),
                color = ArizColors.Accent,
            )
            Text(
                text = stringResource(R.string.rewards_hero_points, PointsFormat.format(points)),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = Color.White,
            )
            if (campaignPoints > 0) {
                Text(
                    text = stringResource(R.string.rewards_hero_campaign_points, PointsFormat.format(campaignPoints)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            Text(
                text = stringResource(R.string.rewards_hero_code, referrer.code),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.85f),
            )
        }
        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroButton(stringResource(R.string.rewards_action_refer), primary = true, onClick = onRefer, modifier = Modifier.weight(1.3f))
            HeroButton(stringResource(R.string.rewards_action_share), primary = false, onClick = onShare, modifier = Modifier.weight(1f), icon = true)
            HeroButton(stringResource(R.string.rewards_action_leave), primary = false, onClick = onLeave, modifier = Modifier.weight(0.8f))
        }
    }
}

@Composable
private fun HeroButton(text: String, primary: Boolean, onClick: () -> Unit, modifier: Modifier, icon: Boolean = false) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (primary) ArizColors.Accent else Color.White,
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (icon) Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.padding(end = 6.dp).size(16.dp), tint = ArizColors.Brand)
            Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = if (primary) ArizColors.OnAccent else ArizColors.Brand)
        }
    }
}

@Composable
private fun Banner(points: Int, code: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ArizColors.BrandSoft,
        border = BorderStroke(1.dp, ArizColors.Brand.copy(alpha = 0.2f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(if (points == 0) R.string.rewards_banner_empty_title else R.string.rewards_banner_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Ink,
            )
            Text(
                text = stringResource(R.string.rewards_banner_text, code),
                style = MaterialTheme.typography.bodySmall,
                color = ArizColors.Muted,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
        color = ArizColors.Brand,
    )
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = ArizColors.Muted,
    )
}

@Composable
private fun Campaigns(campaigns: List<CampaignPoints>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.EmojiEvents, contentDescription = null, modifier = Modifier.size(20.dp), tint = ArizColors.Accent)
            SectionTitle(stringResource(R.string.rewards_campaigns_title))
        }
        if (campaigns.isEmpty()) {
            Text(stringResource(R.string.rewards_campaigns_empty), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
        } else {
            campaigns.forEach { campaign ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ArizColors.Edge),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                campaign.campaignName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ArizColors.Ink,
                            )
                            Text(
                                stringResource(R.string.rewards_campaigns_total, campaign.totalPoints),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = ArizColors.Brand,
                            )
                        }
                        campaign.referrals.forEach { referral ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${referral.name} · ${referral.status}", style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                                Text("+${referral.points}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Success)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IncentiveCard(incentive: Incentive, points: Int, onRedeem: () -> Unit, modifier: Modifier = Modifier) {
    val canRedeem = points >= incentive.pointsRequired && incentive.stock > 0
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            if (incentive.imageUrl != null) {
                AsyncImage(
                    model = incentive.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).background(ArizColors.SurfaceMuted),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                incentive.promotion?.let {
                    Text(
                        it.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp),
                        color = ArizColors.Accent,
                    )
                }
                Text(incentive.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand)
                if (incentive.description.isNotBlank()) {
                    Text(incentive.description, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted, maxLines = 3)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.rewards_incentive_points, PointsFormat.format(incentive.pointsRequired)),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    Text(stringResource(R.string.rewards_incentive_stock, incentive.stock), style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
                }
                if (canRedeem) {
                    ArizPrimaryButton(text = stringResource(R.string.rewards_incentive_redeem), onClick = onRedeem, modifier = Modifier.fillMaxWidth())
                } else {
                    Text(
                        text = stringResource(R.string.rewards_incentive_missing, PointsFormat.format((incentive.pointsRequired - points).coerceAtLeast(0))),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ArizColors.Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun RedemptionRow(redemption: Redemption, modifier: Modifier = Modifier) {
    val (statusRes, color) = when (redemption.status) {
        RedemptionStatus.PENDING -> R.string.rewards_status_pending to ArizColors.Warning
        RedemptionStatus.APPROVED -> R.string.rewards_status_approved to ArizColors.Info
        RedemptionStatus.DELIVERED -> R.string.rewards_status_delivered to ArizColors.Success
        RedemptionStatus.REJECTED -> R.string.rewards_status_rejected to ArizColors.Error
        RedemptionStatus.CANCELLED -> R.string.rewards_status_cancelled to ArizColors.Muted
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(redemption.incentiveName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                Text(
                    buildString {
                        append(stringResource(R.string.rewards_incentive_points, PointsFormat.format(redemption.pointsSpent)))
                        publishedLabel(redemption.requestedAtMillis)?.let { append(" · ").append(it.relative) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ArizColors.Muted,
                )
                redemption.rejectionReason?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Error) }
            }
            Text(
                text = stringResource(statusRes),
                modifier = Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = color,
            )
        }
    }
}

/** Confirmación del canje y aviso del resultado. */
@Composable
private fun RedeemDialogs(state: ClubUiState, onAction: (ClubAction) -> Unit) {
    state.pendingRedeem?.let { incentive ->
        AlertDialog(
            onDismissRequest = { if (!state.isRedeeming) onAction(ClubAction.RedeemDismissed) },
            title = { Text(stringResource(R.string.rewards_redeem_dialog_title)) },
            text = {
                Text(stringResource(R.string.rewards_redeem_dialog_text, incentive.name, PointsFormat.format(incentive.pointsRequired)))
            },
            confirmButton = {
                TextButton(onClick = { onAction(ClubAction.RedeemConfirmed) }, enabled = !state.isRedeeming) {
                    Text(stringResource(R.string.rewards_redeem_dialog_confirm), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ClubAction.RedeemDismissed) }, enabled = !state.isRedeeming) {
                    Text(stringResource(R.string.rewards_redeem_dialog_cancel))
                }
            },
        )
    }
    state.redeemOutcome?.let { outcome ->
        AlertDialog(
            onDismissRequest = { onAction(ClubAction.OutcomeDismissed) },
            title = {
                Text(stringResource(if (outcome is RedeemOutcome.Success) R.string.rewards_redeem_success_title else R.string.rewards_redeem_failure_title))
            },
            text = {
                Text(if (outcome is RedeemOutcome.Failure) outcome.message else stringResource(R.string.rewards_redeem_success_text))
            },
            confirmButton = {
                TextButton(onClick = { onAction(ClubAction.OutcomeDismissed) }) {
                    Text(stringResource(R.string.rewards_ok), fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}
