package com.tecsup.app_ariz.feature.rewards.ui.referral

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizDropdownField
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.rewards.R
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralAvailability
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralInterest
import com.tecsup.app_ariz.feature.rewards.domain.model.Referrer
import com.tecsup.app_ariz.feature.rewards.domain.validation.ReferralFieldError

/**
 * Formulario para referir a alguien. Con [referrer] el usuario ya es miembro del club y solo da los
 * datos del referido; sin él se une al club (pide también sus propios datos).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralFormSheet(
    referrer: Referrer?,
    onDismiss: () -> Unit,
    viewModel: ReferralFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        if (state.sent) {
            Success(onClose = onDismiss)
        } else {
            Form(state = state, referrer = referrer, onAction = viewModel::onAction)
        }
    }
}

@Composable
private fun Form(state: ReferralFormUiState, referrer: Referrer?, onAction: (ReferralFormAction) -> Unit) {
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
            text = stringResource(if (referrer != null) R.string.rewards_referral_title_member else R.string.rewards_referral_title_new),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Ink,
        )

        if (referrer == null) {
            SectionLabel(stringResource(R.string.rewards_referral_you))
            Field(ReferralField.REFERRER_NAME, state.referrerName, R.string.rewards_referral_name, state, onAction)
            Field(ReferralField.REFERRER_EMAIL, state.referrerEmail, R.string.rewards_referral_email, state, onAction, KeyboardType.Email)
            Field(ReferralField.REFERRER_PHONE, state.referrerPhone, R.string.rewards_referral_phone, state, onAction, KeyboardType.Phone)
        }

        SectionLabel(stringResource(R.string.rewards_referral_person))
        Field(ReferralField.NAME, state.name, R.string.rewards_referral_name, state, onAction)
        Field(ReferralField.EMAIL, state.email, R.string.rewards_referral_email, state, onAction, KeyboardType.Email)
        Field(ReferralField.PHONE, state.phone, R.string.rewards_referral_phone, state, onAction, KeyboardType.Phone)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ArizDropdownField(
                label = stringResource(R.string.rewards_referral_interest),
                selected = state.interest,
                options = listOf<ReferralInterest?>(null) + ReferralInterest.entries,
                optionLabel = { interest ->
                    if (interest == null) stringResource(R.string.rewards_referral_availability_none) else stringResource(interest.labelRes())
                },
                onSelected = { it?.let { interest -> onAction(ReferralFormAction.InterestChanged(interest)) } },
                modifier = Modifier.fillMaxWidth(),
            )
            state.errors[ReferralField.INTEREST]?.let {
                Text(stringResource(R.string.rewards_error_interest), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        ArizDropdownField(
            label = stringResource(R.string.rewards_referral_availability),
            selected = state.availability,
            options = listOf<ReferralAvailability?>(null) + ReferralAvailability.entries,
            optionLabel = { availability ->
                if (availability == null) stringResource(R.string.rewards_referral_availability_none) else stringResource(availability.labelRes())
            },
            onSelected = { onAction(ReferralFormAction.AvailabilityChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let { ArizAlert(message = it.message) }
        ArizPrimaryButton(
            text = stringResource(R.string.rewards_referral_submit),
            onClick = { onAction(ReferralFormAction.Submit(referrer)) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isSending,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
        color = ArizColors.Accent,
    )
}

@Composable
private fun Field(
    field: ReferralField,
    value: String,
    labelRes: Int,
    state: ReferralFormUiState,
    onAction: (ReferralFormAction) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    ArizTextField(
        value = value,
        onValueChange = { onAction(ReferralFormAction.FieldChanged(field, it)) },
        label = stringResource(labelRes),
        modifier = Modifier.fillMaxWidth(),
        errorText = state.errors[field]?.let { stringResource(it.textRes()) },
        keyboardType = keyboardType,
        enabled = !state.isSending,
    )
}

@Composable
private fun Success(onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 28.dp).padding(bottom = 28.dp),
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
            text = stringResource(R.string.rewards_referral_success_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ArizColors.Ink,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.rewards_referral_success_text),
            style = MaterialTheme.typography.bodyMedium,
            color = ArizColors.Muted,
            textAlign = TextAlign.Center,
        )
        ArizPrimaryButton(text = stringResource(R.string.rewards_ok), onClick = onClose, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

private fun ReferralFieldError.textRes(): Int = when (this) {
    ReferralFieldError.REQUIRED -> R.string.rewards_error_required
    ReferralFieldError.INVALID_EMAIL -> R.string.rewards_error_email
    ReferralFieldError.INVALID_PHONE -> R.string.rewards_error_phone
    ReferralFieldError.INTEREST_REQUIRED -> R.string.rewards_error_interest
}

private fun ReferralInterest.labelRes(): Int = when (this) {
    ReferralInterest.VENTA -> R.string.rewards_interest_venta
    ReferralInterest.COMPRA -> R.string.rewards_interest_compra
    ReferralInterest.ALQUILER -> R.string.rewards_interest_alquiler
    ReferralInterest.COMPRA_LOTE -> R.string.rewards_interest_compra_lote
    ReferralInterest.INVERSION -> R.string.rewards_interest_inversion
    ReferralInterest.CORRETAJE -> R.string.rewards_interest_corretaje
}

private fun ReferralAvailability.labelRes(): Int = when (this) {
    ReferralAvailability.INMEDIATA -> R.string.rewards_availability_inmediata
    ReferralAvailability.AVERIGUAR -> R.string.rewards_availability_averiguar
    ReferralAvailability.POR_CONFIRMAR -> R.string.rewards_availability_por_confirmar
}
