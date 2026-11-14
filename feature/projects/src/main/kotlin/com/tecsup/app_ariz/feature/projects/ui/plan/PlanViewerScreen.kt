package com.tecsup.app_ariz.feature.projects.ui.plan

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.feature.projects.R
import com.tecsup.app_ariz.feature.projects.domain.model.LotStatus
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlan
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlanApartment
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlanLot
import java.io.File

/**
 * Plano publicado del proyecto (el web lo muestra con un código que coordina un asesor): lista con financiamiento y
 * ficha PDF por lote o departamento, y mapa con los lotes pintados por estado.
 */
@Composable
fun PlanViewerScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: PlanViewerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.files.collect { openSheet(context, it) } }
    var showMap by rememberSaveable { mutableStateOf(false) }
    val plan = state.plan

    if (plan != null && showMap && plan.hasMap) {
        PlanMap(plan, state.code, onClose = { showMap = false }, onSheet = { lot -> viewModel.downloadSheet(lot.id, false, lot.code) }, downloadingId = state.downloadingId)
        return
    }

    Column(modifier.fillMaxSize().background(ArizColors.SectionCanvas).statusBarsPadding().imePadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.projects_back), tint = ArizColors.Brand) }
            Text(stringResource(R.string.projects_plan_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (plan == null) {
                Text(stringResource(R.string.projects_plan_code_text), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
                ArizTextField(
                    value = state.code,
                    onValueChange = viewModel::onCodeChange,
                    label = stringResource(R.string.projects_plan_code_label),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    onImeAction = viewModel::load,
                )
                state.error?.let { ArizAlert(message = it.message) }
                if (state.notPublished) ArizAlert(message = stringResource(R.string.projects_plan_not_published))
                ArizPrimaryButton(
                    text = stringResource(R.string.projects_plan_open),
                    onClick = viewModel::load,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = state.isLoading,
                    enabled = state.code.length == AccessCodeLength,
                )
            } else {
                PlanContent(plan, state, onFilter = viewModel::setFilter, onSheet = viewModel::downloadSheet, onMap = { showMap = true }, onLock = viewModel::lock)
            }
        }
    }
}

@Composable
private fun PlanContent(
    plan: PublicPlan,
    state: PlanViewerUiState,
    onFilter: (String?) -> Unit,
    onSheet: (id: String, apartment: Boolean, label: String) -> Unit,
    onMap: () -> Unit,
    onLock: () -> Unit,
) {
    val statuses = if (plan.isApartments) plan.apartments.map { it.status }.distinct() else plan.lots.map { it.status.name }.distinct()
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = state.filter == null, onClick = { onFilter(null) }, label = { Text(stringResource(R.string.projects_plan_all)) })
        statuses.forEach { status ->
            FilterChip(selected = state.filter == status, onClick = { onFilter(status) }, label = { Text(statusLabel(status)) })
        }
        if (plan.hasMap) FilterChip(selected = false, onClick = onMap, label = { Text(stringResource(R.string.projects_plan_map)) })
    }
    state.error?.let { ArizAlert(message = it.message) }
    if (plan.isApartments) {
        val items = plan.apartments.filter { state.filter == null || it.status == state.filter }
        if (items.isEmpty()) Text(stringResource(R.string.projects_plan_empty), color = ArizColors.Muted)
        items.forEach { ApartmentCard(it, plan.currency, busy = state.downloadingId == it.id) { onSheet(it.id, true, it.code) } }
    } else {
        val items = plan.lots.filter { state.filter == null || it.status.name == state.filter }
        if (items.isEmpty()) Text(stringResource(R.string.projects_plan_empty), color = ArizColors.Muted)
        items.forEach { LotCard(it, plan.currency, busy = state.downloadingId == it.id) { onSheet(it.id, false, it.code) } }
    }
    TextButton(onClick = onLock) { Text(stringResource(R.string.projects_plan_lock)) }
}

@Composable
private fun statusLabel(status: String): String = stringResource(
    when (status) {
        "AVAILABLE" -> R.string.projects_lot_available
        "RESERVED" -> R.string.projects_lot_reserved
        "SOLD" -> R.string.projects_lot_sold
        "RENTED" -> R.string.projects_plan_rented
        else -> R.string.projects_lot_blocked
    },
)

private fun statusColor(status: String): Color = when (status) {
    "AVAILABLE" -> Color(0xFF2E9E5B)
    "RESERVED", "RENTED" -> Color(0xFFF2A900)
    "SOLD" -> Color(0xFFD64545)
    else -> Color(0xFF6B7280)
}

@Composable
private fun PlanCard(title: String, status: String, lines: List<String>, busy: Boolean, onSheet: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, ArizColors.Edge)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                Text(statusLabel(status), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = statusColor(status))
            }
            lines.filter { it.isNotBlank() }.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted) }
            TextButton(onClick = onSheet, enabled = !busy) {
                Text(stringResource(if (busy) R.string.projects_plan_downloading else R.string.projects_plan_sheet), fontWeight = FontWeight.Bold, color = ArizColors.Brand)
            }
        }
    }
}

@Composable
private fun LotCard(lot: PublicPlanLot, currency: Currency, busy: Boolean, onSheet: () -> Unit) {
    val finance = if (lot.financingAllowed) {
        stringResource(R.string.projects_plan_finance, formatPrice(lot.financedPrice, currency), formatPrice(lot.downPayment, currency), lot.installments, formatPrice(lot.monthlyPayment, currency))
    } else ""
    PlanCard(
        title = (if (lot.block.isNotBlank()) lot.block + "-" else "") + lot.code,
        status = lot.status.name,
        lines = listOf(
            stringResource(R.string.projects_plan_area_price, lot.areaM2.toInt(), if (lot.cashAllowed) formatPrice(lot.price, currency) else "-"),
            lot.address,
            finance,
            lot.notes,
        ),
        busy = busy,
        onSheet = onSheet,
    )
}

@Composable
private fun ApartmentCard(apt: PublicPlanApartment, currency: Currency, busy: Boolean, onSheet: () -> Unit) {
    val finance = if (apt.financingAllowed) {
        stringResource(R.string.projects_plan_finance, formatPrice(apt.financedPrice, currency), formatPrice(apt.downPayment, currency), apt.installments, formatPrice(apt.monthlyPayment, currency))
    } else ""
    PlanCard(
        title = apt.code,
        status = apt.status,
        lines = listOf(
            apt.building + " · " + apt.floor,
            stringResource(R.string.projects_plan_area_price, apt.areaM2.toInt(), formatPrice(apt.price, currency)),
            stringResource(R.string.projects_plan_rooms, apt.bedrooms, apt.bathrooms),
            finance,
            apt.notes,
        ),
        busy = busy,
        onSheet = onSheet,
    )
}

/** Mapa del plano: imagen superpuesta y lotes pintados por estado; tocar un lote muestra sus datos. */
@Composable
private fun PlanMap(plan: PublicPlan, code: String, onClose: () -> Unit, onSheet: (PublicPlanLot) -> Unit, downloadingId: String?) {
    val context = LocalContext.current
    val first = plan.lots.firstNotNullOfOrNull { it.path.firstOrNull() }
    val overlayCenter = plan.overlay?.let { LatLng(it.lat, it.lng) }
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(overlayCenter ?: first?.let { LatLng(it.lat, it.lng) } ?: LatLng(-9.19, -75.01), 18f)
    }
    var selected by remember { mutableStateOf<PublicPlanLot?>(null) }
    var mapReady by remember { mutableStateOf(false) }
    var bitmap by remember { mutableStateOf<BitmapDescriptor?>(null) }
    LaunchedEffect(plan.overlay?.url, mapReady) {
        val url = plan.overlay?.url
        if (!mapReady || url.isNullOrBlank()) return@LaunchedEffect
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).size(2048).build()
        (context.imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()?.let { bitmap = BitmapDescriptorFactory.fromBitmap(it) }
    }
    androidx.activity.compose.BackHandler(onBack = onClose)

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(mapType = MapType.HYBRID),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false),
            onMapLoaded = { mapReady = true },
            onMapClick = { selected = null },
        ) {
            val overlay = plan.overlay
            val image = bitmap
            if (overlay != null && image != null) {
                GroundOverlay(
                    position = GroundOverlayPosition.create(LatLng(overlay.lat, overlay.lng), overlay.widthM.toFloat(), overlay.heightM.toFloat()),
                    image = image,
                    bearing = overlay.rotation.toFloat(),
                    transparency = (1.0 - overlay.opacity).toFloat().coerceIn(0f, 1f),
                )
            }
            plan.lots.filter { it.path.size >= 3 }.forEach { lot ->
                val color = statusColor(lot.status.name)
                Polygon(
                    points = lot.path.map { LatLng(it.lat, it.lng) },
                    clickable = true,
                    fillColor = color.copy(alpha = if (selected?.id == lot.id) 0.7f else 0.4f),
                    strokeColor = if (selected?.id == lot.id) Color.White else color,
                    strokeWidth = if (selected?.id == lot.id) 6f else 3f,
                    onClick = { selected = lot },
                )
            }
        }
        Surface(Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
            IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.projects_back), tint = ArizColors.Brand) }
        }
        selected?.let { lot ->
            Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(8.dp)) {
                LotCard(lot, plan.currency, busy = downloadingId == lot.id) { onSheet(lot) }
            }
        }
    }
}

private fun openSheet(context: android.content.Context, path: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.projectfiles", File(path))
    val view = Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/pdf").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val intent = if (view.resolveActivity(context.packageManager) != null) view
    else Intent.createChooser(Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "PDF")
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
