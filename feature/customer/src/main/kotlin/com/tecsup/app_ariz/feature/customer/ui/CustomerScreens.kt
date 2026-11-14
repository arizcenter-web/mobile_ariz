package com.tecsup.app_ariz.feature.customer.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardMembership
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.tecsup.app_ariz.core.ui.component.PanelMenuRow
import com.tecsup.app_ariz.core.ui.component.PanelPage
import com.tecsup.app_ariz.core.ui.component.StatTile
import com.tecsup.app_ariz.core.ui.component.StatusPill
import com.tecsup.app_ariz.feature.customer.R
import com.tecsup.app_ariz.feature.customer.domain.MembershipPackage
import com.tecsup.app_ariz.feature.customer.domain.MembershipStatus
import com.tecsup.app_ariz.feature.customer.domain.MembershipSummary

/** Destinos que el dashboard ofrece; los de propiedades los resuelve `:app` (reutiliza el panel de propiedades). */
data class CustomerDestinations(
    val onPublish: () -> Unit,
    val onProperties: () -> Unit,
    val onRentals: () -> Unit,
    val onMembership: () -> Unit,
    val onProfile: () -> Unit,
    val onFavorites: () -> Unit,
)

@Composable
private fun WithNotice(notice: CustomerNotice?, onShown: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val host = remember { SnackbarHostState() }
    LaunchedEffect(notice) {
        notice?.let {
            host.showSnackbar(when (it) { is CustomerNotice.Success -> it.message; is CustomerNotice.Failure -> it.message })
            onShown()
        }
    }
    Box(modifier.fillMaxSize()) {
        content()
        SnackbarHost(host, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    }
}

@Composable
fun CustomerDashboardScreen(
    onBackClick: () -> Unit,
    destinations: CustomerDestinations,
    modifier: Modifier = Modifier,
    viewModel: CustomerDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val summary = state.summary
    PanelPage(
        eyebrow = stringResource(R.string.cus_dashboard_welcome),
        title = stringResource(R.string.cus_dashboard_hello, state.firstName.substringBefore(' ')),
        subtitle = stringResource(R.string.cus_dashboard_intro),
        onBackClick = onBackClick,
        backDescription = stringResource(R.string.cus_back),
        modifier = modifier,
    ) {
        when {
            state.isLoading -> PanelLoading()
            summary == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.cus_retry), onRetry = viewModel::load)
            else -> {
                val stats = state.stats
                val days = summary.daysRemaining(System.currentTimeMillis())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        label = stringResource(R.string.cus_stat_properties),
                        value = (stats?.total ?: 0).toString(),
                        caption = stringResource(R.string.cus_stat_properties_caption, stats?.published ?: 0, stats?.paused ?: 0),
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = stringResource(R.string.cus_stat_rentals),
                        value = (stats?.rentals ?: 0).toString(),
                        caption = stringResource(R.string.cus_stat_rentals_caption),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        label = stringResource(R.string.cus_stat_favorites),
                        value = state.favorites.toString(),
                        caption = stringResource(R.string.cus_stat_favorites_caption),
                        modifier = Modifier.weight(1f),
                    )
                    if (summary.trialAvailable) {
                        StatTile(
                            label = stringResource(R.string.cus_stat_trial),
                            value = stringResource(R.string.cus_stat_trial_value),
                            caption = stringResource(R.string.cus_stat_trial_caption),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        StatTile(
                            label = stringResource(R.string.cus_stat_membership),
                            value = when {
                                days != null -> stringResource(R.string.cus_stat_days_value, days)
                                summary.membership?.status == MembershipStatus.PENDING_PAYMENT -> stringResource(R.string.cus_stat_in_review)
                                else -> stringResource(R.string.cus_stat_no_plan)
                            },
                            caption = stringResource(R.string.cus_stat_membership_caption),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.cus_menu_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
                ArizPrimaryButton(stringResource(R.string.cus_menu_publish), destinations.onPublish, Modifier.fillMaxWidth())
                PanelMenuRow(Icons.Outlined.Home, stringResource(R.string.cus_menu_properties), destinations.onProperties)
                PanelMenuRow(Icons.Outlined.Key, stringResource(R.string.cus_menu_rentals), destinations.onRentals)
                PanelMenuRow(Icons.Outlined.FavoriteBorder, stringResource(R.string.cus_menu_favorites), destinations.onFavorites)
                PanelMenuRow(Icons.Outlined.CardMembership, stringResource(R.string.cus_menu_membership), destinations.onMembership)
                PanelMenuRow(Icons.Outlined.Person, stringResource(R.string.cus_menu_profile), destinations.onProfile)
            }
        }
    }
}

@Composable
fun MembershipScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: MembershipViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf<MembershipPackage?>(null) }
    val sentMessage = stringResource(R.string.cus_membership_sent)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val pack = pending
        pending = null
        if (pack != null && uri != null) viewModel.request(pack, uri.toString(), sentMessage)
    }

    WithNotice(state.notice, onShown = viewModel::noticeShown, modifier = modifier) {
        PanelPage(
            eyebrow = stringResource(R.string.cus_eyebrow),
            title = stringResource(R.string.cus_membership_title),
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.cus_back),
        ) {
            val summary = state.summary
            when {
                state.isLoading -> PanelLoading()
                summary == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.cus_retry), onRetry = viewModel::load)
                else -> {
                    if (summary.trialAvailable) TrialNotice()
                    CurrentMembership(summary)
                    Text(
                        text = stringResource(R.string.cus_packages_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    Text(stringResource(R.string.cus_package_hint), style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted)
                    MembershipPackage.All.forEach { pack ->
                        PackageCard(pack, busy = state.isRequesting) {
                            pending = pack
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrialNotice() {
    PanelCard {
        Text(stringResource(R.string.cus_trial_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Success)
        Text(stringResource(R.string.cus_trial_text), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
    }
}

@Composable
private fun CurrentMembership(summary: MembershipSummary) {
    PanelCard {
        Text(
            text = stringResource(R.string.cus_membership_current).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = ArizColors.Accent,
        )
        val m = summary.membership
        if (m == null) {
            Text(stringResource(R.string.cus_membership_none), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
            Text(stringResource(R.string.cus_membership_none_text), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
            return@PanelCard
        }
        val (text, color) = when (m.status) {
            MembershipStatus.ACTIVE -> stringResource(R.string.cus_membership_active, m.propertyLimit, summary.daysRemaining(System.currentTimeMillis()) ?: 0) to ArizColors.Success
            MembershipStatus.PENDING_PAYMENT -> stringResource(R.string.cus_membership_pending) to ArizColors.Warning
            MembershipStatus.REJECTED -> stringResource(R.string.cus_membership_rejected, m.rejectionReason.orEmpty()) to ArizColors.Error
            MembershipStatus.EXPIRED -> stringResource(R.string.cus_membership_expired) to ArizColors.Muted
        }
        StatusPill(text, color)
        Text(stringResource(R.string.cus_membership_published, summary.publishedCount), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
    }
}

@Composable
private fun PackageCard(pack: MembershipPackage, busy: Boolean, onChoose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.cus_package_properties, pack.propertyLimit),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            Text(stringResource(R.string.cus_package_days, pack.durationDays), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
            Text(
                text = "S/ " + pack.price.toInt(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = ArizColors.Ink,
            )
            ArizPrimaryButton(stringResource(R.string.cus_package_choose), onChoose, Modifier.fillMaxWidth(), isLoading = busy)
        }
    }
}

@Composable
fun CustomerProfileScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, viewModel: CustomerProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile
    val form = state.form
    val saved = stringResource(R.string.cus_profile_saved)
    val missing = stringResource(R.string.cus_password_missing)
    val done = stringResource(R.string.cus_password_done)

    WithNotice(state.notice, onShown = viewModel::noticeShown, modifier = modifier) {
        PanelPage(
            eyebrow = stringResource(R.string.cus_eyebrow),
            title = stringResource(R.string.cus_profile_title),
            onBackClick = onBackClick,
            backDescription = stringResource(R.string.cus_back),
        ) {
            when {
                state.isLoading -> PanelLoading()
                profile == null || form == null -> PanelError(state.error?.message.orEmpty(), stringResource(R.string.cus_retry), onRetry = viewModel::load)
                else -> {
                    PanelCard {
                        PanelField(stringResource(R.string.cus_profile_email), profile.email)
                        Field(R.string.cus_profile_first_name, form.firstName) { v -> viewModel.edit { it.copy(firstName = v) } }
                        Field(R.string.cus_profile_last_name, form.lastName) { v -> viewModel.edit { it.copy(lastName = v) } }
                        Field(R.string.cus_profile_phone, form.phone, KeyboardType.Phone) { v -> viewModel.edit { it.copy(phone = v) } }
                        Field(R.string.cus_profile_bio, form.biography) { v -> viewModel.edit { it.copy(biography = v) } }
                        ArizPrimaryButton(stringResource(R.string.cus_profile_save), { viewModel.save(saved) }, Modifier.fillMaxWidth(), isLoading = state.isSaving)
                    }
                    PanelCard {
                        Text(stringResource(R.string.cus_password_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                        ArizTextField(
                            value = state.currentPassword,
                            onValueChange = viewModel::currentPasswordChanged,
                            label = stringResource(R.string.cus_password_current),
                            modifier = Modifier.fillMaxWidth(),
                            isPassword = true,
                        )
                        ArizTextField(
                            value = state.newPassword,
                            onValueChange = viewModel::newPasswordChanged,
                            label = stringResource(R.string.cus_password_new),
                            modifier = Modifier.fillMaxWidth(),
                            isPassword = true,
                        )
                        ArizPrimaryButton(
                            stringResource(R.string.cus_password_change),
                            { viewModel.changePassword(missing, done) },
                            Modifier.fillMaxWidth(),
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
    ArizTextField(value = value, onValueChange = onChange, label = stringResource(labelRes), modifier = Modifier.fillMaxWidth(), keyboardType = keyboardType)
}
