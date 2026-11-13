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
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
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
            Surface(
                onClick = { onPageClick(page) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ArizColors.Edge),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(page.icon(), contentDescription = null, modifier = Modifier.size(22.dp), tint = ArizColors.Brand)
                    Text(
                        text = stringResource(page.labelRes()),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ArizColors.Ink,
                    )
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArizColors.Muted)
                }
            }
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
}

private fun InfoPage.icon() = when (this) {
    InfoPage.ABOUT -> Icons.Outlined.Apartment
    InfoPage.SERVICES -> Icons.Outlined.Work
    InfoPage.BUILD -> Icons.Outlined.Construction
    InfoPage.CONTACT -> Icons.Outlined.Email
    InfoPage.HELP -> Icons.Outlined.HelpOutline
    InfoPage.TERMS -> Icons.Outlined.Description
}
