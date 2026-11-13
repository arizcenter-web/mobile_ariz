package com.tecsup.app_ariz.feature.projects.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.projects.ui.detail.ProjectDetailScreen
import com.tecsup.app_ariz.feature.projects.ui.list.ProjectListScreen
import kotlinx.serialization.Serializable

/** Directorio de proyectos. */
@Serializable
data object ProjectsRoute

/** Detalle de un proyecto. `slug` es el identificador público que usan las tarjetas de Inicio y del directorio. */
@Serializable
data class ProjectDetailRoute(val slug: String)

/**
 * Punto de entrada publico de la feature: :app solo conoce esta funcion.
 *
 * @param onProjectClick abrir el detalle de un proyecto; `:app` decide cómo navegar (el directorio y
 * otras pantallas piden "ver este slug" sin conocerse entre sí).
 */
fun NavGraphBuilder.projectsScreen(
    onBackClick: () -> Unit,
    onProjectClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<ProjectsRoute> {
        ProjectListScreen(onBackClick = onBackClick, onProjectClick = onProjectClick, modifier = modifier)
    }
    composable<ProjectDetailRoute> {
        ProjectDetailScreen(onBackClick = onBackClick, modifier = modifier)
    }
}
