package com.tecsup.app_ariz.feature.agents.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.agents.ui.AgentsScreen
import kotlinx.serialization.Serializable

@Serializable
data object AgentsRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.agentsScreen(modifier: Modifier = Modifier) {
    composable<AgentsRoute> { AgentsScreen(modifier) }
}
