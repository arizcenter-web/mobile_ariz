package com.tecsup.app_ariz.feature.listings.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.listings.ui.ListingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object ListingsRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.listingsScreen(modifier: Modifier = Modifier) {
    composable<ListingsRoute> { ListingsScreen(modifier) }
}
