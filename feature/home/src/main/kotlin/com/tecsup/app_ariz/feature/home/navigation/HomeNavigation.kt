package com.tecsup.app_ariz.feature.home.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/**
 * Lugares a los que el inicio puede llevar al usuario. Home no conoce las demás features: solo
 * pide ir a un destino y `:app` decide a qué pantalla corresponde.
 */
enum class HomeDestination { SEARCH, PROJECTS, SELL, REWARDS, INVESTMENTS, BUILD }

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.homeScreen(
    onDestinationClick: (HomeDestination) -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onProjectClick: (slug: String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<HomeRoute> {
        HomeScreen(
            onDestinationClick = onDestinationClick,
            onPropertyClick = onPropertyClick,
            onProjectClick = onProjectClick,
            onLoginRequired = onLoginRequired,
            modifier = modifier,
        )
    }
}
