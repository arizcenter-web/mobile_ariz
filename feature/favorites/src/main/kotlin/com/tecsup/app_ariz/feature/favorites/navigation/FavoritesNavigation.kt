package com.tecsup.app_ariz.feature.favorites.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.favorites.ui.FavoritesScreen
import kotlinx.serialization.Serializable

@Serializable
data object FavoritesRoute

/**
 * Punto de entrada publico de la feature: :app solo conoce esta funcion.
 *
 * La feature no conoce a las demás: pide "abrir esta propiedad", "iniciar sesión" o "explorar" y
 * `:app` decide a qué pantalla corresponde.
 */
fun NavGraphBuilder.favoritesScreen(
    onPropertyClick: (slug: String) -> Unit,
    onLoginClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<FavoritesRoute> {
        FavoritesScreen(
            onPropertyClick = onPropertyClick,
            onLoginClick = onLoginClick,
            onExploreClick = onExploreClick,
            modifier = modifier,
        )
    }
}
