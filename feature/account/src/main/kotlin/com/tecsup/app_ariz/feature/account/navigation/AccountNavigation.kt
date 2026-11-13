package com.tecsup.app_ariz.feature.account.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.account.ui.account.AccountScreen
import kotlinx.serialization.Serializable

@Serializable
data object AccountRoute

/**
 * @param onLoginClick la feature no conoce `feature:auth`; quien la registra decide a dónde ir.
 * @param onFavoritesClick abrir "Mis favoritos" (otra feature); `:app` decide cómo navegar.
 * @param onClubClick abrir el Ariz Club (feature de recompensas).
 * @param onInfoClick abrir "Ayuda e información" (feature de páginas informativas).
 */
fun NavGraphBuilder.accountScreen(
    onLoginClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onClubClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<AccountRoute> {
        AccountScreen(
            onLoginClick = onLoginClick,
            onFavoritesClick = onFavoritesClick,
            onClubClick = onClubClick,
            onInfoClick = onInfoClick,
            modifier = modifier,
        )
    }
}
