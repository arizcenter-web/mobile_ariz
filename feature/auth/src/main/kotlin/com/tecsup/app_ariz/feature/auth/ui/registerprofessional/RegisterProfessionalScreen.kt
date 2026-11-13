package com.tecsup.app_ariz.feature.auth.ui.registerprofessional

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.feature.auth.R
import com.tecsup.app_ariz.feature.auth.domain.model.ProfessionalType
import com.tecsup.app_ariz.feature.auth.ui.component.AuthFooterLink
import com.tecsup.app_ariz.feature.auth.ui.component.AuthFormLayout
import com.tecsup.app_ariz.feature.auth.ui.component.FormErrorText
import com.tecsup.app_ariz.feature.auth.ui.component.asMessage

@Composable
fun RegisterProfessionalScreen(
    onRegistered: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterProfessionalViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterProfessionalEvent.Registered -> onRegistered()
            }
        }
    }

    RegisterProfessionalContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
fun RegisterProfessionalContent(
    state: RegisterProfessionalUiState,
    onAction: (RegisterProfessionalAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthFormLayout(
        eyebrow = stringResource(R.string.auth_professional_eyebrow),
        title = stringResource(R.string.auth_professional_title),
        onBackClick = onBackClick,
        subtitle = stringResource(R.string.auth_professional_subtitle),
        modifier = modifier,
    ) {
        ProfessionalTypeSelector(
            selected = state.type,
            enabled = !state.isLoading,
            onSelected = { onAction(RegisterProfessionalAction.TypeChanged(it)) },
        )

        val lastField = state.visibleFields.last()
        state.visibleFields.forEach { field ->
            ArizTextField(
                value = state.valueOf(field),
                onValueChange = { onAction(RegisterProfessionalAction.FieldChanged(field, it)) },
                label = stringResource(field.labelRes()) +
                    if (field.isRequired) "" else " " + stringResource(R.string.auth_field_optional_suffix),
                modifier = Modifier.fillMaxWidth(),
                errorText = state.fieldErrors[field].asMessage(),
                enabled = !state.isLoading,
                isPassword = field == ProfessionalField.PASSWORD,
                keyboardType = field.keyboardType(),
                imeAction = if (field == lastField) ImeAction.Done else ImeAction.Next,
                onImeAction = { if (field == lastField) onAction(RegisterProfessionalAction.Submit) },
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.acceptTerms,
                onCheckedChange = { onAction(RegisterProfessionalAction.TermsChanged(it)) },
                enabled = !state.isLoading,
            )
            Text(stringResource(R.string.auth_register_accept_terms))
        }
        state.termsError.asMessage()?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }

        FormErrorText(state.error)
        ArizPrimaryButton(
            text = stringResource(R.string.auth_register_submit),
            onClick = { onAction(RegisterProfessionalAction.Submit) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
        )
        AuthFooterLink(
            prefix = stringResource(R.string.auth_professional_customer_prompt),
            link = stringResource(R.string.auth_professional_customer_link),
            onClick = onBackClick,
        )
    }
}

/**
 * Opciones apiladas (una por tipo) en lugar de chips en fila: los nombres son largos y en una
 * pantalla estrecha la fila se cortaba. Toda la fila es el área táctil.
 */
@Composable
private fun ProfessionalTypeSelector(
    selected: ProfessionalType,
    enabled: Boolean,
    onSelected: (ProfessionalType) -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.auth_professional_type_label),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
        ProfessionalType.entries.forEach { type ->
            val isSelected = type == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else ArizColors.Edge,
                        shape = shape,
                    )
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelected(type) },
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = isSelected, onClick = null, enabled = enabled)
                Text(
                    text = stringResource(type.labelRes()),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@StringRes
private fun ProfessionalType.labelRes(): Int = when (this) {
    ProfessionalType.INDEPENDENT_AGENT -> R.string.auth_type_independent_agent
    ProfessionalType.REAL_ESTATE_AGENCY -> R.string.auth_type_agency
    ProfessionalType.DEVELOPER -> R.string.auth_type_developer
}

@StringRes
private fun ProfessionalField.labelRes(): Int = when (this) {
    ProfessionalField.FIRST_NAME -> R.string.auth_field_first_name
    ProfessionalField.LAST_NAME -> R.string.auth_field_last_name
    ProfessionalField.DNI -> R.string.auth_field_dni
    ProfessionalField.BUSINESS_NAME -> R.string.auth_field_business_name
    ProfessionalField.RUC -> R.string.auth_field_ruc
    ProfessionalField.EMAIL -> R.string.auth_field_email
    ProfessionalField.PHONE -> R.string.auth_field_phone
    ProfessionalField.WHATSAPP -> R.string.auth_field_whatsapp
    ProfessionalField.CITY -> R.string.auth_field_city
    ProfessionalField.DISTRICTS -> R.string.auth_field_districts
    ProfessionalField.ADDRESS -> R.string.auth_field_address
    ProfessionalField.WEBSITE -> R.string.auth_field_website
    ProfessionalField.SOCIAL_LINKS -> R.string.auth_field_social_links
    ProfessionalField.PASSWORD -> R.string.auth_field_password
}

private fun ProfessionalField.keyboardType(): KeyboardType = when (this) {
    ProfessionalField.EMAIL -> KeyboardType.Email
    ProfessionalField.PHONE, ProfessionalField.WHATSAPP -> KeyboardType.Phone
    ProfessionalField.DNI, ProfessionalField.RUC -> KeyboardType.Number
    ProfessionalField.WEBSITE -> KeyboardType.Uri
    else -> KeyboardType.Text
}

@Preview(showBackground = true)
@Composable
private fun RegisterProfessionalContentPreview() {
    ArizTheme {
        RegisterProfessionalContent(state = RegisterProfessionalUiState(), onAction = {}, onBackClick = {})
    }
}
