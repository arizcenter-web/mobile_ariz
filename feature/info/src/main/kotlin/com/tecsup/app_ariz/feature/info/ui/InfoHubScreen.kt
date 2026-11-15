package com.tecsup.app_ariz.feature.info.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.app_ariz.core.ui.component.PanelMenuRow
import com.tecsup.app_ariz.feature.info.R
import com.tecsup.app_ariz.feature.info.navigation.InfoPage

/** Menú con todas las páginas informativas: Nosotros, Servicios, Construye, Contacto, Ayuda y Términos. */
@Composable
fun InfoHubScreen(
    onBackClick: () -> Unit,
    onPageClick: (InfoPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoPageLayout(
        eyebrow = stringResource(R.string.info_hub_title).uppercase(),
        title = stringResource(R.string.info_hub_title),
        subtitle = stringResource(R.string.info_hub_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        InfoPage.entries.forEach { page ->
            PanelMenuRow(icon = page.icon(), label = stringResource(page.labelRes()), onClick = { onPageClick(page) })
        }
    }
}

private fun InfoPage.labelRes(): Int = when (this) {
    InfoPage.ABOUT -> R.string.info_page_about
    InfoPage.SERVICES -> R.string.info_page_services
    InfoPage.BUILD -> R.string.info_page_build
    InfoPage.CONTACT -> R.string.info_page_contact
    InfoPage.HELP -> R.string.info_page_help
    InfoPage.TERMS -> R.string.info_page_terms
    InfoPage.CLAIMS -> R.string.info_page_claims
}

private fun InfoPage.icon() = when (this) {
    InfoPage.ABOUT -> Icons.Outlined.Apartment
    InfoPage.SERVICES -> Icons.Outlined.Work
    InfoPage.BUILD -> Icons.Outlined.Construction
    InfoPage.CONTACT -> Icons.Outlined.Email
    InfoPage.HELP -> Icons.Outlined.HelpOutline
    InfoPage.TERMS -> Icons.Outlined.Description
    InfoPage.CLAIMS -> Icons.Outlined.MenuBook
}
