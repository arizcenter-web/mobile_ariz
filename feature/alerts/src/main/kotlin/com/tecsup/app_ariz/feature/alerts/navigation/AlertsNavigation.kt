package com.tecsup.app_ariz.feature.alerts.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.alerts.ui.AlertsScreen
import kotlinx.serialization.Serializable

@Serializable
data object AlertsRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.alertsScreen(modifier: Modifier = Modifier) {
    composable<AlertsRoute> { AlertsScreen(modifier) }
}
