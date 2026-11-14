package com.tecsup.app_ariz.feature.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.feature.map.R

/** Mapa de proyectos: marcadores, búsqueda por texto, filtro por tipo y tarjeta del proyecto elegido. */
@Composable
fun MapScreen(onBackClick: () -> Unit, onProjectClick: (String) -> Unit, modifier: Modifier = Modifier, viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(-9.19, -75.01), 5f) }
    val visible = state.visible

    // Al cargar o filtrar, el mapa encuadra los marcadores visibles.
    LaunchedEffect(visible.map { it.id }) {
        val points = visible.mapNotNull { p -> p.latitude?.let { la -> p.longitude?.let { lo -> LatLng(la, lo) } } }
        when {
            points.size == 1 -> camera.animate(CameraUpdateFactory.newLatLngZoom(points.first(), 15f))
            points.size > 1 -> runCatching {
                val bounds = LatLngBounds.builder().apply { points.forEach(::include) }.build()
                camera.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false),
            onMapClick = { viewModel.select(null) },
        ) {
            visible.forEach { project ->
                val lat = project.latitude ?: return@forEach
                val lng = project.longitude ?: return@forEach
                Marker(
                    state = rememberMarkerState(key = project.id, position = LatLng(lat, lng)),
                    title = project.name,
                    icon = BitmapDescriptorFactory.defaultMarker(if (project.id == state.selectedId) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED),
                    onClick = { viewModel.select(project.id); false },
                )
            }
        }

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
                Column(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.map_back), tint = ArizColors.Brand) }
                        ArizTextField(
                            value = state.query,
                            onValueChange = viewModel::onQuery,
                            label = stringResource(R.string.map_search),
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                        )
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = state.category == null, onClick = { viewModel.onCategory(null) }, label = { Text(stringResource(R.string.map_all)) })
                        FilterChip(selected = state.category == ProjectCategory.LOTIZACION, onClick = { viewModel.onCategory(ProjectCategory.LOTIZACION) }, label = { Text(stringResource(R.string.map_lots)) })
                        FilterChip(selected = state.category == ProjectCategory.DEPARTAMENTOS, onClick = { viewModel.onCategory(ProjectCategory.DEPARTAMENTOS) }, label = { Text(stringResource(R.string.map_apartments)) })
                    }
                }
            }
            val status = when {
                state.isLoading -> stringResource(R.string.map_loading)
                state.error != null -> stringResource(R.string.map_error)
                visible.isEmpty() -> stringResource(R.string.map_empty)
                else -> stringResource(R.string.map_count, visible.size)
            }
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                Text(
                    status,
                    Modifier.clickable(enabled = state.error != null, onClick = viewModel::load).padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ArizColors.Brand,
                )
            }
        }

        state.selected?.let { project ->
            ProjectCard(project, onOpen = { onProjectClick(project.slug) }, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(8.dp))
        }
    }
}

@Composable
private fun ProjectCard(project: ProjectSummary, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onOpen, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(84.dp).clip(RoundedCornerShape(10.dp)).background(ArizColors.SurfaceMuted)) {
                project.coverImageUrl?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(project.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand, maxLines = 2)
                Text(listOf(project.developer, project.district).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted, maxLines = 1)
                Text(stringResource(R.string.map_from, formatPrice(project.cheapestPrice ?: project.priceFrom, project.currency)), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                TextButton(onClick = onOpen) { Text(stringResource(R.string.map_open), fontWeight = FontWeight.Bold) }
            }
        }
    }
}
