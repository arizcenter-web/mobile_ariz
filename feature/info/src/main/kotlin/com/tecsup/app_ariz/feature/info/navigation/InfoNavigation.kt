package com.tecsup.app_ariz.feature.info.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tecsup.app_ariz.feature.info.ui.AboutPage
import com.tecsup.app_ariz.feature.info.ui.BuildPage
import com.tecsup.app_ariz.feature.info.ui.ClaimsPage
import com.tecsup.app_ariz.feature.info.ui.ContactPage
import com.tecsup.app_ariz.feature.info.ui.HelpPage
import com.tecsup.app_ariz.feature.info.ui.InfoHubScreen
import com.tecsup.app_ariz.feature.info.ui.ServicesPage
import com.tecsup.app_ariz.feature.info.ui.TermsPage
import kotlinx.serialization.Serializable

/** Páginas informativas de la web que la app también ofrece. */
@Serializable
enum class InfoPage { ABOUT, SERVICES, BUILD, CONTACT, HELP, TERMS, CLAIMS }

/** Menú con todas las páginas informativas. */
@Serializable
data object InfoRoute

/** Una página informativa concreta. */
@Serializable
data class InfoPageRoute(val page: InfoPage)

/**
 * Punto de entrada publico de la feature: :app solo conoce esta funcion.
 *
 * @param onPageClick abrir otra página informativa (el menú y los enlaces entre páginas).
 */
fun NavGraphBuilder.infoScreen(
    onBackClick: () -> Unit,
    onPageClick: (InfoPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable<InfoRoute> { InfoHubScreen(onBackClick = onBackClick, onPageClick = onPageClick, modifier = modifier) }
    composable<InfoPageRoute> { entry ->
        val onContact = { onPageClick(InfoPage.CONTACT) }
        when (entry.toRoute<InfoPageRoute>().page) {
            InfoPage.ABOUT -> AboutPage(onBackClick, onContactClick = onContact, onServicesClick = { onPageClick(InfoPage.SERVICES) }, modifier = modifier)
            InfoPage.SERVICES -> ServicesPage(onBackClick, modifier)
            InfoPage.BUILD -> BuildPage(onBackClick, modifier)
            InfoPage.CONTACT -> ContactPage(onBackClick, modifier)
            InfoPage.HELP -> HelpPage(onBackClick, onContactClick = onContact, modifier = modifier)
            InfoPage.TERMS -> TermsPage(onBackClick, onContactClick = onContact, modifier = modifier)
            InfoPage.CLAIMS -> ClaimsPage(onBackClick, modifier)
        }
    }
}
