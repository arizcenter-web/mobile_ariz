package com.tecsup.app_ariz.feature.customer.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tecsup.app_ariz.feature.customer.ui.CustomerDashboardScreen
import com.tecsup.app_ariz.feature.customer.ui.CustomerDestinations
import com.tecsup.app_ariz.feature.customer.ui.CustomerProfileScreen
import com.tecsup.app_ariz.feature.customer.ui.MembershipScreen
import kotlinx.serialization.Serializable

/** Panel del cliente (propietario particular): grafo con dashboard, membresía y perfil. */
@Serializable
data object CustomerRoute

@Serializable
internal data object CustomerDashboardRoute

@Serializable
internal data object CustomerMembershipRoute

@Serializable
internal data object CustomerProfileRoute

/**
 * @param onExit salir del panel.
 * @param onPublish / [onProperties] / [onRentals]: las pantallas de propiedades viven en `feature:professional`
 * (las features no se conocen entre sí), así que `:app` decide a dónde navegar.
 */
fun NavGraphBuilder.customerGraph(
    navController: NavController,
    onExit: () -> Unit,
    onPublish: () -> Unit,
    onProperties: () -> Unit,
    onRentals: () -> Unit,
    onFavorites: () -> Unit,
    modifier: Modifier = Modifier,
) {
    navigation<CustomerRoute>(startDestination = CustomerDashboardRoute) {
        composable<CustomerDashboardRoute> {
            CustomerDashboardScreen(
                onBackClick = onExit,
                destinations = CustomerDestinations(
                    onPublish = onPublish,
                    onProperties = onProperties,
                    onRentals = onRentals,
                    onMembership = { navController.navigate(CustomerMembershipRoute) },
                    onProfile = { navController.navigate(CustomerProfileRoute) },
                    onFavorites = onFavorites,
                ),
                modifier = modifier,
            )
        }
        composable<CustomerMembershipRoute> { MembershipScreen(onBackClick = { navController.popBackStack() }, modifier = modifier) }
        composable<CustomerProfileRoute> { CustomerProfileScreen(onBackClick = { navController.popBackStack() }, modifier = modifier) }
    }
}
