package com.tecsup.app_ariz.feature.home.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.homeScreen(modifier: Modifier = Modifier) {
    composable<HomeRoute> { HomeScreen(modifier) }
}
