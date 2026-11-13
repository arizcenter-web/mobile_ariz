package com.tecsup.app_ariz.feature.projects.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.projects.ui.ProjectsScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProjectsRoute

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.projectsScreen(modifier: Modifier = Modifier) {
    composable<ProjectsRoute> { ProjectsScreen(modifier) }
}
