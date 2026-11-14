package com.tecsup.app_ariz.feature.map.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.map.ui.MapScreen
import kotlinx.serialization.Serializable

/** Mapa público de proyectos (la página "Mapa" del web). */
@Serializable
data object MapRoute

/** Punto de entrada público de la feature: :app solo conoce esta función. */
fun NavGraphBuilder.mapScreen(
    onBackClick: () -> Unit,
    onProjectClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<MapRoute> { MapScreen(onBackClick = onBackClick, onProjectClick = onProjectClick, modifier = modifier) }
}
