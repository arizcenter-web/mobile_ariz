package com.tecsup.app_ariz.feature.professional.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelMenuRow
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.StatTile
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.ApprovalState
import com.tecsup.app_ariz.feature.professional.domain.PanelData
import com.tecsup.app_ariz.feature.professional.domain.canManageProjects
import com.tecsup.app_ariz.feature.professional.domain.isAgency

/** Destinos del panel profesional: el dashboard los ofrece como menú. */
internal data class ProDestinations(
    val onProperties: () -> Unit,
    val onNewProperty: () -> Unit,
    val onProjects: () -> Unit,
    val onLeads: () -> Unit,
    val onStats: () -> Unit,
    val onSubscription: () -> Unit,
    val onProfile: () -> Unit,
    val onSettings: () -> Unit,
)

@Composable
internal fun DashboardScreen(
    onBackClick: () -> Unit,
    destinations: ProDestinations,
    modifier: Modifier = Modifier,
    viewModel: PanelViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DashboardContent(state = state, onRetry = viewModel::load, onBackClick = onBackClick, destinations = destinations, modifier = modifier)
}

@Composable
internal fun DashboardContent(
    state: PanelUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    destinations: ProDestinations,
    modifier: Modifier = Modifier,
) {
    val data = state.data
    PanelPage(
        eyebrow = stringResource(R.string.pro_dashboard_welcome),
        title = stringResource(R.string.pro_dashboard_hello, data?.profile?.firstName?.substringBefore(' ').orEmpty().ifBlank { "" }),
        subtitle = stringResource(R.string.pro_dashboard_intro),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.pro_back),
        modifier = modifier,
    ) {
        when {
            state.isLoading -> PanelLoading()
            data == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.pro_retry), onRetry = onRetry)
            else -> {
                StatusPill(stringResource(data.profile.type.labelRes()), ArizColors.Brand)
                StatusNotice(data)
                StatsGrid(data)
                SubscriptionCard(data, onSeePlans = destinations.onSubscription)
                Text(
                    text = stringResource(R.string.pro_menu_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
                ArizPrimaryButton(
                    text = stringResource(R.string.pro_menu_publish),
                    onClick = destinations.onNewProperty,
                    modifier = Modifier.fillMaxWidth(),
                )
                PanelMenuRow(Icons.Outlined.Home, stringResource(R.string.pro_menu_properties), destinations.onProperties)
                if (data.profile.type.canManageProjects) {
                    PanelMenuRow(Icons.Outlined.Apartment, stringResource(R.string.pro_menu_projects), destinations.onProjects)
                }
                PanelMenuRow(Icons.Outlined.Forum, stringResource(R.string.pro_menu_leads), destinations.onLeads)
                PanelMenuRow(Icons.Outlined.BarChart, stringResource(R.string.pro_menu_stats), destinations.onStats)
                PanelMenuRow(
                    Icons.Outlined.Notifications,
                    stringResource(if (data.profile.type.isAgency) R.string.pro_menu_enablement else R.string.pro_menu_subscription),
                    destinations.onSubscription,
                )
                PanelMenuRow(Icons.Outlined.Person, stringResource(R.string.pro_menu_profile), destinations.onProfile)
                PanelMenuRow(Icons.Outlined.Settings, stringResource(R.string.pro_menu_settings), destinations.onSettings)
            }
        }
    }
}

/** Aviso de perfil pendiente o rechazado (los `STATUS_NOTICES` del web). */
@Composable
private fun StatusNotice(data: PanelData) {
    val (text, color) = when (data.profile.status) {
        ApprovalState.PENDING -> R.string.pro_notice_pending to ArizColors.Warning
        ApprovalState.REJECTED -> R.string.pro_notice_rejected to ArizColors.Error
        ApprovalState.APPROVED -> return
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    ) {
        Text(
            text = stringResource(text),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Composable
private fun StatsGrid(data: PanelData) {
    val stats = data.stats
    val type = data.profile.type
    val tiles = buildList<@Composable (Modifier) -> Unit> {
        add { m ->
            StatTile(
                label = stringResource(R.string.pro_stat_published),
                value = stats.publishedCount.toString(),
                caption = stringResource(R.string.pro_stat_published_caption, stats.publishedCount, stats.pausedCount),
                modifier = m,
            )
        }
        // Solo inmobiliarias y desarrolladoras gestionan proyectos.
        if (type.canManageProjects) {
            add { m ->
                StatTile(
                    label = stringResource(R.string.pro_stat_projects),
                    value = stats.activeProjects.toString(),
                    caption = stringResource(R.string.pro_stat_projects_caption, stats.totalProjects),
                    modifier = m,
                )
            }
        }
        add { m ->
            StatTile(
                label = stringResource(R.string.pro_stat_leads),
                value = stats.totalLeads.toString(),
                caption = stringResource(R.string.pro_stat_leads_caption, stats.newLeads),
                modifier = m,
            )
        }
        add { m ->
            StatTile(
                label = stringResource(R.string.pro_stat_days),
                value = stats.daysRemaining?.toString() ?: "-",
                caption = when {
                    type.isAgency -> stringResource(R.string.pro_stat_days_agency)
                    stats.daysRemaining == null -> stringResource(R.string.pro_stat_days_none)
                    else -> stringResource(R.string.pro_stat_days_caption)
                },
                modifier = m,
            )
        }
    }
    tiles.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { tile -> tile(Modifier.weight(1f)) }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SubscriptionCard(data: PanelData, onSeePlans: () -> Unit) {
    val subscription = data.subscription
    PanelCard {
        Text(
            text = stringResource(R.string.pro_menu_subscription).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = ArizColors.Accent,
        )
        Text(
            text = if (data.profile.type.isAgency) {
                stringResource(R.string.pro_agency_notice_title)
            } else if (subscription?.isActive == true) {
                stringResource(R.string.pro_dashboard_subscription_active, subscription.planName)
            } else {
                stringResource(R.string.pro_dashboard_subscription_none)
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Ink,
        )
        Text(
            text = stringResource(
                when {
                    data.profile.type.isAgency -> R.string.pro_agency_notice_text
                    subscription?.isActive == true -> R.string.pro_dashboard_subscription_active_text
                    else -> R.string.pro_dashboard_subscription_none_text
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = ArizColors.Muted,
        )
        Surface(
            onClick = onSeePlans,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, ArizColors.Edge),
        ) {
            Text(
                text = stringResource(if (data.profile.type.isAgency) R.string.pro_agency_contact else R.string.pro_dashboard_see_plans),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Brand,
            )
        }
    }
}
