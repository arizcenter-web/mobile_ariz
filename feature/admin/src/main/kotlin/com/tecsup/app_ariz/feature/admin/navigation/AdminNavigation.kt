package com.tecsup.app_ariz.feature.admin.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tecsup.app_ariz.feature.admin.ui.AdminDashboardScreen
import com.tecsup.app_ariz.feature.admin.ui.AdminFormScreen
import com.tecsup.app_ariz.feature.admin.ui.AdminSectionScreen
import com.tecsup.app_ariz.feature.admin.ui.SitePlanScreen
import kotlinx.serialization.Serializable

/** Panel del administrador: dashboard + secciones de gestión. */
@Serializable
data object AdminRoute

@Serializable
internal data object AdminDashboardRoute

/** `key` es la clave de la sección (ver `AdminSections`); el `AdminSectionViewModel` la lee con ese mismo nombre. */
@Serializable
internal data class AdminSectionRoute(val key: String)

/** Plano del proyecto `id` (inventario de lotes o edificios, pisos y departamentos). */
@Serializable
internal data class AdminSitePlanRoute(val id: String)

/** Alta (`id == null`) o edición de un registro de la sección `key`. */
@Serializable
internal data class AdminFormRoute(val key: String, val id: String? = null)

/** [onPropertyForm]: abre el formulario de propiedad de ARIZ (lo aporta la app; los features no se conocen). */
fun NavGraphBuilder.adminGraph(
    navController: NavController,
    onExit: () -> Unit,
    onPropertyForm: (id: String?) -> Unit,
    onProjectForm: (id: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    navigation<AdminRoute>(startDestination = AdminDashboardRoute) {
        composable<AdminDashboardRoute> {
            AdminDashboardScreen(
                onBackClick = onExit,
                onSectionClick = { key -> navController.navigate(AdminSectionRoute(key)) },
                modifier = modifier,
            )
        }
        composable<AdminSectionRoute> {
            AdminSectionScreen(
                onBackClick = { navController.popBackStack() },
                onSitePlan = { id -> navController.navigate(AdminSitePlanRoute(id)) },
                onFormClick = { key, id ->
                    when (key) {
                        "properties" -> onPropertyForm(id)
                        "projects" -> onProjectForm(id)
                        else -> navController.navigate(AdminFormRoute(key, id))
                    }
                },
                modifier = modifier,
            )
        }
        composable<AdminSitePlanRoute> {
            SitePlanScreen(onBackClick = { navController.popBackStack() }, modifier = modifier)
        }
        composable<AdminFormRoute> {
            AdminFormScreen(onBackClick = { navController.popBackStack() }, onSaved = { navController.popBackStack() }, modifier = modifier)
        }
    }
}
