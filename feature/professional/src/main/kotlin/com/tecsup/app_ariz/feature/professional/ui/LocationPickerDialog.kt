package com.tecsup.app_ariz.feature.professional.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.professional.R
import java.util.Locale

/**
 * Elegir el punto exacto del proyecto tocando el mapa (en el web es un selector sobre Google Maps). Parte de las
 * coordenadas que ya se escribieron, o de Perú si no hay ninguna.
 */
@Composable
internal fun LocationPickerDialog(
    initialLat: Double?,
    initialLng: Double?,
    onDismiss: () -> Unit,
    onPicked: (lat: Double, lng: Double) -> Unit,
) {
    val start = if (initialLat != null && initialLng != null) LatLng(initialLat, initialLng) else null
    val camera = rememberCameraPositionState {
        position = if (start != null) CameraPosition.fromLatLngZoom(start, 17f) else CameraPosition.fromLatLngZoom(LatLng(-9.19, -75.01), 5f)
    }
    var picked by remember { mutableStateOf(start) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                properties = MapProperties(mapType = MapType.HYBRID),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false),
                onMapClick = { picked = it },
            ) {
                picked?.let { Marker(state = rememberMarkerState(position = it)) }
            }
            Surface(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
            ) {
                Text(stringResource(R.string.pro_map_pick_hint), Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Ink)
            }
            Surface(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        picked?.let { String.format(Locale.US, "%.6f, %.6f", it.latitude, it.longitude) } ?: stringResource(R.string.pro_map_pick_none),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ArizColors.Brand,
                    )
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.pro_cancel)) }
                        TextButton(onClick = { picked?.let { onPicked(it.latitude, it.longitude) } }, enabled = picked != null) {
                            Text(stringResource(R.string.pro_map_pick_use), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
