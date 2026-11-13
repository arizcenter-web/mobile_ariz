package com.tecsup.app_ariz.feature.rewards.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.rewards.ui.club.ClubScreen
import kotlinx.serialization.Serializable

/**
 * Ariz Club (programa de referentes). `code` es opcional: con un código (enlace de invitación) se
 * entra directo al catálogo; sin él se pide el código de referente.
 */
@Serializable
data class RewardsRoute(val code: String? = null)

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.rewardsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<RewardsRoute> { ClubScreen(onBackClick = onBackClick, modifier = modifier) }
}
