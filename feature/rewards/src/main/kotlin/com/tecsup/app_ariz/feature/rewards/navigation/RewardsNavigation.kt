package com.tecsup.app_ariz.feature.rewards.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.rewards.ui.RewardsScreen
import kotlinx.serialization.Serializable

@Serializable
data object RewardsRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.rewardsScreen(modifier: Modifier = Modifier) {
    composable<RewardsRoute> { RewardsScreen(modifier) }
}
