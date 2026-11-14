package com.tecsup.app_ariz.feature.professional.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.common.ArizContact
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.Plan
import com.tecsup.app_ariz.feature.professional.domain.Subscription
import com.tecsup.app_ariz.feature.professional.domain.SubscriptionStatus
import com.tecsup.app_ariz.feature.professional.domain.isAgency

@Composable
internal fun SubscriptionScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: SubscriptionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Plan de pago elegido: al volver del selector de fotos se envía con ese comprobante.
    var pendingPaidPlan by remember { mutableStateOf<Plan?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val plan = pendingPaidPlan
        pendingPaidPlan = null
        if (plan != null && uri != null) viewModel.onAction(SubscriptionAction.RequestPaid(plan, uri.toString()))
    }

    WithNotice(state.notice, onShown = { viewModel.onAction(SubscriptionAction.NoticeShown) }, modifier = modifier) {
        PanelPage(
            eyebrow = stringResource(R.string.pro_panel_eyebrow),
            title = stringResource(R.string.pro_subscription_title),
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.pro_back),
        ) {
            val data = state.data
            val profile = state.profile
            when {
                state.isLoading -> PanelLoading()
                profile != null && profile.type.isAgency -> AgencyContact(profile.businessName.ifBlank { profile.firstName })
                data == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.pro_retry)) { viewModel.onAction(SubscriptionAction.Refresh) }
                else -> {
                    CurrentPlan(data.subscription)
                    Text(
                        text = stringResource(R.string.pro_plans_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    data.plans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            busy = state.requestingPlanId == plan.id,
                            onRequest = {
                                if (plan.isFree || plan.price <= 0) {
                                    viewModel.onAction(SubscriptionAction.RequestFree(plan))
                                } else {
                                    pendingPaidPlan = plan
                                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgencyContact(name: String) {
    val context = LocalContext.current
    val message = stringResource(R.string.pro_agency_whatsapp_message, name)
    PanelCard {
        Text(
            text = stringResource(R.string.pro_agency_notice_title).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = ArizColors.Accent,
        )
        Text(stringResource(R.string.pro_agency_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
        Text(stringResource(R.string.pro_agency_text), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
        ArizPrimaryButton(
            text = stringResource(R.string.pro_agency_whatsapp),
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ArizContact.whatsappUrl(message)))) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CurrentPlan(subscription: Subscription?) {
    PanelCard {
        Text(
            text = stringResource(R.string.pro_subscription_current).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = ArizColors.Accent,
        )
        if (subscription == null) {
            Text(stringResource(R.string.pro_subscription_none), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
            return@PanelCard
        }
        Text(subscription.planName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
        val (text, color) = when (subscription.status) {
            SubscriptionStatus.ACTIVE -> {
                val days = subscription.expiresAtMillis?.let { maxOf(0L, (it - System.currentTimeMillis()) / 86_400_000L).toInt() }
                (days?.let { stringResource(R.string.pro_subscription_expires, it) } ?: stringResource(R.string.pro_status_published)) to ArizColors.Success
            }
            SubscriptionStatus.PENDING_PAYMENT -> stringResource(R.string.pro_subscription_pending) to ArizColors.Warning
            SubscriptionStatus.REJECTED -> stringResource(R.string.pro_subscription_rejected, subscription.rejectionReason.orEmpty()) to ArizColors.Error
            SubscriptionStatus.EXPIRED -> stringResource(R.string.pro_status_archived) to ArizColors.Muted
        }
        StatusPill(text, color)
    }
}

@Composable
private fun PlanCard(plan: Plan, busy: Boolean, onRequest: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (plan.highlight) 2.dp else 1.dp, if (plan.highlight) ArizColors.Accent else ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (plan.highlight) StatusPill(stringResource(R.string.pro_plan_highlight), ArizColors.Accent)
            Text(plan.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
            Text(
                text = if (plan.isFree || plan.price <= 0) stringResource(R.string.pro_plan_free) else formatPrice(plan.price, plan.currency),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Ink,
            )
            Text(
                text = listOf(
                    stringResource(R.string.pro_plan_days, plan.durationDays),
                    stringResource(R.string.pro_plan_properties, plan.propertyLimit),
                    stringResource(R.string.pro_plan_projects, plan.projectLimit),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ArizColors.Muted,
            )
            plan.features.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = ArizColors.Ink) }
            if (!plan.isFree && plan.price > 0) {
                Text(stringResource(R.string.pro_plan_receipt_hint), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
            }
            ArizPrimaryButton(
                text = stringResource(if (plan.isFree || plan.price <= 0) R.string.pro_plan_request else R.string.pro_plan_request_paid),
                onClick = onRequest,
                modifier = Modifier.fillMaxWidth(),
                isLoading = busy,
            )
        }
    }
}
