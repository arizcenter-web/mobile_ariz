package com.tecsup.app_ariz.feature.favorites.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.favorites.ui.FavoritesScreen
import kotlinx.serialization.Serializable

@Serializable
data object FavoritesRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.favoritesScreen(modifier: Modifier = Modifier) {
    composable<FavoritesRoute> { FavoritesScreen(modifier) }
}
