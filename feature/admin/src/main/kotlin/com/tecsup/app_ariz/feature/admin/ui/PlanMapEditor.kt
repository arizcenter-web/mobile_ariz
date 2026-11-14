package com.tecsup.app_ariz.feature.admin.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.ConfirmDialog
import com.tecsup.app_ariz.feature.admin.R
import com.tecsup.app_ariz.feature.admin.domain.AdminField
import com.tecsup.app_ariz.feature.admin.domain.AdminFormSpec
import com.tecsup.app_ariz.feature.admin.domain.FieldKind
import com.tecsup.app_ariz.feature.admin.domain.LotStatusLabels
import com.tecsup.app_ariz.feature.admin.domain.SitePlans
import com.tecsup.app_ariz.feature.admin.domain.ZoneKind
import kotlinx.serialization.json.JsonObject
import java.util.Locale
import kotlin.math.pow

private enum class MapTool { VIEW, LOT, ZONE, IMAGE }

/** Lo seleccionado en el mapa. */
private sealed interface MapSelection {
    data class Lot(val id: String) : MapSelection
    data class Zone(val id: String) : MapSelection
}

private sealed interface MapDialog {
    /** Dibujo de lote terminado: elegir si es uno nuevo o de un lote del inventario sin dibujo. */
    data class AssignLot(val path: List<LatLng>) : MapDialog
    data class NewLot(val path: List<LatLng>) : MapDialog
    data class NewZone(val path: List<LatLng>) : MapDialog
    data class EditLot(val lot: JsonObject) : MapDialog
    data class EditZone(val zone: JsonObject) : MapDialog
    data class PdfPage(val uri: String) : MapDialog
    data object Crop : MapDialog
    data class Remove(val selection: MapSelection, val label: String) : MapDialog
}

private val ZoneSpec = AdminFormSpec(
    title = "Zona",
    fields = listOf(
        AdminField("name", "Nombre", required = true),
        AdminField("kind", "Tipo", FieldKind.CHOICE, options = ZoneKind.entries.map { it.name }, optionLabels = ZoneKind.entries.associate { it.name to it.label }, default = "BLOCK"),
    ),
)

private fun List<LatLng>.pairs() = map { it.latitude to it.longitude }

private fun List<Pair<Double, Double>>.latLngs() = map { LatLng(it.first, it.second) }

private fun lotColor(status: String): Color = when (status) {
    "AVAILABLE" -> Color(0xFF2E9E5B)
    "RESERVED" -> Color(0xFFF2A900)
    "SOLD" -> Color(0xFFD64545)
    else -> Color(0xFF6B7280)
}

private fun zoneColor(kind: String): Color = when (kind) {
    "STREET" -> Color(0xFF9CA3AF)
    "PARK" -> Color(0xFF34A853)
    "CONTOUR" -> Color(0xFFFFFFFF)
    else -> Color(0xFF4774BE)
}

/**
 * Editor del plano sobre el mapa (como el del web): imagen del plano colocada sobre el terreno, lotes y zonas
 * dibujados tocando el mapa. Trabaja sobre el mismo plano de trabajo que la pantalla del plano; guardar usa el mismo flujo.
 */
@Composable
internal fun PlanMapEditor(state: SitePlanUiState, viewModel: SitePlanViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val doc = state.doc
    val image = SitePlans.image(state.plan)
    val lots = SitePlans.lots(state.plan)
    val zones = SitePlans.zones(state.plan)

    val camera = rememberCameraPositionState {
        val firstLot = lots.firstNotNullOfOrNull { l -> SitePlans.geoPath(l).firstOrNull() }
        val start = image?.let { it.latitude to it.longitude } ?: firstLot ?: doc?.latitude?.let { la -> doc.longitude?.let { lo -> la to lo } }
        position = if (start != null) CameraPosition.fromLatLngZoom(LatLng(start.first, start.second), 18f) else CameraPosition.fromLatLngZoom(LatLng(-9.19, -75.01), 5f)
    }

    var tool by remember { mutableStateOf(MapTool.VIEW) }
    var draft by remember { mutableStateOf(emptyList<LatLng>()) }
    var selected by remember { mutableStateOf<MapSelection?>(null) }
    var redraw by remember { mutableStateOf<MapSelection?>(null) }
    var dialog by remember { mutableStateOf<MapDialog?>(null) }
    var hint by remember { mutableStateOf<String?>(null) }
    var mapReady by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val invalidDrawing = stringResource(R.string.adm_map_invalid)
    val restoreFailed = stringResource(R.string.adm_map_restore_failed)

    BackHandler(onBack = onClose)

    // La imagen del plano se baja una vez cargado el mapa (el descriptor de bitmap lo exige) y se reutiliza al moverla.
    val imageUrl = image?.url
    LaunchedEffect(imageUrl, mapReady) {
        if (!mapReady || imageUrl.isNullOrBlank()) return@LaunchedEffect
        val request = ImageRequest.Builder(context).data(imageUrl).allowHardware(false).size(2048).build()
        (context.imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()?.let { source = it }
    }
    // Con recorte, el mapa dibuja solo la parte visible de la hoja.
    val crop = image?.crop
    val overlay = remember(source, crop) {
        source?.let { bmp ->
            val visible = if (crop == null) bmp else {
                val x = (crop.left * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                val y = (crop.top * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                val w = ((crop.right - crop.left) * bmp.width).toInt().coerceIn(1, bmp.width - x)
                val h = ((crop.bottom - crop.top) * bmp.height).toInt().coerceIn(1, bmp.height - y)
                android.graphics.Bitmap.createBitmap(bmp, x, y, w, h)
            }
            BitmapDescriptorFactory.fromBitmap(visible)
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) camera.position.target.let { viewModel.placeImage(uri.toString(), it.latitude, it.longitude) }
    }
    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) dialog = MapDialog.PdfPage(uri.toString())
    }

    fun finishDrawing() {
        val path = draft
        if (!SitePlans.isValidGeoPolygon(path.pairs())) {
            hint = invalidDrawing
            return
        }
        val target = redraw
        when {
            target is MapSelection.Lot -> lots.firstOrNull { SitePlans.id(it) == target.id }?.let { lot ->
                viewModel.edit { SitePlans.upsertLot(it, SitePlans.withGeoPath(lot, path.pairs())) }
            }
            target is MapSelection.Zone -> zones.firstOrNull { SitePlans.id(it) == target.id }?.let { zone ->
                viewModel.edit { SitePlans.upsertZone(it, SitePlans.withGeoPath(zone, path.pairs())) }
            }
            tool == MapTool.LOT -> dialog = if (SitePlans.lotsWithoutPath(state.plan).isEmpty()) MapDialog.NewLot(path) else MapDialog.AssignLot(path)
            else -> dialog = MapDialog.NewZone(path)
        }
        if (target != null) {
            redraw = null
            tool = MapTool.VIEW
        }
        draft = emptyList()
        hint = null
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(mapType = MapType.HYBRID),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false),
            onMapLoaded = { mapReady = true },
            onMapClick = { point ->
                when (tool) {
                    MapTool.LOT, MapTool.ZONE -> draft = draft + point
                    MapTool.VIEW -> selected = null
                    MapTool.IMAGE -> Unit
                }
            },
        ) {
            val overlayImage = overlay
            if (image != null && overlayImage != null) {
                GroundOverlay(
                    position = GroundOverlayPosition.create(LatLng(image.latitude, image.longitude), image.widthM.toFloat(), image.heightM.toFloat()),
                    image = overlayImage,
                    bearing = image.rotation.toFloat(),
                    transparency = (1.0 - image.opacity).toFloat().coerceIn(0f, 1f),
                )
            }
            zones.forEach { zone ->
                val points = SitePlans.geoPath(zone).latLngs()
                if (points.size >= 3) {
                    val id = SitePlans.id(zone)
                    val color = zoneColor(SitePlans.text(zone, "kind"))
                    Polygon(
                        points = points,
                        clickable = tool == MapTool.VIEW,
                        fillColor = color.copy(alpha = if (selected == MapSelection.Zone(id)) 0.55f else 0.25f),
                        strokeColor = if (selected == MapSelection.Zone(id)) Color.White else color,
                        strokeWidth = if (selected == MapSelection.Zone(id)) 6f else 3f,
                        onClick = { selected = MapSelection.Zone(id) },
                    )
                }
            }
            lots.forEach { lot ->
                val points = SitePlans.geoPath(lot).latLngs()
                if (points.size >= 3) {
                    val id = SitePlans.id(lot)
                    val color = lotColor(SitePlans.text(lot, "status"))
                    Polygon(
                        points = points,
                        clickable = tool == MapTool.VIEW,
                        fillColor = color.copy(alpha = if (selected == MapSelection.Lot(id)) 0.7f else 0.4f),
                        strokeColor = if (selected == MapSelection.Lot(id)) Color.White else color,
                        strokeWidth = if (selected == MapSelection.Lot(id)) 6f else 3f,
                        onClick = { selected = MapSelection.Lot(id) },
                    )
                }
            }
            // Con un lote o zona elegido (modo Ver) sus esquinas se pueden arrastrar para ajustar el dibujo.
            val sel = selected
            if (tool == MapTool.VIEW && sel != null) {
                val owner = when (sel) {
                    is MapSelection.Lot -> lots.firstOrNull { SitePlans.id(it) == sel.id }
                    is MapSelection.Zone -> zones.firstOrNull { SitePlans.id(it) == sel.id }
                }
                if (owner != null) {
                    val path = SitePlans.geoPath(owner)
                    path.forEachIndexed { index, (lat, lng) ->
                        VertexHandle(key = SitePlans.id(owner) + "-" + index, point = LatLng(lat, lng)) { moved ->
                            val next = path.toMutableList().also { it[index] = moved.latitude to moved.longitude }
                            if (SitePlans.isValidGeoPolygon(next)) {
                                hint = null
                                viewModel.edit { plan ->
                                    when (sel) {
                                        is MapSelection.Lot -> SitePlans.upsertLot(plan, SitePlans.withGeoPath(owner, next))
                                        is MapSelection.Zone -> SitePlans.upsertZone(plan, SitePlans.withGeoPath(owner, next))
                                    }
                                }
                            } else {
                                hint = invalidDrawing
                            }
                        }
                    }
                }
            }
            if (draft.size >= 2) Polyline(points = if (draft.size > 2) draft + draft.first() else draft, color = Color(0xFFFFD54F), width = 6f)
            val dotRadius = (6.0 * 2.0.pow(19.0 - camera.position.zoom.toDouble())).coerceIn(0.3, 500.0)
            draft.forEach { Circle(center = it, radius = dotRadius, fillColor = Color(0xFFFFD54F), strokeColor = Color.White, strokeWidth = 2f) }
        }

        // Barra superior: cerrar, guardar y herramientas.
        Surface(Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(8.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onClose) { Text(stringResource(R.string.adm_map_close), fontWeight = FontWeight.Bold) }
                    Text(
                        if (state.dirty) stringResource(R.string.adm_plan_unsaved) else stringResource(R.string.adm_map_title),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.dirty) ArizColors.Warning else ArizColors.Muted,
                    )
                    TextButton(onClick = { viewModel.save(publish = false) }, enabled = state.dirty && !state.isSaving && !state.uploading) {
                        Text(stringResource(R.string.adm_map_save), fontWeight = FontWeight.Bold)
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MapTool.entries.forEach { option ->
                        FilterChip(
                            selected = tool == option,
                            onClick = { tool = option; draft = emptyList(); hint = null; redraw = null; if (option != MapTool.VIEW) selected = null },
                            label = {
                                Text(
                                    stringResource(
                                        when (option) {
                                            MapTool.VIEW -> R.string.adm_map_tool_view
                                            MapTool.LOT -> R.string.adm_map_tool_lot
                                            MapTool.ZONE -> R.string.adm_map_tool_zone
                                            MapTool.IMAGE -> R.string.adm_map_tool_image
                                        },
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }

        // Panel inferior según la herramienta.
        Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(8.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                if (state.uploading) Text(stringResource(R.string.adm_uploading), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                when (tool) {
                    MapTool.VIEW -> SelectionPanel(
                        state = state,
                        selection = selected,
                        onEdit = { sel ->
                            dialog = when (sel) {
                                is MapSelection.Lot -> lots.firstOrNull { SitePlans.id(it) == sel.id }?.let { MapDialog.EditLot(it) }
                                is MapSelection.Zone -> zones.firstOrNull { SitePlans.id(it) == sel.id }?.let { MapDialog.EditZone(it) }
                            }
                        },
                        onRedraw = { sel -> redraw = sel; tool = if (sel is MapSelection.Lot) MapTool.LOT else MapTool.ZONE; draft = emptyList() },
                        onRemove = { sel, label -> dialog = MapDialog.Remove(sel, label) },
                    )
                    MapTool.LOT, MapTool.ZONE -> {
                        Text(stringResource(R.string.adm_map_draw_hint, draft.size), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Ink)
                        if (draft.size >= 3) {
                            val points = draft.pairs()
                            Text(stringResource(R.string.adm_map_area, SitePlans.geoAreaM2(points), SitePlans.geoPerimeterM(points)), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { draft = draft.dropLast(1) }, enabled = draft.isNotEmpty()) { Text(stringResource(R.string.adm_plan_undo)) }
                            TextButton(onClick = { draft = emptyList() }, enabled = draft.isNotEmpty()) { Text(stringResource(R.string.adm_plan_clear)) }
                            TextButton(onClick = ::finishDrawing, enabled = draft.size >= 3) { Text(stringResource(R.string.adm_map_finish), fontWeight = FontWeight.Black) }
                        }
                    }
                    MapTool.IMAGE -> ImagePanel(
                        image = image,
                        busy = state.uploading,
                        onPickImage = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onPickPdf = { pickPdf.launch(arrayOf("application/pdf")) },
                        onCenterHere = { val t = camera.position.target; viewModel.adjustImage { it.copy(latitude = t.latitude, longitude = t.longitude) } },
                        onAdjust = viewModel::adjustImage,
                        onGoTo = { img -> camera.move(CameraUpdateFactory.newLatLng(LatLng(img.latitude, img.longitude))) },
                        onCrop = { dialog = MapDialog.Crop },
                        onRestoreCrop = { if (!viewModel.restoreCrop()) hint = restoreFailed },
                    )
                }
            }
        }
    }

    MapDialogs(state, dialog, viewModel, camera.position.target, setDialog = { dialog = it }, onRemoved = { selected = null })
}

/** Esquina arrastrable de un polígono: al soltarla se informa la nueva posición. */
@Composable
@GoogleMapComposable
private fun VertexHandle(key: String, point: LatLng, onMoved: (LatLng) -> Unit) {
    val state = rememberMarkerState(key = key, position = point)
    LaunchedEffect(state.dragState) {
        if (state.dragState == DragState.END) onMoved(state.position)
    }
    Marker(
        state = state,
        draggable = true,
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
        zIndex = 2f,
    )
}

@Composable
private fun SelectionPanel(
    state: SitePlanUiState,
    selection: MapSelection?,
    onEdit: (MapSelection) -> Unit,
    onRedraw: (MapSelection) -> Unit,
    onRemove: (MapSelection, String) -> Unit,
) {
    if (selection == null) {
        Text(stringResource(R.string.adm_map_select_hint), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
        val withoutPath = SitePlans.lotsWithoutPath(state.plan).size
        if (withoutPath > 0) Text(stringResource(R.string.adm_map_undrawn, withoutPath), style = MaterialTheme.typography.bodySmall, color = ArizColors.Warning)
        return
    }
    val (title, detail) = when (selection) {
        is MapSelection.Lot -> SitePlans.lots(state.plan).firstOrNull { SitePlans.id(it) == selection.id }?.let {
            SitePlans.text(it, "code") to (LotStatusLabels[SitePlans.text(it, "status")] ?: "") + " · " + SitePlans.text(it, "areaM2") + " m²"
        }
        is MapSelection.Zone -> SitePlans.zones(state.plan).firstOrNull { SitePlans.id(it) == selection.id }?.let {
            SitePlans.text(it, "name") to (ZoneKind.entries.firstOrNull { k -> k.name == SitePlans.text(it, "kind") }?.label ?: "")
        }
    } ?: return
    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
    Text(detail, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = { onEdit(selection) }) { Text(stringResource(R.string.adm_edit), fontWeight = FontWeight.Bold) }
        TextButton(onClick = { onRedraw(selection) }) { Text(stringResource(R.string.adm_map_redraw), fontWeight = FontWeight.Bold) }
        TextButton(onClick = { onRemove(selection, title) }) { Text(stringResource(R.string.adm_remove), fontWeight = FontWeight.Bold, color = ArizColors.Error) }
    }
}

@Composable
private fun ImagePanel(
    image: com.tecsup.app_ariz.feature.admin.domain.PlanImage?,
    busy: Boolean,
    onPickImage: () -> Unit,
    onPickPdf: () -> Unit,
    onCenterHere: () -> Unit,
    onAdjust: ((com.tecsup.app_ariz.feature.admin.domain.PlanImage) -> com.tecsup.app_ariz.feature.admin.domain.PlanImage) -> Unit,
    onGoTo: (com.tecsup.app_ariz.feature.admin.domain.PlanImage) -> Unit,
    onCrop: () -> Unit,
    onRestoreCrop: () -> Unit,
) {
    Text(stringResource(R.string.adm_map_image_hint), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = onPickImage, enabled = !busy) { Text(stringResource(if (image == null) R.string.adm_map_pick_image else R.string.adm_map_replace_image), fontWeight = FontWeight.Bold) }
        TextButton(onClick = onPickPdf, enabled = !busy) { Text(stringResource(R.string.adm_map_pick_pdf), fontWeight = FontWeight.Bold) }
        if (image != null) {
            TextButton(onClick = onCenterHere) { Text(stringResource(R.string.adm_map_center_here), fontWeight = FontWeight.Bold) }
            TextButton(onClick = { onGoTo(image) }) { Text(stringResource(R.string.adm_map_go_image)) }
            TextButton(onClick = onCrop, enabled = !busy) { Text(stringResource(R.string.adm_map_crop), fontWeight = FontWeight.Bold) }
            if (image.crop != null) TextButton(onClick = onRestoreCrop, enabled = !busy) { Text(stringResource(R.string.adm_map_restore_crop)) }
        }
    }
    if (image != null) {
        val ratio = if (image.widthM > 0) image.heightM / image.widthM else 1.0
        SliderRow(stringResource(R.string.adm_map_size, image.widthM.toInt()), image.widthM.toFloat(), 20f..1500f) { w -> onAdjust { it.copy(widthM = w.toDouble(), heightM = w * ratio) } }
        SliderRow(stringResource(R.string.adm_map_rotation, image.rotation.toInt()), image.rotation.toFloat(), -180f..180f) { r -> onAdjust { it.copy(rotation = r.toDouble()) } }
        SliderRow(stringResource(R.string.adm_map_opacity, (image.opacity * 100).toInt()), image.opacity.toFloat(), 0.1f..1f) { o -> onAdjust { it.copy(opacity = o.toDouble()) } }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = ArizColors.Ink)
        Slider(value = value.coerceIn(range), onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun MapDialogs(
    state: SitePlanUiState,
    dialog: MapDialog?,
    viewModel: SitePlanViewModel,
    cameraTarget: LatLng,
    setDialog: (MapDialog?) -> Unit,
    onRemoved: () -> Unit,
) {
    val onClose = { setDialog(null) }
    when (dialog) {
        null -> Unit
        is MapDialog.AssignLot -> AssignDialog(state, dialog.path, onDismiss = onClose, onNew = { setDialog(MapDialog.NewLot(dialog.path)) }, onExisting = { lot ->
            viewModel.edit { SitePlans.upsertLot(it, SitePlans.withGeoPath(lot, dialog.path.pairs())) }
            onClose()
        })
        is MapDialog.NewLot -> {
            val points = dialog.path.pairs()
            val base = SitePlans.withGeoPath(SitePlans.newLot(), points)
            val initial = SitePlans.valuesOf(base, SitePlans.LotSpec) + mapOf(
                "areaM2" to String.format(Locale.US, "%.2f", SitePlans.geoAreaM2(points)),
                "perimeterM" to String.format(Locale.US, "%.2f", SitePlans.geoPerimeterM(points)),
                "frontM" to String.format(Locale.US, "%.2f", SitePlans.geoFrontM(points)),
            )
            RecordFormDialog(stringResource(R.string.adm_plan_new_lot), SitePlans.LotSpec, initial, onDismiss = onClose) { values ->
                val (lot, invalid) = SitePlans.applyValues(base, SitePlans.LotSpec, values)
                if (invalid.isEmpty()) {
                    viewModel.edit { SitePlans.upsertLot(it, lot) }
                    onClose()
                }
                invalid
            }
        }
        is MapDialog.NewZone -> RecordFormDialog(stringResource(R.string.adm_map_new_zone), ZoneSpec, ZoneSpec.fields.associate { it.key to it.default }, onDismiss = onClose) { values ->
            val name = values["name"].orEmpty().trim()
            if (name.isEmpty()) setOf("name") else {
                val kind = ZoneKind.entries.firstOrNull { it.name == values["kind"] } ?: ZoneKind.BLOCK
                viewModel.edit { SitePlans.upsertZone(it, SitePlans.newZone(kind, name, dialog.path.pairs())) }
                onClose()
                emptySet()
            }
        }
        is MapDialog.EditLot -> RecordFormDialog(stringResource(R.string.adm_plan_edit_lot), SitePlans.LotSpec, SitePlans.valuesOf(dialog.lot, SitePlans.LotSpec), onDismiss = onClose) { values ->
            val (lot, invalid) = SitePlans.applyValues(dialog.lot, SitePlans.LotSpec, values)
            if (invalid.isEmpty()) {
                viewModel.edit { SitePlans.upsertLot(it, lot) }
                onClose()
            }
            invalid
        }
        is MapDialog.EditZone -> RecordFormDialog(stringResource(R.string.adm_map_edit_zone), ZoneSpec, SitePlans.valuesOf(dialog.zone, ZoneSpec), onDismiss = onClose) { values ->
            val (zone, invalid) = SitePlans.applyValues(dialog.zone, ZoneSpec, values)
            if (invalid.isEmpty()) {
                viewModel.edit { SitePlans.upsertZone(it, zone) }
                onClose()
            }
            invalid
        }
        is MapDialog.PdfPage -> PdfPageDialog(onDismiss = onClose) { page ->
            viewModel.placePdf(dialog.uri, page, cameraTarget.latitude, cameraTarget.longitude)
            onClose()
        }
        is MapDialog.Crop -> CropDialog(onDismiss = onClose) { l, t, r, b ->
            val ok = viewModel.cropImage(l, t, r, b)
            if (ok) onClose()
            ok
        }
        is MapDialog.Remove -> ConfirmDialog(
            title = stringResource(R.string.adm_plan_remove_title),
            text = stringResource(R.string.adm_plan_remove_text, dialog.label),
            confirmLabel = stringResource(R.string.adm_remove),
            dismissLabel = stringResource(R.string.adm_cancel),
            onConfirm = {
                viewModel.edit { plan ->
                    when (val sel = dialog.selection) {
                        is MapSelection.Lot -> SitePlans.removeLot(plan, sel.id)
                        is MapSelection.Zone -> SitePlans.removeZone(plan, sel.id)
                    }
                }
                onRemoved()
                onClose()
            },
            onDismiss = onClose,
        )
    }
}

@Composable
private fun AssignDialog(
    state: SitePlanUiState,
    path: List<LatLng>,
    onDismiss: () -> Unit,
    onNew: () -> Unit,
    onExisting: (JsonObject) -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.adm_map_assign_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.adm_map_assign_text, SitePlans.geoAreaM2(path.pairs())), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                TextButton(onClick = onNew) { Text(stringResource(R.string.adm_map_assign_new), fontWeight = FontWeight.Bold) }
                SitePlans.lotsWithoutPath(state.plan).forEach { lot ->
                    val block = SitePlans.text(lot, "block")
                    TextButton(onClick = { onExisting(lot) }) { Text((if (block.isNotBlank()) "$block-" else "") + SitePlans.text(lot, "code")) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.adm_cancel)) } },
    )
}

@Composable
private fun CropField(value: String, label: String, onChange: (String) -> Unit) {
    ArizTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }.take(6)) },
        label = label,
        modifier = Modifier.fillMaxWidth(),
        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
    )
}

/** Recorte por porcentajes de lo que hoy se ve de la hoja (izquierda, arriba, derecha, abajo). */
@Composable
private fun CropDialog(onDismiss: () -> Unit, onConfirm: (Double, Double, Double, Double) -> Boolean) {
    var left by remember { mutableStateOf("0") }
    var top by remember { mutableStateOf("0") }
    var right by remember { mutableStateOf("100") }
    var bottom by remember { mutableStateOf("100") }
    var failed by remember { mutableStateOf(false) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.adm_map_crop_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.adm_map_crop_text), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                CropField(left, stringResource(R.string.adm_map_crop_left)) { left = it; failed = false }
                CropField(top, stringResource(R.string.adm_map_crop_top)) { top = it; failed = false }
                CropField(right, stringResource(R.string.adm_map_crop_right)) { right = it; failed = false }
                CropField(bottom, stringResource(R.string.adm_map_crop_bottom)) { bottom = it; failed = false }
                if (failed) Text(stringResource(R.string.adm_map_crop_invalid), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                fun pct(s: String) = (s.toDoubleOrNull() ?: Double.NaN) / 100.0
                failed = !onConfirm(pct(left), pct(top), pct(right), pct(bottom))
            }) { Text(stringResource(R.string.adm_map_crop_apply), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.adm_cancel)) } },
    )
}

@Composable
private fun PdfPageDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var page by remember { mutableStateOf("1") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.adm_map_pdf_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.adm_map_pdf_text), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                ArizTextField(value = page, onValueChange = { page = it.filter(Char::isDigit).take(4) }, label = stringResource(R.string.adm_map_pdf_page), modifier = Modifier.fillMaxWidth(), keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(page.toIntOrNull() ?: 1) }) { Text(stringResource(R.string.adm_map_pdf_use), fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.adm_cancel)) } },
    )
}
