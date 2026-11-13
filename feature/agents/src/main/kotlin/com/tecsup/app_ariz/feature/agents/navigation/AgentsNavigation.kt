package com.tecsup.app_ariz.feature.agents.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.agents.ui.profile.AgentProfileScreen
import kotlinx.serialization.Serializable

/** Perfil público de un anunciante. `id` es el id (o slug) de su perfil profesional. */
@Serializable
data class AgentProfileRoute(val id: String)

/**
 * Punto de entrada publico de la feature: :app solo conoce esta funcion.
 *
 * @param onPropertyClick abrir una propiedad de su portafolio; `:app` decide cómo navegar.
 * @param onLoginRequired tocar un corazón sin sesión: llevar al usuario a iniciar sesión.
 */
fun NavGraphBuilder.agentsScreen(
    onBackClick: () -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<AgentProfileRoute> {
        AgentProfileScreen(
            onBackClick = onBackClick,
            onPropertyClick = onPropertyClick,
            onLoginRequired = onLoginRequired,
            modifier = modifier,
        )
    }
}
