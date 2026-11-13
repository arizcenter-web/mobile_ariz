package com.tecsup.app_ariz.feature.info.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.common.ArizContact
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.info.R

private data class BigService(val name: Int, val title: Int, val text: Int, val bullets: List<Int>)

private val bigServices = listOf(
    BigService(
        R.string.info_service_prospeccion_name, R.string.info_service_prospeccion_title, R.string.info_service_prospeccion_text,
        listOf(R.string.info_service_prospeccion_b1, R.string.info_service_prospeccion_b2, R.string.info_service_prospeccion_b3, R.string.info_service_prospeccion_b4),
    ),
    BigService(
        R.string.info_service_legal_name, R.string.info_service_legal_title, R.string.info_service_legal_text,
        listOf(R.string.info_service_legal_b1, R.string.info_service_legal_b2, R.string.info_service_legal_b3, R.string.info_service_legal_b4),
    ),
    BigService(
        R.string.info_service_contable_name, R.string.info_service_contable_title, R.string.info_service_contable_text,
        listOf(R.string.info_service_contable_b1, R.string.info_service_contable_b2, R.string.info_service_contable_b3, R.string.info_service_contable_b4),
    ),
)

private val smallServices = listOf(
    R.string.info_service_small_prospeccion_title to R.string.info_service_small_prospeccion_text,
    R.string.info_service_small_ventas_title to R.string.info_service_small_ventas_text,
    R.string.info_service_small_marketing_title to R.string.info_service_small_marketing_text,
    R.string.info_service_small_corretaje_title to R.string.info_service_small_corretaje_text,
    R.string.info_service_small_comercial_title to R.string.info_service_small_comercial_text,
    R.string.info_service_small_inversion_title to R.string.info_service_small_inversion_text,
    R.string.info_service_small_legal_title to R.string.info_service_small_legal_text,
    R.string.info_service_small_contable_title to R.string.info_service_small_contable_text,
    R.string.info_service_small_saneamiento_title to R.string.info_service_small_saneamiento_text,
    R.string.info_service_small_tramites_title to R.string.info_service_small_tramites_text,
    R.string.info_service_small_financiamiento_title to R.string.info_service_small_financiamiento_text,
    R.string.info_service_small_postventa_title to R.string.info_service_small_postventa_text,
)

@Composable
internal fun ServicesPage(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val whatsappTemplate = stringResource(R.string.info_services_whatsapp, "%s")
    InfoPageLayout(
        eyebrow = stringResource(R.string.info_services_eyebrow),
        title = stringResource(R.string.info_services_title),
        subtitle = stringResource(R.string.info_services_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        bigServices.forEach { service ->
            val name = stringResource(service.name)
            InfoCard {
                Eyebrow(name.uppercase())
                Text(
                    text = stringResource(service.title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
                BodyText(stringResource(service.text))
                service.bullets.forEach { Bullet(stringResource(it)) }
                BrandButton(
                    text = stringResource(R.string.info_services_quote),
                    onClick = { uriHandler.openUri(ArizContact.whatsappUrl(whatsappTemplate.format(name))) },
                )
            }
        }
        SectionTitle(stringResource(R.string.info_services_all_title))
        smallServices.forEach { (title, text) ->
            InfoCard {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                BodyText(stringResource(text))
            }
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(modifier = Modifier.padding(top = 8.dp).size(8.dp), shape = RoundedCornerShape(50), color = ArizColors.Accent) {}
        Text(text, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Ink)
    }
}

private val buildServices = listOf(
    R.string.info_build_1_title to R.string.info_build_1_text,
    R.string.info_build_2_title to R.string.info_build_2_text,
    R.string.info_build_3_title to R.string.info_build_3_text,
    R.string.info_build_4_title to R.string.info_build_4_text,
    R.string.info_build_5_title to R.string.info_build_5_text,
    R.string.info_build_6_title to R.string.info_build_6_text,
    R.string.info_build_7_title to R.string.info_build_7_text,
    R.string.info_build_8_title to R.string.info_build_8_text,
    R.string.info_build_9_title to R.string.info_build_9_text,
    R.string.info_build_10_title to R.string.info_build_10_text,
    R.string.info_build_11_title to R.string.info_build_11_text,
    R.string.info_build_12_title to R.string.info_build_12_text,
)

/** "ARIZ Proyectos construye": los 12 servicios de construcción, cada uno con su botón de cotizar. */
@Composable
internal fun BuildPage(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val whatsappTemplate = stringResource(R.string.info_build_whatsapp, "%s")
    InfoPageLayout(
        eyebrow = stringResource(R.string.info_page_build).uppercase(),
        title = stringResource(R.string.info_build_title),
        subtitle = stringResource(R.string.info_build_quote),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        buildServices.forEach { (titleRes, textRes) ->
            val title = stringResource(titleRes)
            InfoCard {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                BodyText(stringResource(textRes))
                BrandButton(
                    text = stringResource(R.string.info_build_cta),
                    onClick = { uriHandler.openUri(ArizContact.whatsappUrl(whatsappTemplate.format(title))) },
                )
            }
        }
    }
}

private const val SupportEmail = "arizcenter@gmail.com"

private val faqs = listOf(
    R.string.info_help_q1 to R.string.info_help_a1,
    R.string.info_help_q2 to R.string.info_help_a2,
    R.string.info_help_q3 to R.string.info_help_a3,
    R.string.info_help_q4 to R.string.info_help_a4,
    R.string.info_help_q5 to R.string.info_help_a5,
)

/** Centro de ayuda: preguntas frecuentes desplegables y el correo de soporte. */
@Composable
internal fun HelpPage(onBackClick: () -> Unit, onContactClick: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val subject = android.net.Uri.encode(stringResource(R.string.info_help_mail_subject))
    val body = android.net.Uri.encode(stringResource(R.string.info_help_mail_body))
    val mailUrl = "mailto:$SupportEmail?subject=$subject&body=$body"

    InfoPageLayout(
        eyebrow = stringResource(R.string.info_help_eyebrow),
        title = stringResource(R.string.info_help_title),
        subtitle = stringResource(R.string.info_help_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        BrandButton(stringResource(R.string.info_help_write, SupportEmail), onClick = { uriHandler.openUri(mailUrl) })

        InfoCard {
            SectionTitle(stringResource(R.string.info_help_faq_title))
            faqs.forEach { (question, answer) -> Faq(stringResource(question), stringResource(answer)) }
        }

        InfoCard {
            Eyebrow(stringResource(R.string.info_help_support_eyebrow))
            Text(
                text = stringResource(R.string.info_help_support_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            BodyText(stringResource(R.string.info_help_support_text))
            BrandButton(stringResource(R.string.info_help_send_mail), onClick = { uriHandler.openUri(mailUrl) })
            BrandButton(stringResource(R.string.info_help_go_contact), onClick = onContactClick, filled = false)
        }
    }
}

/** Pregunta frecuente desplegable (`<details>` del web). */
@Composable
private fun Faq(question: String, answer: String) {
    var expanded by rememberSaveable(question) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = question,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
                Text(if (expanded) "−" else "+", style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), color = ArizColors.Accent)
            }
            AnimatedVisibility(visible = expanded) { BodyText(answer) }
        }
    }
}
