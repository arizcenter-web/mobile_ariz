package com.tecsup.app_ariz.feature.search.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.search.ui.SearchScreen
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.searchScreen(modifier: Modifier = Modifier) {
    composable<SearchRoute> { SearchScreen(modifier) }
}
