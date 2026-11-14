package com.tecsup.app_ariz.feature.professional.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.component.PanelCard
import com.tecsup.app_ariz.core.ui.component.PanelError
import com.tecsup.app_ariz.core.ui.component.PanelField
import com.tecsup.app_ariz.core.ui.component.PanelLoading
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.PanelSwitchRow
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.ApprovalState
import com.tecsup.app_ariz.feature.professional.domain.ProfileUpdate

@Composable
internal fun ProfileScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile
    val form = state.form

    WithNotice(state.notice, onShown = { viewModel.onAction(ProfileAction.NoticeShown) }, modifier = modifier) {
        PanelPage(
            eyebrow = stringResource(R.string.pro_panel_eyebrow),
            title = stringResource(R.string.pro_profile_title),
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.pro_back),
        ) {
            when {
                state.isLoading -> PanelLoading()
                profile == null || form == null ->
                    PanelError(state.error?.message.orEmpty(), stringResource(R.string.pro_retry)) { viewModel.onAction(ProfileAction.Retry) }
                else -> {
                    PanelCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(stringResource(profile.type.labelRes()), ArizColors.Brand)
                            val (status, color) = when (profile.status) {
                                ApprovalState.APPROVED -> R.string.pro_profile_status_approved to ArizColors.Success
                                ApprovalState.PENDING -> R.string.pro_profile_status_pending to ArizColors.Warning
                                ApprovalState.REJECTED -> R.string.pro_profile_status_rejected to ArizColors.Error
                            }
                            StatusPill(stringResource(status), color)
                        }
                        PanelField(stringResource(R.string.pro_profile_email), profile.email)
                    }
                    PanelCard {
                        Field(R.string.pro_profile_first_name, form.firstName) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(firstName = v) }) }
                        Field(R.string.pro_profile_business, form.businessName) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(businessName = v) }) }
                        Field(R.string.pro_profile_phone, form.phone, KeyboardType.Phone) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(phone = v) }) }
                        Field(R.string.pro_profile_whatsapp, form.whatsapp, KeyboardType.Phone) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(whatsapp = v) }) }
                        Field(R.string.pro_profile_ruc, form.ruc, KeyboardType.Number) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(ruc = v) }) }
                        Field(R.string.pro_profile_city, form.city) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(city = v) }) }
                        Field(R.string.pro_profile_districts, form.districts) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(districts = v) }) }
                        Field(R.string.pro_profile_address, form.address) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(address = v) }) }
                        Field(R.string.pro_profile_website, form.website, KeyboardType.Uri) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(website = v) }) }
                        Field(R.string.pro_profile_social, form.socialLinks) { v -> viewModel.onAction(ProfileAction.Edit { it.copy(socialLinks = v) }) }
                        ArizPrimaryButton(
                            text = stringResource(R.string.pro_profile_save),
                            onClick = { viewModel.onAction(ProfileAction.Save) },
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = state.isSaving,
                        )
                    }
                    PanelCard {
                        Text(stringResource(R.string.pro_password_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                        ArizTextField(
                            value = state.currentPassword,
                            onValueChange = { viewModel.onAction(ProfileAction.CurrentPasswordChanged(it)) },
                            label = stringResource(R.string.pro_password_current),
                            modifier = Modifier.fillMaxWidth(),
                            isPassword = true,
                        )
                        ArizTextField(
                            value = state.newPassword,
                            onValueChange = { viewModel.onAction(ProfileAction.NewPasswordChanged(it)) },
                            label = stringResource(R.string.pro_password_new),
                            modifier = Modifier.fillMaxWidth(),
                            isPassword = true,
                        )
                        ArizPrimaryButton(
                            text = stringResource(R.string.pro_password_change),
                            onClick = { viewModel.onAction(ProfileAction.ChangePassword) },
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = state.isChangingPassword,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(labelRes: Int, value: String, keyboardType: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    ArizTextField(
        value = value,
        onValueChange = onChange,
        label = stringResource(labelRes),
        modifier = Modifier.fillMaxWidth(),
        keyboardType = keyboardType,
    )
}

@Composable
internal fun SettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings = state.settings

    WithNotice(state.notice, onShown = { viewModel.onAction(SettingsAction.NoticeShown) }, modifier = modifier) {
        PanelPage(
            eyebrow = stringResource(R.string.pro_panel_eyebrow),
            title = stringResource(R.string.pro_settings_title),
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.pro_back),
        ) {
            when {
                state.isLoading -> PanelLoading()
                settings == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.pro_retry)) { viewModel.onAction(SettingsAction.Retry) }
                else -> PanelCard {
                    PanelSwitchRow(stringResource(R.string.pro_settings_email_leads), stringResource(R.string.pro_settings_email_leads_text), settings.emailLeads) { v ->
                        viewModel.onAction(SettingsAction.Change { it.copy(emailLeads = v) })
                    }
                    PanelSwitchRow(stringResource(R.string.pro_settings_whatsapp_leads), stringResource(R.string.pro_settings_whatsapp_leads_text), settings.whatsappLeads) { v ->
                        viewModel.onAction(SettingsAction.Change { it.copy(whatsappLeads = v) })
                    }
                    PanelSwitchRow(stringResource(R.string.pro_settings_membership), stringResource(R.string.pro_settings_membership_text), settings.membershipReminders) { v ->
                        viewModel.onAction(SettingsAction.Change { it.copy(membershipReminders = v) })
                    }
                    PanelSwitchRow(stringResource(R.string.pro_settings_weekly), stringResource(R.string.pro_settings_weekly_text), settings.weeklyStats) { v ->
                        viewModel.onAction(SettingsAction.Change { it.copy(weeklyStats = v) })
                    }
                }
            }
        }
    }
}
