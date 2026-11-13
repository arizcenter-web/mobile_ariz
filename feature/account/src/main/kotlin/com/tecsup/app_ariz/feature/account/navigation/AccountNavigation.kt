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
 */
fun NavGraphBuilder.accountScreen(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<AccountRoute> { AccountScreen(onLoginClick = onLoginClick, modifier = modifier) }
}
