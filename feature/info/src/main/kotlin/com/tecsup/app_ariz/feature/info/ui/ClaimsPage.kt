package com.tecsup.app_ariz.feature.info.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.component.ArizDropdownField
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.info.R
import com.tecsup.app_ariz.feature.info.domain.ClaimError
import com.tecsup.app_ariz.feature.info.domain.ClaimField
import com.tecsup.app_ariz.feature.info.domain.ClaimKind

private val AssetTypes = listOf("Producto", "Servicio")

/** Libro de Reclamaciones: datos de la empresa, del consumidor, del bien, tipo y detalle (igual que el web). */
@Composable
internal fun ClaimsPage(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: ClaimViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft = state.draft
    val errorTexts = ClaimField.entries.associateWith { field -> state.errors[field]?.let { stringResource(it.textRes()) } }
    fun error(field: ClaimField): String? = errorTexts[field]

    InfoPageLayout(
        eyebrow = stringResource(R.string.info_claims_eyebrow),
        title = stringResource(R.string.info_claims_title),
        subtitle = stringResource(R.string.info_claims_subtitle),
        onBackClick = onBackClick,
        modifier = modifier.imePadding(),
    ) {
        val receipt = state.receipt
        if (receipt != null) {
            InfoCard {
                SectionTitle(stringResource(R.string.info_claims_success_title))
                BodyText(stringResource(R.string.info_claims_success_text))
                Eyebrow(stringResource(R.string.info_claims_code))
                Text(receipt.code, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                BodyText(stringResource(R.string.info_claims_code_hint))
                BodyText(stringResource(R.string.info_claims_summary, receipt.fullName, stringResource(receipt.kind.labelRes())))
            }
            ArizPrimaryButton(text = stringResource(R.string.info_claims_close), onClick = viewModel::reset, modifier = Modifier.fillMaxWidth())
            return@InfoPageLayout
        }

        InfoCard {
            Eyebrow(stringResource(R.string.info_claims_company))
            BodyText(stringResource(R.string.info_claims_company_name))
            BodyText(stringResource(R.string.info_claims_company_ruc))
            BodyText(stringResource(R.string.info_claims_company_address))
        }
        InfoCard {
            Eyebrow(stringResource(R.string.info_claims_consumer))
            ArizTextField(draft.fullName, { v -> viewModel.edit { it.copy(fullName = v.take(200)) } }, stringResource(R.string.info_claims_full_name), Modifier.fillMaxWidth(), errorText = error(ClaimField.FULL_NAME), enabled = !state.isSending)
            ArizTextField(draft.address, { v -> viewModel.edit { it.copy(address = v.take(300)) } }, stringResource(R.string.info_claims_address), Modifier.fillMaxWidth(), errorText = error(ClaimField.ADDRESS), enabled = !state.isSending)
            ArizTextField(draft.dniOrCe, { v -> viewModel.edit { it.copy(dniOrCe = v.take(12)) } }, stringResource(R.string.info_claims_dni), Modifier.fillMaxWidth(), errorText = error(ClaimField.DNI), enabled = !state.isSending)
            ArizTextField(draft.phone, { v -> viewModel.edit { it.copy(phone = v.take(20)) } }, stringResource(R.string.info_claims_phone), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Phone, enabled = !state.isSending)
            ArizTextField(draft.email, { v -> viewModel.edit { it.copy(email = v) } }, stringResource(R.string.info_claims_email), Modifier.fillMaxWidth(), errorText = error(ClaimField.EMAIL), keyboardType = KeyboardType.Email, enabled = !state.isSending)
            ArizTextField(draft.parentGuardian, { v -> viewModel.edit { it.copy(parentGuardian = v.take(200)) } }, stringResource(R.string.info_claims_guardian), Modifier.fillMaxWidth(), enabled = !state.isSending)
        }
        InfoCard {
            Eyebrow(stringResource(R.string.info_claims_asset))
            ArizDropdownField(
                label = stringResource(R.string.info_claims_asset_type),
                selected = draft.assetType,
                options = AssetTypes,
                optionLabel = { it },
                onSelected = { v -> viewModel.edit { it.copy(assetType = v) } },
                modifier = Modifier.fillMaxWidth(),
            )
            ArizTextField(draft.claimedAmount, { v -> viewModel.edit { it.copy(claimedAmount = v.filter { c -> c.isDigit() || c == '.' || c == ',' }) } }, stringResource(R.string.info_claims_amount), Modifier.fillMaxWidth(), errorText = error(ClaimField.AMOUNT), keyboardType = KeyboardType.Decimal, enabled = !state.isSending)
            ArizTextField(draft.assetDescription, { v -> viewModel.edit { it.copy(assetDescription = v.take(1000)) } }, stringResource(R.string.info_claims_asset_description), Modifier.fillMaxWidth(), errorText = error(ClaimField.ASSET_DESCRIPTION), singleLine = false, minLines = 3, enabled = !state.isSending)
        }
        InfoCard {
            Eyebrow(stringResource(R.string.info_claims_claim))
            ArizDropdownField(
                label = stringResource(R.string.info_claims_kind),
                selected = draft.kind,
                options = ClaimKind.entries,
                optionLabel = { stringResource(it.labelRes()) },
                onSelected = { v -> viewModel.edit { it.copy(kind = v) } },
                modifier = Modifier.fillMaxWidth(),
            )
            ArizTextField(draft.claimDetails, { v -> viewModel.edit { it.copy(claimDetails = v.take(2000)) } }, stringResource(R.string.info_claims_details), Modifier.fillMaxWidth(), errorText = error(ClaimField.DETAILS), singleLine = false, minLines = 5, enabled = !state.isSending)
            ArizTextField(draft.consumerRequest, { v -> viewModel.edit { it.copy(consumerRequest = v.take(2000)) } }, stringResource(R.string.info_claims_request), Modifier.fillMaxWidth(), errorText = error(ClaimField.REQUEST), singleLine = false, minLines = 4, enabled = !state.isSending)
        }
        state.error?.let { ArizAlert(message = it.message) }
        ArizPrimaryButton(
            text = stringResource(R.string.info_claims_submit),
            onClick = viewModel::submit,
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isSending,
        )
        Text(stringResource(R.string.info_claims_terms), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
    }
}

private fun ClaimKind.labelRes() = if (this == ClaimKind.CLAIM) R.string.info_claims_kind_claim else R.string.info_claims_kind_complaint

private fun ClaimError.textRes() = when (this) {
    ClaimError.REQUIRED -> R.string.info_claims_err_required
    ClaimError.TOO_SHORT_3 -> R.string.info_claims_err_min3
    ClaimError.TOO_SHORT_5 -> R.string.info_claims_err_min5
    ClaimError.TOO_SHORT_10 -> R.string.info_claims_err_min10
    ClaimError.TOO_SHORT_20 -> R.string.info_claims_err_min20
    ClaimError.DNI_LENGTH -> R.string.info_claims_err_dni
    ClaimError.INVALID_EMAIL -> R.string.info_claims_err_email
    ClaimError.INVALID_AMOUNT -> R.string.info_claims_err_amount
}
