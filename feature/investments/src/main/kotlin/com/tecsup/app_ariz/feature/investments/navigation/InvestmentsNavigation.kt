package com.tecsup.app_ariz.feature.investments.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tecsup.app_ariz.feature.investments.ui.detail.InvestmentDetailScreen
import com.tecsup.app_ariz.feature.investments.ui.list.InvestmentListScreen
import kotlinx.serialization.Serializable

/** Catálogo de oportunidades de inversión con su calculadora. */
@Serializable
data object InvestmentsRoute

/** Detalle de una oportunidad. `slug` es su identificador público. */
@Serializable
data class InvestmentDetailRoute(val slug: String)

/** Punto de entrada publico de la feature: :app solo conoce esta funcion. */
fun NavGraphBuilder.investmentsScreen(
    onBackClick: () -> Unit,
    onOpportunityClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<InvestmentsRoute> {
        InvestmentListScreen(onBackClick = onBackClick, onOpportunityClick = onOpportunityClick, modifier = modifier)
    }
    composable<InvestmentDetailRoute> { InvestmentDetailScreen(onBackClick = onBackClick, modifier = modifier) }
}
