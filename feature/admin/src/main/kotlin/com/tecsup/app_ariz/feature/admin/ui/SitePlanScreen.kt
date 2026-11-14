package com.tecsup.app_ariz.feature.admin.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizSecondaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.ConfirmDialog
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.feature.admin.R
import com.tecsup.app_ariz.feature.admin.domain.AdminField
import com.tecsup.app_ariz.feature.admin.domain.AdminFormSpec
import com.tecsup.app_ariz.feature.admin.domain.ApartmentStatusLabels
import com.tecsup.app_ariz.feature.admin.domain.FieldKind
import com.tecsup.app_ariz.feature.admin.domain.LotStatusLabels
import com.tecsup.app_ariz.feature.admin.domain.PlanMode
import com.tecsup.app_ariz.feature.admin.domain.SavePlanOutcome
import com.tecsup.app_ariz.feature.admin.domain.SitePlans
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val BuildingSpec = AdminFormSpec(
    title = "Edificio",
    fields = listOf(
        AdminField("name", "Nombre del edificio", required = true),
        AdminField("widthM", "Ancho (m)", FieldKind.NUMBER, required = true, default = "20"),
        AdminField("lengthM", "Largo (m)", FieldKind.NUMBER, required = true, default = "30"),
    ),
)

/** Qué diálogo de edición está abierto. */
private sealed interface PlanDialog {
    data class Lot(val lot: JsonObject, val isNew: Boolean) : PlanDialog
    data object NewBuilding : PlanDialog
    data class NewFloor(val buildingId: String) : PlanDialog
    data class Apartment(val buildingId: String, val floorId: String, val apartment: JsonObject, val isNew: Boolean) : PlanDialog
    /** [remove] transforma el plano quitando el elemento. */
    data class Remove(val label: String, val remove: (JsonObject) -> JsonObject) : PlanDialog
}

/**
 * Plano de un proyecto (solo administrador): inventario de lotes o edificios → pisos → departamentos. Se edita sobre el
 * plano completo para conservar los polígonos que solo se dibujan en el panel web.
 */
@Composable
fun SitePlanScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: SitePlanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<PlanDialog?>(null) }
    var mapOpen by rememberSaveable { mutableStateOf(false) }
    val host = remember { SnackbarHostState() }
    val savedText = stringResource(R.string.adm_plan_saved)
    val publishedText = stringResource(R.string.adm_plan_published)

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) { viewModel.files.collect { openExport(context, it) } }
    val importedText = state.imported?.let { stringResource(R.string.adm_plan_imported, it) }
    LaunchedEffect(state.imported) { importedText?.let { host.showSnackbar(it); viewModel.messageShown() } }
    LaunchedEffect(state.outcome, state.failure) {
        state.outcome?.let { host.showSnackbar(if (it == SavePlanOutcome.PUBLISHED) publishedText else savedText) }
        state.failure?.let { host.showSnackbar(it) }
        if (state.outcome != null || state.failure != null) viewModel.messageShown()
    }

    Box(modifier.fillMaxSize()) {
        if (mapOpen && state.doc != null) {
            PlanMapEditor(state, viewModel, onClose = { mapOpen = false })
        } else PanelPage(
            eyebrow = stringResource(R.string.adm_eyebrow),
            title = stringResource(R.string.adm_plan_title),
            subtitle = state.doc?.projectName,
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.adm_back),
        ) {
            val doc = state.doc
            when {
                state.isLoading -> PanelLoading()
                doc == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.adm_retry), onRetry = viewModel::load)
                else -> {
                    PlanHeader(state)
                    if (state.mode == PlanMode.LOTS) LotsSection(state, onDialog = { dialog = it }, onImport = viewModel::importLots, onTemplate = viewModel::downloadTemplate, onOpenMap = { mapOpen = true })
                    else ApartmentsSection(state, onDialog = { dialog = it }, onFloorImage = viewModel::uploadFloorImage)
                    ArizSecondaryButton(
                        onClick = { viewModel.save(publish = false) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving && !state.uploading && state.dirty,
                    ) { Text(stringResource(R.string.adm_plan_save_draft), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
                    ArizPrimaryButton(
                        text = stringResource(R.string.adm_plan_publish),
                        onClick = { viewModel.save(publish = true) },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = state.isSaving,
                        enabled = !state.uploading,
                    )
                }
            }
        }
        SnackbarHost(host, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    }

    PlanDialogs(state, dialog, onClose = { dialog = null }, viewModel = viewModel)
}

@Composable
private fun PlanHeader(state: SitePlanUiState) {
    val doc = state.doc ?: return
    val summary = SitePlans.summary(state.plan)
    PanelCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(if (state.mode == PlanMode.LOTS) R.string.adm_plan_mode_lots else R.string.adm_plan_mode_apts),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            StatusPill(
                stringResource(if (doc.publishedAt != null) R.string.adm_plan_is_published else R.string.adm_plan_is_draft),
                if (doc.publishedAt != null) ArizColors.Success else ArizColors.Warning,
            )
        }
        val counts = if (state.mode == PlanMode.LOTS) {
            stringResource(R.string.adm_plan_counts_lots, summary.lots, summary.lotsAvailable, summary.zones)
        } else {
            stringResource(R.string.adm_plan_counts_apts, summary.buildings, summary.floors, summary.apartments, summary.apartmentsAvailable)
        }
        Text(counts, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
        Text(stringResource(R.string.adm_plan_revision, doc.revision), style = MaterialTheme.typography.labelSmall, color = ArizColors.Muted)
        if (state.dirty) Text(stringResource(R.string.adm_plan_unsaved), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Warning)
    }
}

@Composable
private fun SmallActions(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) { content() }
}

@Composable
private fun LinkText(text: String, color: Color = ArizColors.Brand, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled) { Text(text, fontWeight = FontWeight.Bold, color = color) }
}

// ---------- lotes ----------

@Composable
private fun LotsSection(state: SitePlanUiState, onDialog: (PlanDialog) -> Unit, onImport: (String) -> Unit, onTemplate: () -> Unit, onOpenMap: () -> Unit) {
    ArizPrimaryButton(text = stringResource(R.string.adm_plan_open_map), onClick = onOpenMap, modifier = Modifier.fillMaxWidth())
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) onImport(uri.toString()) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ArizSecondaryButton(
            onClick = { picker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/octet-stream")) },
            modifier = Modifier.weight(1f),
            enabled = !state.uploading,
        ) { Text(stringResource(R.string.adm_plan_import), fontWeight = FontWeight.Bold, color = ArizColors.Brand) }
        ArizSecondaryButton(onClick = onTemplate, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.adm_plan_template), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
        }
    }
    ArizSecondaryButton(onClick = { onDialog(PlanDialog.Lot(SitePlans.newLot(), isNew = true)) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.adm_plan_add_lot), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
    }
    val lots = SitePlans.lots(state.plan)
    if (lots.isEmpty()) Text(stringResource(R.string.adm_plan_no_lots), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
    lots.forEach { lot ->
        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    val block = SitePlans.text(lot, "block")
                    Text(
                        (if (block.isNotBlank()) "$block-" else "") + SitePlans.text(lot, "code"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    Text(
                        stringResource(R.string.adm_plan_lot_line, SitePlans.text(lot, "areaM2"), SitePlans.text(lot, "price")),
                        style = MaterialTheme.typography.bodySmall,
                        color = ArizColors.Muted,
                    )
                }
                val status = SitePlans.text(lot, "status")
                StatusPill(LotStatusLabels[status] ?: status, statusTone(status))
            }
            SmallActions {
                LinkText(stringResource(R.string.adm_edit)) { onDialog(PlanDialog.Lot(lot, isNew = false)) }
                LinkText(stringResource(R.string.adm_remove), ArizColors.Error) {
                    val id = SitePlans.id(lot)
                    onDialog(PlanDialog.Remove(SitePlans.text(lot, "code")) { plan -> SitePlans.removeLot(plan, id) })
                }
            }
        }
    }
}

private fun statusTone(status: String): Color = when (status) {
    "AVAILABLE" -> ArizColors.Success
    "RESERVED", "RENTED" -> ArizColors.Warning
    "SOLD" -> ArizColors.Error
    else -> ArizColors.Muted
}

// ---------- departamentos ----------

@Composable
private fun ApartmentsSection(state: SitePlanUiState, onDialog: (PlanDialog) -> Unit, onFloorImage: (String, String, String) -> Unit) {
    val hasPoint = state.doc?.latitude != null && state.doc?.longitude != null
    if (!hasPoint) ArizAlert(message = stringResource(R.string.adm_plan_needs_point))
    ArizSecondaryButton(onClick = { onDialog(PlanDialog.NewBuilding) }, modifier = Modifier.fillMaxWidth(), enabled = hasPoint) {
        Text(stringResource(R.string.adm_plan_add_building), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
    }
    SitePlans.buildings(state.plan).forEach { building ->
        val buildingId = SitePlans.id(building)
        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(SitePlans.text(building, "name"), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                LinkText(stringResource(R.string.adm_plan_add_floor)) { onDialog(PlanDialog.NewFloor(buildingId)) }
                LinkText(stringResource(R.string.adm_remove), ArizColors.Error) {
                    onDialog(PlanDialog.Remove(SitePlans.text(building, "name")) { plan -> SitePlans.removeBuilding(plan, buildingId) })
                }
            }
            SitePlans.floors(building).forEach { floor ->
                FloorBlock(building = buildingId, floor = floor, uploading = state.uploading, onDialog = onDialog, onFloorImage = onFloorImage)
            }
        }
    }
}

@Composable
private fun FloorBlock(building: String, floor: JsonObject, uploading: Boolean, onDialog: (PlanDialog) -> Unit, onFloorImage: (String, String, String) -> Unit) {
    val floorId = SitePlans.id(floor)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) onFloorImage(building, floorId, uri.toString()) }
    val hasImage = floor["image"] is JsonObject
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = ArizColors.SurfaceMuted) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    SitePlans.text(floor, "name") + " (" + SitePlans.text(floor, "number") + ")",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ArizColors.Brand,
                )
                LinkText(stringResource(R.string.adm_remove), ArizColors.Error) {
                    onDialog(PlanDialog.Remove(SitePlans.text(floor, "name")) { plan -> SitePlans.removeFloor(plan, building, floorId) })
                }
            }
            SmallActions {
                LinkText(stringResource(if (hasImage) R.string.adm_plan_change_image else R.string.adm_plan_upload_image), enabled = !uploading) {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                LinkText(stringResource(R.string.adm_plan_add_apartment)) {
                    onDialog(PlanDialog.Apartment(building, floorId, SitePlans.newApartment(emptyList()), isNew = true))
                }
            }
            SitePlans.apartments(floor).forEach { apt ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(SitePlans.text(apt, "code"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                        Text(
                            stringResource(R.string.adm_plan_apt_line, SitePlans.text(apt, "areaM2"), SitePlans.text(apt, "price")),
                            style = MaterialTheme.typography.bodySmall,
                            color = ArizColors.Muted,
                        )
                    }
                    val status = SitePlans.text(apt, "status")
                    StatusPill(ApartmentStatusLabels[status] ?: status, statusTone(status))
                    LinkText(stringResource(R.string.adm_edit)) { onDialog(PlanDialog.Apartment(building, floorId, apt, isNew = false)) }
                    LinkText(stringResource(R.string.adm_remove), ArizColors.Error) {
                        val aptId = SitePlans.id(apt)
                        onDialog(PlanDialog.Remove(SitePlans.text(apt, "code")) { plan -> SitePlans.removeApartment(plan, building, floorId, aptId) })
                    }
                }
            }
        }
    }
}

// ---------- diálogos ----------

@Composable
private fun PlanDialogs(state: SitePlanUiState, dialog: PlanDialog?, onClose: () -> Unit, viewModel: SitePlanViewModel) {
    when (dialog) {
        null -> Unit
        is PlanDialog.Lot -> RecordFormDialog(
            title = stringResource(if (dialog.isNew) R.string.adm_plan_new_lot else R.string.adm_plan_edit_lot),
            spec = SitePlans.LotSpec,
            initial = SitePlans.valuesOf(dialog.lot, SitePlans.LotSpec),
            onDismiss = onClose,
        ) { values ->
            val (lot, invalid) = SitePlans.applyValues(dialog.lot, SitePlans.LotSpec, values)
            if (invalid.isEmpty()) {
                viewModel.edit { SitePlans.upsertLot(it, lot) }
                onClose()
            }
            invalid
        }
        PlanDialog.NewBuilding -> RecordFormDialog(
            title = stringResource(R.string.adm_plan_add_building),
            spec = BuildingSpec,
            initial = BuildingSpec.fields.associate { it.key to it.default },
            onDismiss = onClose,
        ) { values ->
            val (obj, invalid) = SitePlans.applyValues(JsonObject(emptyMap()), BuildingSpec, values)
            val doc = state.doc
            val lat = doc?.latitude
            val lng = doc?.longitude
            if (invalid.isEmpty() && lat != null && lng != null) {
                val name = SitePlans.text(obj, "name")
                val width = (obj["widthM"] as JsonPrimitive).content.toDouble()
                val length = (obj["lengthM"] as JsonPrimitive).content.toDouble()
                viewModel.edit { SitePlans.addBuilding(it, name, width, length, lat, lng) }
                onClose()
            }
            invalid
        }
        is PlanDialog.NewFloor -> RecordFormDialog(
            title = stringResource(R.string.adm_plan_add_floor),
            spec = SitePlans.FloorSpec,
            initial = mapOf("number" to "1", "name" to "Piso 1"),
            onDismiss = onClose,
        ) { values ->
            val (obj, invalid) = SitePlans.applyValues(JsonObject(emptyMap()), SitePlans.FloorSpec, values)
            if (invalid.isEmpty()) {
                val number = (obj["number"] as JsonPrimitive).content.toInt()
                viewModel.edit { SitePlans.addFloor(it, dialog.buildingId, number, SitePlans.text(obj, "name")) }
                onClose()
            }
            invalid
        }
        is PlanDialog.Apartment -> ApartmentDialog(state, dialog, onDismiss = onClose) { apartment ->
            viewModel.edit { SitePlans.upsertApartment(it, dialog.buildingId, dialog.floorId, apartment) }
            onClose()
        }
        is PlanDialog.Remove -> ConfirmDialog(
            title = stringResource(R.string.adm_plan_remove_title),
            text = stringResource(R.string.adm_plan_remove_text, dialog.label),
            confirmLabel = stringResource(R.string.adm_remove),
            dismissLabel = stringResource(R.string.adm_cancel),
            onConfirm = {
                viewModel.edit(dialog.remove)
                onClose()
            },
            onDismiss = onClose,
        )
    }
}

/** Diálogo de formulario dirigido por un [AdminFormSpec]; [onConfirm] devuelve las claves inválidas (vacío = listo). */
@Composable
internal fun RecordFormDialog(
    title: String,
    spec: AdminFormSpec,
    initial: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Set<String>,
) {
    var values by rememberSaveable { mutableStateOf(initial) }
    var invalid by remember { mutableStateOf(emptySet<String>()) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    spec.fields.forEach { field ->
                        FieldInput(
                            field = field,
                            value = values[field.key].orEmpty(),
                            invalid = field.key in invalid,
                            choices = emptyList(),
                            uploading = false,
                            onChange = { values = values + (field.key to it); invalid = invalid - field.key },
                            onImage = {},
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.adm_cancel)) }
                    TextButton(onClick = { invalid = onConfirm(values) }) { Text(stringResource(R.string.adm_save), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

/** Departamento: dibujo sobre el plano del piso (toques) + datos. */
@Composable
private fun ApartmentDialog(state: SitePlanUiState, dialog: PlanDialog.Apartment, onDismiss: () -> Unit, onSave: (JsonObject) -> Unit) {
    val spec = SitePlans.ApartmentSpec
    val floor = SitePlans.buildings(state.plan).firstOrNull { SitePlans.id(it) == dialog.buildingId }
        ?.let { b -> SitePlans.floors(b).firstOrNull { SitePlans.id(it) == dialog.floorId } }
    val image = floor?.get("image") as? JsonObject
    val imageUrl = image?.let { SitePlans.text(it, "url") }?.ifBlank { null }
    val aspect = image?.let { (SitePlans.text(it, "width").toFloatOrNull() ?: 4f) / (SitePlans.text(it, "height").toFloatOrNull() ?: 3f) } ?: (4f / 3f)
    val others = floor?.let { SitePlans.apartments(it).filter { a -> SitePlans.id(a) != SitePlans.id(dialog.apartment) }.map { a -> SitePlans.pathOf(a) } }.orEmpty()

    var points by remember { mutableStateOf(SitePlans.pathOf(dialog.apartment)) }
    var values by rememberSaveable { mutableStateOf(SitePlans.valuesOf(dialog.apartment, spec)) }
    var invalid by remember { mutableStateOf(emptySet<String>()) }
    var pathError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().padding(8.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(if (dialog.isNew) R.string.adm_plan_new_apartment else R.string.adm_plan_edit_apartment),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
                Text(stringResource(R.string.adm_plan_draw_hint), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                PolygonPad(imageUrl, aspect, points, others, onChange = { points = it; pathError = false })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { points = points.dropLast(1) }, enabled = points.isNotEmpty()) { Text(stringResource(R.string.adm_plan_undo)) }
                    TextButton(onClick = { points = emptyList() }, enabled = points.isNotEmpty()) { Text(stringResource(R.string.adm_plan_clear)) }
                }
                if (pathError) Text(stringResource(R.string.adm_plan_draw_invalid), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                spec.fields.forEach { field ->
                    FieldInput(
                        field = field,
                        value = values[field.key].orEmpty(),
                        invalid = field.key in invalid,
                        choices = emptyList(),
                        uploading = false,
                        onChange = { values = values + (field.key to it); invalid = invalid - field.key },
                        onImage = {},
                    )
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.adm_cancel)) }
                    TextButton(onClick = {
                        val (apartment, bad) = SitePlans.applyValues(dialog.apartment, spec, values, optionalNumbersWhenBlank = true)
                        val drawn = SitePlans.isSimplePolygon(points)
                        invalid = bad
                        pathError = !drawn
                        if (bad.isEmpty() && drawn) onSave(SitePlans.withPath(apartment, points))
                    }) { Text(stringResource(R.string.adm_save), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

/**
 * Lienzo para dibujar un polígono tocando el plano del piso. Los puntos son normalizados (0..1), como los guarda el
 * backend; los demás departamentos del piso se dibujan atenuados como referencia.
 */
@Composable
private fun PolygonPad(
    imageUrl: String?,
    aspect: Float,
    points: List<Pair<Double, Double>>,
    others: List<List<Pair<Double, Double>>>,
    onChange: (List<Pair<Double, Double>>) -> Unit,
) {
    val currentPoints by rememberUpdatedState(points)
    val currentChange by rememberUpdatedState(onChange)
    Box(Modifier.fillMaxWidth().aspectRatio(aspect).background(Color(0xFFEEF1F7), RoundedCornerShape(8.dp))) {
        if (imageUrl != null) AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        Canvas(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { tap ->
                    val x = (tap.x / size.width).toDouble().coerceIn(0.0, 1.0)
                    val y = (tap.y / size.height).toDouble().coerceIn(0.0, 1.0)
                    currentChange(currentPoints + (x to y))
                }
            },
        ) {
            fun path(list: List<Pair<Double, Double>>, close: Boolean) = Path().apply {
                list.forEachIndexed { i, (x, y) ->
                    val o = Offset((x * size.width).toFloat(), (y * size.height).toFloat())
                    if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                }
                if (close && list.size > 2) close()
            }
            others.filter { it.size > 2 }.forEach { drawPath(path(it, true), Color(0x66F2A900), style = Stroke(width = 2.dp.toPx())) }
            if (points.size > 2) drawPath(path(points, true), Color(0x3300124F))
            drawPath(path(points, points.size > 2), Color(0xFF00124F), style = Stroke(width = 3.dp.toPx()))
            points.forEach { (x, y) -> drawCircle(Color(0xFF00124F), 6.dp.toPx(), Offset((x * size.width).toFloat(), (y * size.height).toFloat())) }
        }
    }
}
