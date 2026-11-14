package com.tecsup.app_ariz.feature.professional.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.ConfirmDialog
import com.tecsup.app_ariz.core.ui.component.PanelEmpty
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.core.ui.format.publishedLabel
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.OwnerProject
import com.tecsup.app_ariz.feature.professional.domain.OwnerProperty
import com.tecsup.app_ariz.feature.professional.domain.ProjectStatus
import com.tecsup.app_ariz.feature.professional.domain.PropertyStatus

@Composable
internal fun PropertiesScreen(
    onBackClick: () -> Unit,
    onNewClick: () -> Unit,
    onEditClick: (String) -> Unit,
    rentOnly: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: PropertiesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val items = if (rentOnly) state.items.filter { it.isRent } else state.items
    val target = state.items.firstOrNull { it.id == toDelete }

    WithNotice(state.notice, onShown = { viewModel.onAction(PropertiesAction.NoticeShown) }, modifier = modifier) {
        PanelPage(
            eyebrow = stringResource(R.string.pro_panel_eyebrow),
            title = stringResource(if (rentOnly) R.string.pro_rentals_title else R.string.pro_properties_title),
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.pro_back),
        ) {
            ArizPrimaryButton(text = stringResource(R.string.pro_properties_new), onClick = onNewClick, modifier = Modifier.fillMaxWidth())
            when {
                state.isLoading -> PanelLoading()
                state.error != null && state.items.isEmpty() ->
                    PanelError(state.error!!.message, stringResource(R.string.pro_retry)) { viewModel.onAction(PropertiesAction.Refresh) }
                items.isEmpty() -> PanelEmpty(stringResource(R.string.pro_properties_empty), stringResource(R.string.pro_properties_empty_text))
                else -> items.forEach { property ->
                    PropertyRow(
                        property = property,
                        busy = state.busyId == property.id,
                        onEdit = { onEditClick(property.id) },
                        onPublish = { viewModel.onAction(PropertiesAction.Publish(property.id)) },
                        onDelete = { toDelete = property.id },
                    )
                }
            }
        }
    }

    target?.let { property ->
        ConfirmDialog(
            title = stringResource(R.string.pro_property_delete_title),
            text = stringResource(R.string.pro_property_delete_text, property.title),
            confirmLabel = stringResource(R.string.pro_delete),
            dismissLabel = stringResource(R.string.pro_cancel),
            onConfirm = {
                viewModel.onAction(PropertiesAction.Delete(property.id))
                toDelete = null
            },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun PropertyRow(property: OwnerProperty, busy: Boolean, onEdit: () -> Unit, onPublish: () -> Unit, onDelete: () -> Unit) {
    // Solo los borradores, pausadas y rechazadas se pueden (re)enviar; las publicadas o cerradas no.
    val canPublish = property.status in setOf(PropertyStatus.DRAFT, PropertyStatus.PAUSED, PropertyStatus.REJECTED)
    ItemCard(
        imageUrl = property.coverImageUrl,
        title = property.title,
        status = { StatusPill(stringResource(property.status.labelRes()), property.status.color()) },
        line1 = formatPrice(property.price, property.currency) + if (property.isRent) " ${stringResource(R.string.pro_property_rent_suffix)}" else "",
        line2 = listOf(property.district, property.department).filter { it.isNotBlank() }.joinToString(", "),
        line3 = publishedLabel(property.publishedAtMillis)?.relative,
        busy = busy,
        actions = {
            ActionText(stringResource(R.string.pro_property_edit), ArizColors.Brand, onEdit)
            if (canPublish) ActionText(stringResource(R.string.pro_property_publish), ArizColors.Success, onPublish)
            ActionText(stringResource(R.string.pro_delete), ArizColors.Error, onDelete)
        },
    )
}

@Composable
internal fun ProjectsScreen(
    onBackClick: () -> Unit,
    onNewClick: () -> Unit,
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val target = state.items.firstOrNull { it.id == toDelete }

    WithNotice(state.notice, onShown = { viewModel.onAction(ProjectsAction.NoticeShown) }, modifier = modifier) {
        PanelPage(
            eyebrow = stringResource(R.string.pro_panel_eyebrow),
            title = stringResource(R.string.pro_projects_title),
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.pro_back),
        ) {
            ArizPrimaryButton(text = stringResource(R.string.pro_projects_new), onClick = onNewClick, modifier = Modifier.fillMaxWidth())
            when {
                state.isLoading -> PanelLoading()
                state.error != null && state.items.isEmpty() ->
                    PanelError(state.error!!.message, stringResource(R.string.pro_retry)) { viewModel.onAction(ProjectsAction.Refresh) }
                state.items.isEmpty() -> PanelEmpty(stringResource(R.string.pro_projects_empty), stringResource(R.string.pro_projects_empty_text))
                else -> state.items.forEach { project ->
                    ProjectRow(
                        project = project,
                        busy = state.busyId == project.id,
                        onPublish = { viewModel.onAction(ProjectsAction.Publish(project.id)) },
                        onEdit = { onEditClick(project.id) },
                        onDelete = { toDelete = project.id },
                    )
                }
            }
        }
    }

    target?.let { project ->
        ConfirmDialog(
            title = stringResource(R.string.pro_project_delete_title),
            text = stringResource(R.string.pro_project_delete_text, project.name),
            confirmLabel = stringResource(R.string.pro_delete),
            dismissLabel = stringResource(R.string.pro_cancel),
            onConfirm = {
                viewModel.onAction(ProjectsAction.Delete(project.id))
                toDelete = null
            },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun ProjectRow(project: OwnerProject, busy: Boolean, onPublish: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val canPublish = project.status in setOf(ProjectStatus.DRAFT, ProjectStatus.PAUSED, ProjectStatus.REJECTED)
    ItemCard(
        imageUrl = project.coverImageUrl,
        title = project.name,
        status = { StatusPill(stringResource(project.status.labelRes()), project.status.color()) },
        line1 = stringResource(R.string.pro_project_from, formatPrice(project.priceFrom, project.currency)),
        line2 = listOf(project.district, stringResource(R.string.pro_project_lots, project.lotsCount)).filter { it.isNotBlank() }.joinToString(" · "),
        line3 = publishedLabel(project.publishedAtMillis)?.relative,
        busy = busy,
        actions = {
            if (canPublish) ActionText(stringResource(R.string.pro_property_publish), ArizColors.Success, onPublish)
            ActionText(stringResource(R.string.pro_property_edit), ArizColors.Brand, onEdit)
            ActionText(stringResource(R.string.pro_delete), ArizColors.Error, onDelete)
        },
    )
}

/** Tarjeta de una publicación del panel: miniatura, título, estado, datos y acciones. */
@Composable
private fun ItemCard(
    imageUrl: String?,
    title: String,
    status: @Composable () -> Unit,
    line1: String,
    line2: String,
    line3: String?,
    busy: Boolean,
    actions: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(84.dp).background(ArizColors.SurfaceMuted, RoundedCornerShape(8.dp))) {
                    if (imageUrl != null) {
                        AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(84.dp), contentScale = ContentScale.Crop)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    status()
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand, maxLines = 2)
                    Text(line1, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Ink)
                    if (line2.isNotBlank()) Text(line2, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted, maxLines = 1)
                    line3?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted) }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                if (busy) Text("…", style = MaterialTheme.typography.titleMedium, color = ArizColors.Muted) else actions()
            }
        }
    }
}

@Composable
private fun ActionText(text: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = color,
    )
}
