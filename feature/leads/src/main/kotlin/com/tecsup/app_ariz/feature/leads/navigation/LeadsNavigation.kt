package com.tecsup.app_ariz.feature.leads.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.leads.ui.LeadsScreen
import kotlinx.serialization.Serializable

@Serializable
data object LeadsRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.leadsScreen(modifier: Modifier = Modifier) {
    composable<LeadsRoute> { LeadsScreen(modifier) }
}
