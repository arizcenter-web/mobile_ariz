package com.tecsup.app_ariz.feature.info.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.common.ArizContact
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizDropdownField
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.info.R
import com.tecsup.app_ariz.feature.info.domain.ContactFieldError

private val valueProps = listOf(
    Triple(Icons.Outlined.Apartment, R.string.info_contact_value_1_title, R.string.info_contact_value_1_text),
    Triple(Icons.Outlined.Place, R.string.info_contact_value_2_title, R.string.info_contact_value_2_text),
    Triple(Icons.Outlined.Shield, R.string.info_contact_value_3_title, R.string.info_contact_value_3_text),
    Triple(Icons.Outlined.Handshake, R.string.info_contact_value_4_title, R.string.info_contact_value_4_text),
    Triple(Icons.Outlined.Hub, R.string.info_contact_value_5_title, R.string.info_contact_value_5_text),
    Triple(Icons.Outlined.Lightbulb, R.string.info_contact_value_6_title, R.string.info_contact_value_6_text),
)

@Composable
internal fun ContactPage(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ContactContent(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, modifier = modifier)
}

/** Contacto: propuesta de valor, formulario `POST /contact-reports` y los canales directos de ARIZ. */
@Composable
internal fun ContactContent(
    state: ContactUiState,
    onAction: (ContactAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val topicLabel = stringResource(state.topic.labelRes())

    InfoPageLayout(
        eyebrow = stringResource(R.string.info_contact_eyebrow),
        title = stringResource(R.string.info_contact_title),
        subtitle = stringResource(R.string.info_contact_subtitle),
        onBackClick = onBackClick,
        modifier = modifier.imePadding(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Eyebrow(stringResource(R.string.info_contact_values_eyebrow))
            SectionTitle(stringResource(R.string.info_contact_values_title))
        }
        valueProps.forEach { (icon, title, text) ->
            InfoCard {
                IconBadge(icon)
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                BodyText(stringResource(text))
            }
        }

        InfoCard {
            Eyebrow(stringResource(R.string.info_contact_form_eyebrow))
            SectionTitle(stringResource(R.string.info_contact_form_title))
            ArizTextField(
                value = state.name,
                onValueChange = { onAction(ContactAction.NameChanged(it)) },
                label = stringResource(R.string.info_contact_field_name),
                modifier = Modifier.fillMaxWidth(),
                errorText = state.nameError?.text(),
                enabled = !state.isSending,
            )
            ArizTextField(
                value = state.email,
                onValueChange = { onAction(ContactAction.EmailChanged(it)) },
                label = stringResource(R.string.info_contact_field_email),
                modifier = Modifier.fillMaxWidth(),
                errorText = state.emailError?.text(),
                keyboardType = KeyboardType.Email,
                enabled = !state.isSending,
            )
            ArizTextField(
                value = state.phone,
                onValueChange = { onAction(ContactAction.PhoneChanged(it)) },
                label = stringResource(R.string.info_contact_field_phone),
                modifier = Modifier.fillMaxWidth(),
                errorText = state.phoneError?.text(),
                keyboardType = KeyboardType.Phone,
                enabled = !state.isSending,
            )
            ArizDropdownField(
                label = stringResource(R.string.info_contact_field_topic),
                selected = state.topic,
                options = ContactTopic.entries,
                optionLabel = { stringResource(it.labelRes()) },
                onSelected = { onAction(ContactAction.TopicChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            ArizTextField(
                value = state.message,
                onValueChange = { onAction(ContactAction.MessageChanged(it)) },
                label = stringResource(R.string.info_contact_field_message),
                modifier = Modifier.fillMaxWidth(),
                errorText = state.messageError?.text(),
                singleLine = false,
                minLines = 4,
                imeAction = ImeAction.Default,
                enabled = !state.isSending,
            )
            if (state.error != null) ArizAlert(message = stringResource(R.string.info_contact_error_send))
            ArizPrimaryButton(
                text = stringResource(R.string.info_contact_submit),
                onClick = { onAction(ContactAction.Submit(topicLabel)) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isSending,
            )
        }

        InfoCard {
            Eyebrow(stringResource(R.string.info_contact_together_eyebrow))
            SectionTitle(stringResource(R.string.info_contact_together_title))
            BodyText(stringResource(R.string.info_contact_together_text))
        }
        val whatsappMessage = stringResource(R.string.info_contact_whatsapp_message)
        ContactChannel(Icons.Outlined.Place, R.string.info_contact_office_label, R.string.info_contact_office_value) {
            uriHandler.openUri("https://www.google.com/maps/search/Calle%20Huascar%20310%2C%20Tumbes%2C%20Tumbes")
        }
        ContactChannel(Icons.Outlined.Phone, R.string.info_contact_whatsapp_label, R.string.info_contact_whatsapp_value) {
            uriHandler.openUri(ArizContact.whatsappUrl(whatsappMessage))
        }
        ContactChannel(Icons.Outlined.Email, R.string.info_contact_email_label, R.string.info_contact_email_value) {
            uriHandler.openUri("mailto:arizinmobiliaria@gmail.com")
        }
        ContactChannel(Icons.Outlined.Share, R.string.info_contact_facebook_label, R.string.info_contact_facebook_value) {
            uriHandler.openUri("https://web.facebook.com/Arizproyectos?locale=es_LA")
        }
        Text(
            text = stringResource(R.string.info_contact_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = ArizColors.Muted,
        )
    }

    if (state.sent) {
        AlertDialog(
            onDismissRequest = { onAction(ContactAction.SentDismissed) },
            title = { Text(stringResource(R.string.info_contact_success_title)) },
            text = { Text(stringResource(R.string.info_contact_success_text)) },
            confirmButton = {
                TextButton(onClick = { onAction(ContactAction.SentDismissed) }) {
                    Text(stringResource(R.string.info_contact_success_action), fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}

@Composable
private fun ContactChannel(icon: ImageVector, labelRes: Int, valueRes: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon)
            Column {
                Text(
                    stringResource(labelRes).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                    color = ArizColors.Muted,
                )
                Text(stringResource(valueRes), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
            }
        }
    }
}

private fun ContactTopic.labelRes(): Int = when (this) {
    ContactTopic.INFORMATION -> R.string.info_contact_topic_information
    ContactTopic.BUY -> R.string.info_contact_topic_buy
    ContactTopic.SELL -> R.string.info_contact_topic_sell
    ContactTopic.INVEST -> R.string.info_contact_topic_invest
    ContactTopic.PUBLISH -> R.string.info_contact_topic_publish
    ContactTopic.SUPPORT -> R.string.info_contact_topic_support
}

@Composable
private fun ContactFieldError.text(): String = stringResource(
    when (this) {
        ContactFieldError.REQUIRED -> R.string.info_contact_error_required
        ContactFieldError.INVALID_EMAIL -> R.string.info_contact_error_email
        ContactFieldError.INVALID_PHONE -> R.string.info_contact_error_phone
        ContactFieldError.MESSAGE_TOO_SHORT -> R.string.info_contact_error_message_short
    },
)
