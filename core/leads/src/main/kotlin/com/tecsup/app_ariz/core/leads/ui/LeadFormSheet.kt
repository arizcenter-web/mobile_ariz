package com.tecsup.app_ariz.core.leads.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.leads.R
import com.tecsup.app_ariz.core.leads.domain.LeadFieldError
import com.tecsup.app_ariz.core.leads.domain.LeadTarget

/**
 * Formulario "Contacta al anunciante" en una hoja inferior. Lo usan el detalle de propiedad y el de
 * proyecto: cada uno indica el [target] y un [defaultMessage] sugerido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadFormSheet(
    target: LeadTarget,
    defaultMessage: String,
    onDismiss: () -> Unit,
    viewModel: LeadFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(defaultMessage) { viewModel.prepare(defaultMessage) }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onAction(LeadFormAction.Dismissed)
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        if (state.sent) {
            SuccessContent(onClose = {
                viewModel.onAction(LeadFormAction.Dismissed)
                onDismiss()
            })
        } else {
            FormContent(state = state, onAction = viewModel::onAction, target = target)
        }
    }
}

@Composable
private fun FormContent(state: LeadFormUiState, onAction: (LeadFormAction) -> Unit, target: LeadTarget) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.leads_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Ink,
        )
        ArizTextField(
            value = state.email,
            onValueChange = { onAction(LeadFormAction.EmailChanged(it)) },
            label = stringResource(R.string.leads_field_email),
            modifier = Modifier.fillMaxWidth(),
            errorText = state.emailError?.text(),
            keyboardType = KeyboardType.Email,
            enabled = !state.isSending,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ArizTextField(
                value = state.name,
                onValueChange = { onAction(LeadFormAction.NameChanged(it)) },
                label = stringResource(R.string.leads_field_name),
                modifier = Modifier.weight(1f),
                errorText = state.nameError?.text(),
                enabled = !state.isSending,
            )
            ArizTextField(
                value = state.phone,
                onValueChange = { onAction(LeadFormAction.PhoneChanged(it)) },
                label = stringResource(R.string.leads_field_phone),
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Phone,
                enabled = !state.isSending,
            )
        }
        ArizTextField(
            value = state.dni,
            onValueChange = { onAction(LeadFormAction.DniChanged(it)) },
            label = stringResource(R.string.leads_field_dni),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number,
            enabled = !state.isSending,
        )
        ArizTextField(
            value = state.message,
            onValueChange = { onAction(LeadFormAction.MessageChanged(it)) },
            label = stringResource(R.string.leads_field_message),
            modifier = Modifier.fillMaxWidth(),
            errorText = state.messageError?.text(),
            singleLine = false,
            minLines = 3,
            imeAction = ImeAction.Default,
            enabled = !state.isSending,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.acceptTerms,
                onCheckedChange = { onAction(LeadFormAction.TermsChanged(it)) },
                enabled = !state.isSending,
            )
            Text(
                text = stringResource(R.string.leads_terms),
                style = MaterialTheme.typography.bodySmall,
                color = ArizColors.Ink,
            )
        }
        state.termsError?.let {
            Text(it.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        if (state.error != null) {
            ArizAlert(message = stringResource(R.string.leads_error_send))
        }
        ArizPrimaryButton(
            text = stringResource(R.string.leads_submit),
            onClick = { onAction(LeadFormAction.Submit(target)) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isSending,
        )
    }
}

/** Confirmación del web: círculo verde con una marca, "¡Mensaje enviado correctamente!" y "Entendido". */
@Composable
private fun SuccessContent(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(ArizColors.Success.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(28.dp), tint = ArizColors.Success)
        }
        Text(
            text = stringResource(R.string.leads_success_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Ink,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.leads_success_description),
            style = MaterialTheme.typography.bodyMedium,
            color = ArizColors.Muted,
            textAlign = TextAlign.Center,
        )
        ArizPrimaryButton(
            text = stringResource(R.string.leads_success_action),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

@Composable
private fun LeadFieldError.text(): String = stringResource(
    when (this) {
        LeadFieldError.REQUIRED -> R.string.leads_error_required
        LeadFieldError.INVALID_EMAIL -> R.string.leads_error_invalid_email
        LeadFieldError.TERMS_NOT_ACCEPTED -> R.string.leads_error_terms
    },
)
