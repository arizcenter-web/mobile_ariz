package com.tecsup.app_ariz.feature.listings.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.listings.ui.ListingsScreen
import com.tecsup.app_ariz.feature.listings.ui.detail.PropertyDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data object ListingsRoute

/** Detalle de una propiedad. `slug` es el identificador público que usan las tarjetas de Inicio y Buscar. */
@Serializable
data class PropertyDetailRoute(val slug: String)

/**
 * Punto de entrada publico de la feature: :app solo conoce esta funcion.
 *
 * @param onPropertyClick abrir otra propiedad (las "similares" del detalle); `:app` decide cómo navegar.
 */
fun NavGraphBuilder.listingsScreen(
    onBackClick: () -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onAgentClick: (agentId: String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<ListingsRoute> { ListingsScreen(modifier) }
    composable<PropertyDetailRoute> {
        PropertyDetailScreen(
            onBackClick = onBackClick,
            onPropertyClick = onPropertyClick,
            onAgentClick = onAgentClick,
            onLoginRequired = onLoginRequired,
            modifier = modifier,
        )
    }
}
