package com.tecsup.app_ariz.feature.info.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.info.R

@Composable
internal fun AboutPage(onBackClick: () -> Unit, onContactClick: () -> Unit, onServicesClick: () -> Unit, modifier: Modifier = Modifier) {
    InfoPageLayout(
        eyebrow = stringResource(R.string.info_about_eyebrow),
        title = stringResource(R.string.info_about_title),
        subtitle = stringResource(R.string.info_about_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        InfoCard {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.Apartment)
                Column {
                    Eyebrow(stringResource(R.string.info_about_who_eyebrow))
                    SectionTitle(stringResource(R.string.info_about_who_title))
                }
            }
            BodyText(stringResource(R.string.info_about_who_p1))
            BodyText(stringResource(R.string.info_about_who_p2))
            BrandButton(stringResource(R.string.info_about_cta_contact), onClick = onContactClick)
            BrandButton(stringResource(R.string.info_about_cta_services), onClick = onServicesClick, filled = false)
        }

        InfoCard {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.Description)
                Column {
                    Eyebrow(stringResource(R.string.info_about_company_eyebrow))
                    Text(
                        text = stringResource(R.string.info_about_company_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                }
            }
            Fact(R.string.info_about_company_name_label, R.string.info_about_company_name_value)
            Fact(R.string.info_about_trade_label, R.string.info_about_trade_value)
            Fact(R.string.info_about_ruc_label, R.string.info_about_ruc_value)
            Fact(R.string.info_about_registry_label, R.string.info_about_registry_value)
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Eyebrow(stringResource(R.string.info_about_identity_eyebrow))
            SectionTitle(stringResource(R.string.info_about_identity_title))
        }
        Identity(Icons.Outlined.Flag, R.string.info_about_mission_title, R.string.info_about_mission_text)
        Identity(Icons.Outlined.TrendingUp, R.string.info_about_vision_title, R.string.info_about_vision_text)
        Identity(Icons.Outlined.Shield, R.string.info_about_commitment_title, R.string.info_about_commitment_text)

        InfoCard {
            Eyebrow(stringResource(R.string.info_about_principles_eyebrow))
            SectionTitle(stringResource(R.string.info_about_principles_title))
            listOf(
                R.string.info_about_principle_1, R.string.info_about_principle_2, R.string.info_about_principle_3,
                R.string.info_about_principle_4, R.string.info_about_principle_5, R.string.info_about_principle_6,
            ).forEach { principle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = ArizColors.Accent)
                    Text(stringResource(principle), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Ink)
                }
            }
        }
    }
}

@Composable
private fun Fact(labelRes: Int, valueRes: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(labelRes).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
            color = ArizColors.Muted,
        )
        Text(stringResource(valueRes), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Ink)
    }
}

@Composable
private fun Identity(icon: androidx.compose.ui.graphics.vector.ImageVector, titleRes: Int, textRes: Int) {
    InfoCard {
        IconBadge(icon)
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = ArizColors.Brand,
        )
        BodyText(stringResource(textRes))
    }
}

/** Términos y condiciones: 11 secciones de dos párrafos cada una (`terminos/page.tsx`). */
@Composable
internal fun TermsPage(onBackClick: () -> Unit, onContactClick: () -> Unit, modifier: Modifier = Modifier) {
    val sections = listOf(
        R.string.info_terms_1_title to listOf(R.string.info_terms_1_p1, R.string.info_terms_1_p2),
        R.string.info_terms_2_title to listOf(R.string.info_terms_2_p1, R.string.info_terms_2_p2),
        R.string.info_terms_3_title to listOf(R.string.info_terms_3_p1, R.string.info_terms_3_p2),
        R.string.info_terms_4_title to listOf(R.string.info_terms_4_p1, R.string.info_terms_4_p2),
        R.string.info_terms_5_title to listOf(R.string.info_terms_5_p1, R.string.info_terms_5_p2),
        R.string.info_terms_6_title to listOf(R.string.info_terms_6_p1, R.string.info_terms_6_p2),
        R.string.info_terms_7_title to listOf(R.string.info_terms_7_p1, R.string.info_terms_7_p2),
        R.string.info_terms_8_title to listOf(R.string.info_terms_8_p1, R.string.info_terms_8_p2),
        R.string.info_terms_9_title to listOf(R.string.info_terms_9_p1, R.string.info_terms_9_p2),
        R.string.info_terms_10_title to listOf(R.string.info_terms_10_p1, R.string.info_terms_10_p2),
        R.string.info_terms_11_title to listOf(R.string.info_terms_11_p1, R.string.info_terms_11_p2),
    )
    InfoPageLayout(
        eyebrow = stringResource(R.string.info_terms_eyebrow),
        title = stringResource(R.string.info_terms_title),
        subtitle = stringResource(R.string.info_terms_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        InfoCard {
            Text(
                text = stringResource(R.string.info_terms_updated),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ArizColors.Muted,
            )
            sections.forEach { (title, paragraphs) ->
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    paragraphs.forEach { BodyText(stringResource(it)) }
                }
            }
        }
        InfoCard {
            Eyebrow(stringResource(R.string.info_contact_together_eyebrow))
            Text(
                text = stringResource(R.string.info_terms_help_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            BodyText(stringResource(R.string.info_terms_help_text))
            BrandButton(stringResource(R.string.info_terms_help_action), onClick = onContactClick)
        }
    }
}
