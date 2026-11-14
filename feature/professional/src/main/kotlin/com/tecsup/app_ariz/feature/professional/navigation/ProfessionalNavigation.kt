package com.tecsup.app_ariz.feature.professional.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.tecsup.app_ariz.feature.professional.ui.DashboardScreen
import com.tecsup.app_ariz.feature.professional.ui.LeadsScreen
import com.tecsup.app_ariz.feature.professional.ui.ProDestinations
import com.tecsup.app_ariz.feature.professional.ui.ProfileScreen
import com.tecsup.app_ariz.feature.professional.ui.ProjectFormScreen
import com.tecsup.app_ariz.feature.professional.ui.ProjectsScreen
import com.tecsup.app_ariz.feature.professional.ui.PropertiesScreen
import com.tecsup.app_ariz.feature.professional.ui.PropertyFormScreen
import com.tecsup.app_ariz.feature.professional.ui.SettingsScreen
import com.tecsup.app_ariz.feature.professional.ui.StatsScreen
import com.tecsup.app_ariz.feature.professional.ui.SubscriptionScreen
import kotlinx.serialization.Serializable

/** Panel del profesional (inmobiliaria, agente o desarrolladora): grafo completo. */
@Serializable
data object ProfessionalRoute

@Serializable
internal data object ProDashboardRoute

/**
 * "Mis propiedades" (con `rentOnly` solo los alquileres). Es pública porque el panel de cliente reutiliza estas
 * pantallas: cualquier cuenta puede publicar y administrar sus propiedades con los mismos endpoints.
 */
@Serializable
data class OwnerPropertiesRoute(val rentOnly: Boolean = false)

/** Crear (`id == null`) o editar una propiedad propia. */
@Serializable
data class OwnerPropertyFormRoute(val id: String? = null, val admin: Boolean = false)

@Serializable
internal data object ProProjectsRoute

@Serializable
data class ProProjectFormRoute(val admin: Boolean = false, val id: String? = null)

@Serializable
internal data object ProLeadsRoute

@Serializable
internal data object ProStatsRoute

@Serializable
internal data object ProSubscriptionRoute

@Serializable
internal data object ProProfileRoute

@Serializable
internal data object ProSettingsRoute

/**
 * Registra el panel del profesional: dashboard con menú, propiedades (listar, crear, editar), proyectos,
 * consultas, estadísticas, suscripción, perfil y configuración.
 *
 * @param onExit salir del panel (volver a la pestaña de la que se vino).
 */
fun NavGraphBuilder.professionalGraph(
    navController: NavController,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    navigation<ProfessionalRoute>(startDestination = ProDashboardRoute) {
        composable<ProDashboardRoute> {
            DashboardScreen(
                onBackClick = onExit,
                destinations = ProDestinations(
                    onProperties = { navController.navigate(OwnerPropertiesRoute()) },
                    onNewProperty = { navController.navigate(OwnerPropertyFormRoute()) },
                    onProjects = { navController.navigate(ProProjectsRoute) },
                    onLeads = { navController.navigate(ProLeadsRoute) },
                    onStats = { navController.navigate(ProStatsRoute) },
                    onSubscription = { navController.navigate(ProSubscriptionRoute) },
                    onProfile = { navController.navigate(ProProfileRoute) },
                    onSettings = { navController.navigate(ProSettingsRoute) },
                ),
                modifier = modifier,
            )
        }
        composable<OwnerPropertiesRoute> { entry ->
            PropertiesScreen(
                rentOnly = entry.toRoute<OwnerPropertiesRoute>().rentOnly,
                onBackClick = { navController.popBackStack() },
                onNewClick = { navController.navigate(OwnerPropertyFormRoute()) },
                onEditClick = { id -> navController.navigate(OwnerPropertyFormRoute(id)) },
                modifier = modifier,
            )
        }
        composable<OwnerPropertyFormRoute> { entry ->
            val admin = entry.toRoute<OwnerPropertyFormRoute>().admin
            PropertyFormScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = {
                    if (admin) {
                        // Admin: vuelve a su lista de propiedades de ARIZ.
                        navController.popBackStack()
                        return@PropertyFormScreen
                    }
                    // Tras guardar se vuelve a "Mis propiedades" (si se vino del dashboard, se abre esa lista).
                    if (!navController.popBackStack<OwnerPropertiesRoute>(inclusive = false)) {
                        navController.popBackStack()
                        navController.navigate(OwnerPropertiesRoute())
                    }
                },
                modifier = modifier,
            )
        }
        composable<ProProjectsRoute> {
            ProjectsScreen(
                onBackClick = { navController.popBackStack() },
                onNewClick = { navController.navigate(ProProjectFormRoute()) },
                onEditClick = { id -> navController.navigate(ProProjectFormRoute(id = id)) },
                modifier = modifier,
            )
        }
        composable<ProProjectFormRoute> { 
            ProjectFormScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                modifier = modifier,
            )
        }
        composable<ProLeadsRoute> { LeadsScreen(onBackClick = { navController.popBackStack() }, modifier = modifier) }
        composable<ProStatsRoute> { StatsScreen(onBackClick = { navController.popBackStack() }, modifier = modifier) }
        composable<ProSubscriptionRoute> { SubscriptionScreen(onBackClick = { navController.popBackStack() }, modifier = modifier) }
        composable<ProProfileRoute> { ProfileScreen(onBackClick = { navController.popBackStack() }, modifier = modifier) }
        composable<ProSettingsRoute> { SettingsScreen(onBackClick = { navController.popBackStack() }, modifier = modifier) }
    }
}
