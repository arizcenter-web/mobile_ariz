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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tecsup.app_ariz.feature.account.navigation.AccountRoute
import com.tecsup.app_ariz.feature.account.navigation.accountScreen
import com.tecsup.app_ariz.feature.agents.navigation.AgentProfileRoute
import com.tecsup.app_ariz.feature.agents.navigation.agentsScreen
import com.tecsup.app_ariz.feature.auth.navigation.AuthRoute
import com.tecsup.app_ariz.feature.auth.navigation.authGraph
import com.tecsup.app_ariz.feature.favorites.navigation.FavoritesRoute
import com.tecsup.app_ariz.feature.favorites.navigation.favoritesScreen
import com.tecsup.app_ariz.feature.home.navigation.HomeDestination
import com.tecsup.app_ariz.feature.home.navigation.HomeRoute
import com.tecsup.app_ariz.feature.home.navigation.homeScreen
import com.tecsup.app_ariz.feature.info.navigation.InfoPage
import com.tecsup.app_ariz.feature.info.navigation.InfoPageRoute
import com.tecsup.app_ariz.feature.info.navigation.InfoRoute
import com.tecsup.app_ariz.feature.info.navigation.infoScreen
import com.tecsup.app_ariz.feature.investments.navigation.InvestmentDetailRoute
import com.tecsup.app_ariz.feature.investments.navigation.InvestmentsRoute
import com.tecsup.app_ariz.feature.investments.navigation.investmentsScreen
import com.tecsup.app_ariz.feature.listings.navigation.PropertyDetailRoute
import com.tecsup.app_ariz.feature.listings.navigation.listingsScreen
import com.tecsup.app_ariz.feature.projects.navigation.ProjectDetailRoute
import com.tecsup.app_ariz.feature.projects.navigation.ProjectsRoute
import com.tecsup.app_ariz.feature.projects.navigation.projectsScreen
import com.tecsup.app_ariz.feature.rewards.navigation.RewardsRoute
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

    // Inicio y Buscar tienen una cabecera que llega hasta arriba: solo respetan la barra inferior.
    val isHome = currentDestination?.hierarchy?.any { it.hasRoute(HomeRoute::class) || it.hasRoute(SearchRoute::class) } == true

    val openProperty: (String) -> Unit = { slug -> navController.navigate(PropertyDetailRoute(slug)) }
    val openAgent: (String) -> Unit = { id -> navController.navigate(AgentProfileRoute(id)) }
    val openLogin: () -> Unit = { navController.navigate(AuthRoute) }
    val openProject: (String) -> Unit = { slug -> navController.navigate(ProjectDetailRoute(slug)) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) NavigationBar {
                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true,
                        onClick = { navController.navigateToTopLevel(destination.route) },
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
            // El inicio dibuja su hero bajo la barra de estado: solo respeta la barra inferior.
            modifier = when {
                !showBottomBar -> Modifier
                isHome -> Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                else -> Modifier.padding(innerPadding)
            },
        ) {
            homeScreen(
                onDestinationClick = { destination ->
                    when (destination) {
                        HomeDestination.SEARCH -> navController.navigateToTopLevel(SearchRoute)
                        HomeDestination.PROJECTS -> navController.navigate(ProjectsRoute)
                        HomeDestination.SELL -> navController.navigate(AuthRoute)
                        HomeDestination.REWARDS -> navController.navigate(RewardsRoute())
                        HomeDestination.INVESTMENTS -> navController.navigate(InvestmentsRoute)
                        HomeDestination.BUILD -> navController.navigate(InfoPageRoute(InfoPage.BUILD))
                    }
                },
                onPropertyClick = openProperty,
                onProjectClick = openProject,
                onLoginRequired = openLogin,
            )
            searchScreen(onPropertyClick = openProperty, onLoginRequired = openLogin)
            favoritesScreen(
                onPropertyClick = openProperty,
                onLoginClick = openLogin,
                onExploreClick = { navController.navigateToTopLevel(SearchRoute) },
            )
            accountScreen(
                onLoginClick = openLogin,
                onFavoritesClick = { navController.navigateToTopLevel(FavoritesRoute) },
                onClubClick = { navController.navigate(RewardsRoute()) },
                onInfoClick = { navController.navigate(InfoRoute) },
            )
            authGraph(
                navController = navController,
                onAuthenticated = { navController.popBackStack<AuthRoute>(inclusive = true) },
            )
            listingsScreen(
                onBackClick = { navController.popBackStack() },
                onPropertyClick = openProperty,
                onAgentClick = openAgent,
                onLoginRequired = openLogin,
            )
            projectsScreen(
                onBackClick = { navController.popBackStack() },
                onProjectClick = openProject,
            )
            agentsScreen(
                onBackClick = { navController.popBackStack() },
                onPropertyClick = openProperty,
                onLoginRequired = openLogin,
            )
            rewardsScreen(onBackClick = { navController.popBackStack() })
            infoScreen(
                onBackClick = { navController.popBackStack() },
                onPageClick = { page -> navController.navigate(InfoPageRoute(page)) },
            )
            investmentsScreen(
                onBackClick = { navController.popBackStack() },
                onOpportunityClick = { slug -> navController.navigate(InvestmentDetailRoute(slug)) },
            )
        }
    }
}

/** Navegación entre pestañas principales: conserva el estado de cada una y no apila copias. */
private fun NavController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
