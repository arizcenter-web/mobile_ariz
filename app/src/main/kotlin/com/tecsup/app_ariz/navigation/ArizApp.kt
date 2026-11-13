package com.tecsup.app_ariz.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tecsup.app_ariz.feature.account.navigation.AccountRoute
import com.tecsup.app_ariz.feature.account.navigation.accountScreen
import com.tecsup.app_ariz.feature.agents.navigation.agentsScreen
import com.tecsup.app_ariz.feature.alerts.navigation.alertsScreen
import com.tecsup.app_ariz.feature.auth.navigation.AuthRoute
import com.tecsup.app_ariz.feature.auth.navigation.authGraph
import com.tecsup.app_ariz.feature.favorites.navigation.FavoritesRoute
import com.tecsup.app_ariz.feature.favorites.navigation.favoritesScreen
import com.tecsup.app_ariz.feature.home.navigation.HomeRoute
import com.tecsup.app_ariz.feature.home.navigation.homeScreen
import com.tecsup.app_ariz.feature.leads.navigation.leadsScreen
import com.tecsup.app_ariz.feature.listings.navigation.listingsScreen
import com.tecsup.app_ariz.feature.projects.navigation.projectsScreen
import com.tecsup.app_ariz.feature.rewards.navigation.rewardsScreen
import com.tecsup.app_ariz.feature.search.navigation.SearchRoute
import com.tecsup.app_ariz.feature.search.navigation.searchScreen
import kotlin.reflect.KClass

private data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(HomeRoute, HomeRoute::class, "Inicio", Icons.Outlined.Home),
    TopLevelDestination(SearchRoute, SearchRoute::class, "Buscar", Icons.Outlined.Search),
    TopLevelDestination(FavoritesRoute, FavoritesRoute::class, "Favoritos", Icons.Outlined.FavoriteBorder),
    TopLevelDestination(AccountRoute, AccountRoute::class, "Cuenta", Icons.Outlined.AccountCircle),
)

/**
 * Raiz de la app: barra inferior + NavHost. Cada feature registra su grafo con una sola funcion
 * (`xxxScreen()`); aqui no se importa nada interno de las features.
 */
@Composable
fun ArizApp(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    LaunchedEffect(Unit) {
        viewModel.sessionExpired.collect {
            viewModel.onSessionExpired()
            navController.navigate(AuthRoute) { launchSingleTop = true }
        }
    }

    // La barra inferior solo aparece en las pestañas principales; login/registro ocupan toda la pantalla.
    val showBottomBar = topLevelDestinations.any { destination ->
        currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) NavigationBar {
                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            // Las pantallas de auth gestionan sus propios insets (foto a pantalla completa).
            modifier = if (showBottomBar) Modifier.padding(innerPadding) else Modifier,
        ) {
            homeScreen()
            searchScreen()
            favoritesScreen()
            accountScreen(onLoginClick = { navController.navigate(AuthRoute) })
            authGraph(
                navController = navController,
                onAuthenticated = { navController.popBackStack<AuthRoute>(inclusive = true) },
            )
            listingsScreen()
            projectsScreen()
            leadsScreen()
            alertsScreen()
            agentsScreen()
            rewardsScreen()
        }
    }
}
