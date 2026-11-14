package com.tecsup.app_ariz.feature.admin.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.ConfirmDialog
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelEmpty
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelMenuRow
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.StatTile
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.feature.admin.R
import com.tecsup.app_ariz.feature.admin.domain.AdminAction
import com.tecsup.app_ariz.feature.admin.domain.AdminItem
import com.tecsup.app_ariz.feature.admin.domain.AdminSections

@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    onSectionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminOverviewViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val analytics by analyticsViewModel.state.collectAsStateWithLifecycle()
    PanelPage(
        eyebrow = stringResource(R.string.adm_eyebrow),
        title = stringResource(R.string.adm_dashboard_title),
        subtitle = stringResource(R.string.adm_dashboard_intro),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.adm_back),
        modifier = modifier,
    ) {
        if (state.isLoading && state.overview == null) {
            PanelLoading()
        } else {
            val overview = state.overview
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(stringResource(R.string.adm_pending_professionals), overview?.pendingProfessionals.show(), stringResource(R.string.adm_pending_caption), Modifier.weight(1f))
                StatTile(stringResource(R.string.adm_pending_subscriptions), overview?.pendingSubscriptions.show(), stringResource(R.string.adm_pending_caption), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(stringResource(R.string.adm_pending_redemptions), overview?.pendingRedemptions.show(), stringResource(R.string.adm_pending_caption), Modifier.weight(1f))
                StatTile(stringResource(R.string.adm_total_claims), overview?.claims.show(), stringResource(R.string.adm_total_caption), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(stringResource(R.string.adm_total_professionals), overview?.professionals.show(), stringResource(R.string.adm_total_caption), Modifier.weight(1f))
                StatTile(stringResource(R.string.adm_total_projects), overview?.projects.show(), stringResource(R.string.adm_total_caption), Modifier.weight(1f))
            }
        }
        AnalyticsSection(analytics, onSelect = { p, r -> analyticsViewModel.select(p, r) }, onRetry = analyticsViewModel::load)
        AdminSections.grouped().forEach { (group, sections) ->
            Text(
                text = group,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            sections.forEach { section ->
                PanelMenuRow(iconFor(group), section.title, onClick = { onSectionClick(section.key) }, caption = section.description)
            }
        }
        Text(stringResource(R.string.adm_web_only), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
    }
}

private fun Int?.show(): String = this?.toString() ?: "-"

private fun iconFor(group: String): ImageVector = when (group) {
    AdminSections.GROUP_ACCOUNTS -> Icons.Outlined.Apartment
    AdminSections.GROUP_LISTINGS -> Icons.Outlined.Home
    AdminSections.GROUP_FINANCE -> Icons.Outlined.AccountBalance
    AdminSections.GROUP_REWARDS -> Icons.Outlined.CardGiftcard
    AdminSections.GROUP_SUPPORT -> Icons.Outlined.Forum
    else -> Icons.Outlined.TrendingUp
}

/** Abre el archivo exportado con la app que corresponda (o lo ofrece para compartir). */
internal fun openExport(context: android.content.Context, path: String) {
    val file = java.io.File(path)
    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.exports", file)
    val mime = when (file.extension.lowercase()) {
        "pdf" -> "application/pdf"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> "*/*"
    }
    val view = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val intent = if (view.resolveActivity(context.packageManager) != null) view
    else Intent.createChooser(
        Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
        file.name,
    )
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Una acción esperando confirmación (y motivo, si lo pide). */
private data class PendingAction(val action: AdminAction, val item: AdminItem)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSectionScreen(
    onBackClick: () -> Unit,
    onFormClick: (key: String, id: String?) -> Unit,
    onSitePlan: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminSectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val section = state.section
    var pending by remember { mutableStateOf<PendingAction?>(null) }
    var reason by rememberSaveable { mutableStateOf("") }
    val host = remember { SnackbarHostState() }
    val done = stringResource(R.string.adm_done)

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.files.collect { path -> openExport(context, path) }
    }
    var result by remember { mutableStateOf<AdminNotice.Result?>(null) }
    LaunchedEffect(state.notice) {
        state.notice?.let {
            viewModel.noticeShown()
            when (it) {
                is AdminNotice.Result -> result = it
                is AdminNotice.Failure -> host.showSnackbar(it.message)
                AdminNotice.Done -> host.showSnackbar(done)
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        PanelPage(
            eyebrow = stringResource(R.string.adm_eyebrow),
            title = section?.title.orEmpty(),
            subtitle = section?.description,
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.adm_back),
        ) {
            if (section == null) return@PanelPage
            if ((section.form != null && section.canCreate) || section.externalForm) {
                ArizPrimaryButton(
                    text = stringResource(R.string.adm_new_record),
                    onClick = { onFormClick(section.key, null) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (section.exports.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.exports.forEach { export ->
                        OutlinedButton(onClick = { viewModel.export(export) }, enabled = !state.exporting) {
                            Text(if (state.exporting) stringResource(R.string.adm_exporting) else export.label)
                        }
                    }
                }
            }
            if (section.filters.size > 1) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.filters.forEachIndexed { index, filter ->
                        FilterChip(selected = index == state.filterIndex, onClick = { viewModel.selectFilter(index) }, label = { Text(filter.label) })
                    }
                }
            }
            when {
                state.isLoading -> PanelLoading()
                state.error != null -> PanelError(state.error!!.message, stringResource(R.string.adm_retry), onRetry = viewModel::load)
                state.items.isEmpty() -> PanelEmpty(stringResource(R.string.adm_empty_title), stringResource(R.string.adm_empty_text))
                else -> state.items.forEach { item ->
                    ItemCard(
                        item = item,
                        detail = state.details[item.id],
                        onExpand = { viewModel.expand(item.id) },
                        onPlan = if (section.sitePlan) ({ onSitePlan(item.id) }) else null,
                        busy = state.busyId == item.id,
                        onEdit = if (section.form != null || (section.externalForm && section.externalEdit)) ({ onFormClick(section.key, item.id) }) else null,
                    ) { action, target ->
                        reason = ""
                        pending = PendingAction(action, target)
                    }
                }
            }
        }
        SnackbarHost(host, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    }

    result?.let { shown ->
        val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { result = null },
            title = { Text(shown.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(shown.value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                    Text(stringResource(R.string.adm_result_hint), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                }
            },
            confirmButton = {
                TextButton(onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(shown.value)) }) {
                    Text(stringResource(R.string.adm_copy), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { result = null }) { Text(stringResource(R.string.adm_close)) } },
        )
    }

    pending?.let { request ->
        if (request.action.reasonKey != null) {
            ReasonDialog(
                title = request.action.label,
                itemTitle = request.item.title,
                reason = reason,
                inputLabel = request.action.inputLabel,
                onReasonChange = { reason = it },
                onConfirm = {
                    viewModel.run(request.action, request.item.id, reason)
                    pending = null
                },
                onDismiss = { pending = null },
            )
        } else {
            ConfirmDialog(
                title = request.action.label,
                text = stringResource(R.string.adm_confirm_text, request.item.title),
                confirmLabel = stringResource(R.string.adm_confirm),
                dismissLabel = stringResource(R.string.adm_cancel),
                onConfirm = {
                    viewModel.run(request.action, request.item.id, null)
                    pending = null
                },
                onDismiss = { pending = null },
            )
        }
    }
}

@Composable
private fun ReasonDialog(
    title: String,
    itemTitle: String,
    reason: String,
    inputLabel: String?,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.adm_confirm_text, itemTitle))
                ArizTextField(value = reason, onValueChange = onReasonChange, label = inputLabel ?: stringResource(R.string.adm_reason_label), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.adm_confirm), fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.adm_cancel)) } },
    )
}

@Composable
private fun ItemCard(
    item: AdminItem,
    detail: AdminItem?,
    onExpand: () -> Unit,
    busy: Boolean,
    onPlan: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onAction: (AdminAction, AdminItem) -> Unit,
) {
    PanelCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                item.subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted) }
            }
            item.status?.let { StatusPill(statusLabel(it), statusColor(it)) }
        }
        item.details.forEach { detail ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(detail.label + ":", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                Text(detail.value, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted, modifier = Modifier.weight(1f))
            }
        }
        var expanded by rememberSaveable(item.id) { mutableStateOf(false) }
        // Al abrir (o si los datos quedaron viejos) se pide el detalle completo al backend.
        LaunchedEffect(expanded, detail == null) { if (expanded && detail == null) onExpand() }
        val shown = detail ?: item
        if (shown.fields.size > item.details.size || detail != null || item.groups.isNotEmpty()) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(stringResource(if (expanded) R.string.adm_hide_detail else R.string.adm_show_detail), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
            }
        }
        if (expanded) {
            shown.fields.forEach { field ->
                Column {
                    Text(field.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Accent)
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(field.value, style = MaterialTheme.typography.bodySmall, color = ArizColors.Ink)
                    }
                }
            }
        }
        item.link?.let { LinkButton(stringResource(R.string.adm_open_receipt), it) }
        if (onEdit != null) {
            TextButton(onClick = onEdit, enabled = !busy) { Text(stringResource(R.string.adm_edit), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
        }
        if (onPlan != null) {
            TextButton(onClick = onPlan, enabled = !busy) { Text(stringResource(R.string.adm_plan_open), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
        }
        ActionRow(item, busy, onAction)
        val groups = if (expanded && detail != null) detail.groups else item.groups
        groups.forEach { group ->
            Text(group.label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = ArizColors.Accent)
            group.items.forEach { child ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ArizColors.Edge),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(child.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Ink)
                            child.status?.let { StatusPill(statusLabel(it), statusColor(it)) }
                        }
                        child.details.forEach { d ->
                            Text(d.label + ": " + d.value, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                        }
                        ActionRow(child, busy, onAction)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ActionRow(item: AdminItem, busy: Boolean, onAction: (AdminAction, AdminItem) -> Unit) {
    val actions = item.actions.filter { it.isAvailableFor(item.status) }
    if (actions.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.forEach { action ->
            OutlinedButton(
                onClick = { onAction(action, item) },
                enabled = !busy,
                border = BorderStroke(1.dp, if (action.destructive) ArizColors.Error else ArizColors.Brand),
            ) {
                Text(action.label, color = if (action.destructive) ArizColors.Error else ArizColors.Brand, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LinkButton(label: String, url: String) {
    val context = LocalContext.current
    TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }) {
        Text(label, fontWeight = FontWeight.Bold, color = ArizColors.Brand)
    }
}

@Composable
private fun statusLabel(status: String): String {
    val res = when (status) {
        "APPROVED" -> R.string.adm_status_approved
        "PENDING" -> R.string.adm_status_pending
        "REJECTED" -> R.string.adm_status_rejected
        "PUBLISHED" -> R.string.adm_status_published
        "PENDING_REVIEW" -> R.string.adm_status_pending_review
        "DRAFT" -> R.string.adm_status_draft
        "PAUSED" -> R.string.adm_status_paused
        "ACTIVE" -> R.string.adm_status_active
        "INACTIVE" -> R.string.adm_status_inactive
        "PENDING_PAYMENT" -> R.string.adm_status_pending_payment
        "EXPIRED" -> R.string.adm_status_expired
        "DELIVERED" -> R.string.adm_status_delivered
        "CANCELLED" -> R.string.adm_status_cancelled
        "PENDING_VALIDATION" -> R.string.adm_status_pending_validation
        "CONTACTED" -> R.string.adm_status_contacted
        "VISIT" -> R.string.adm_status_visit
        "OPERATION_COMPLETED" -> R.string.adm_status_completed
        else -> null
    }
    return if (res != null) stringResource(res) else status
}

private fun statusColor(status: String): Color = when (status) {
    "APPROVED", "PUBLISHED", "ACTIVE", "DELIVERED", "OPERATION_COMPLETED" -> ArizColors.Success
    "PENDING", "PENDING_REVIEW", "PENDING_PAYMENT", "PAUSED", "PENDING_VALIDATION", "CONTACTED", "VISIT" -> ArizColors.Warning
    "REJECTED", "CANCELLED" -> ArizColors.Error
    else -> ArizColors.Muted
}
