package com.tecsup.app_ariz.feature.professional.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.professional.domain.ChangePasswordUseCase
import com.tecsup.app_ariz.feature.professional.domain.GetProfileUseCase
import com.tecsup.app_ariz.feature.professional.domain.GetSettingsUseCase
import com.tecsup.app_ariz.feature.professional.domain.LoadPlansUseCase
import com.tecsup.app_ariz.feature.professional.domain.NotificationSettings
import com.tecsup.app_ariz.feature.professional.domain.Plan
import com.tecsup.app_ariz.feature.professional.domain.PlansData
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalProfile
import com.tecsup.app_ariz.feature.professional.domain.ProfileUpdate
import com.tecsup.app_ariz.feature.professional.domain.RequestPlanUseCase
import com.tecsup.app_ariz.feature.professional.domain.UpdateProfileUseCase
import com.tecsup.app_ariz.feature.professional.domain.UpdateSettingsUseCase
import com.tecsup.app_ariz.feature.professional.domain.UploadPhotoUseCase
import com.tecsup.app_ariz.feature.professional.domain.isAgency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---- Suscripción ----

data class SubscriptionUiState(
    val isLoading: Boolean = true,
    /** Perfil (tipo y nombre) para decidir si se muestran planes o el contacto con ARIZ. */
    val profile: ProfessionalProfile? = null,
    val data: PlansData? = null,
    val error: AppError? = null,
    /** Plan que se está solicitando (para deshabilitar su botón). */
    val requestingPlanId: String? = null,
    val notice: PanelNotice? = null,
)

sealed interface SubscriptionAction {
    data object Refresh : SubscriptionAction

    /** Plan gratis: se solicita directo. */
    data class RequestFree(val plan: Plan) : SubscriptionAction

    /** Plan de pago: primero se sube el comprobante (foto) y con su URL se solicita. */
    data class RequestPaid(val plan: Plan, val receiptUri: String) : SubscriptionAction
    data object NoticeShown : SubscriptionAction
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val loadPlans: LoadPlansUseCase,
    private val requestPlan: RequestPlanUseCase,
    private val uploadPhoto: UploadPhotoUseCase,
    private val getProfile: GetProfileUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionUiState())
    val state: StateFlow<SubscriptionUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: SubscriptionAction) {
        when (action) {
            SubscriptionAction.Refresh -> load()
            is SubscriptionAction.RequestFree -> request(action.plan, receiptUri = null)
            is SubscriptionAction.RequestPaid -> request(action.plan, action.receiptUri)
            SubscriptionAction.NoticeShown -> _state.update { it.copy(notice = null) }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val profile = (getProfile() as? AppResult.Success)?.data
            _state.update { it.copy(profile = profile) }
            // Las inmobiliarias no usan planes: no hace falta pedirlos.
            if (profile?.type?.isAgency == true) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            when (val result = loadPlans()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, data = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun request(plan: Plan, receiptUri: String?) {
        if (_state.value.requestingPlanId != null) return
        _state.update { it.copy(requestingPlanId = plan.id) }
        viewModelScope.launch {
            var receiptUrl: String? = null
            if (receiptUri != null) {
                when (val upload = uploadPhoto(receiptUri, folder = "ariz/receipts")) {
                    is AppResult.Success -> receiptUrl = upload.data.url
                    is AppResult.Failure -> {
                        _state.update { it.copy(requestingPlanId = null, notice = PanelNotice.Failure(upload.error.message)) }
                        return@launch
                    }
                }
            }
            when (val result = requestPlan(plan, receiptUrl)) {
                is AppResult.Success -> {
                    _state.update { it.copy(requestingPlanId = null, notice = PanelNotice.Success("Solicitud enviada. Te avisaremos cuando sea revisada.")) }
                    load()
                }
                is AppResult.Failure -> _state.update { it.copy(requestingPlanId = null, notice = PanelNotice.Failure(result.error.message)) }
            }
        }
    }
}

// ---- Perfil y contraseña ----

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: ProfessionalProfile? = null,
    val form: ProfileUpdate? = null,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val notice: PanelNotice? = null,
    // Cambio de contraseña
    val currentPassword: String = "",
    val newPassword: String = "",
    val isChangingPassword: Boolean = false,
)

sealed interface ProfileAction {
    data object Retry : ProfileAction
    data class Edit(val change: (ProfileUpdate) -> ProfileUpdate) : ProfileAction
    data object Save : ProfileAction
    data class CurrentPasswordChanged(val value: String) : ProfileAction
    data class NewPasswordChanged(val value: String) : ProfileAction
    data object ChangePassword : ProfileAction
    data object NoticeShown : ProfileAction
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val changePassword: ChangePasswordUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.Retry -> load()
            is ProfileAction.Edit -> _state.update { s -> s.form?.let { s.copy(form = action.change(it)) } ?: s }
            ProfileAction.Save -> save()
            is ProfileAction.CurrentPasswordChanged -> _state.update { it.copy(currentPassword = action.value) }
            is ProfileAction.NewPasswordChanged -> _state.update { it.copy(newPassword = action.value) }
            ProfileAction.ChangePassword -> changePasswordNow()
            ProfileAction.NoticeShown -> _state.update { it.copy(notice = null) }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getProfile()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, profile = result.data, form = result.data.toForm()) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun save() {
        val form = _state.value.form ?: return
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = updateProfile(form)) {
                is AppResult.Success -> _state.update {
                    it.copy(isSaving = false, profile = result.data, form = result.data.toForm(), notice = PanelNotice.Success("Perfil actualizado."))
                }
                is AppResult.Failure -> _state.update { it.copy(isSaving = false, notice = PanelNotice.Failure(result.error.message)) }
            }
        }
    }

    private fun changePasswordNow() {
        val current = _state.value
        if (current.isChangingPassword) return
        if (current.currentPassword.isBlank() || current.newPassword.isBlank()) {
            _state.update { it.copy(notice = PanelNotice.Failure("Escribe tu contraseña actual y la nueva.")) }
            return
        }
        _state.update { it.copy(isChangingPassword = true) }
        viewModelScope.launch {
            when (val result = changePassword(current.currentPassword, current.newPassword)) {
                is AppResult.Success -> _state.update {
                    it.copy(isChangingPassword = false, currentPassword = "", newPassword = "", notice = PanelNotice.Success("Contraseña actualizada."))
                }
                is AppResult.Failure -> _state.update { it.copy(isChangingPassword = false, notice = PanelNotice.Failure(result.error.message)) }
            }
        }
    }

    private fun ProfessionalProfile.toForm() = ProfileUpdate(
        firstName = firstName, businessName = businessName, phone = phone, whatsapp = whatsapp, city = city,
        districts = districts, address = address, website = website, socialLinks = socialLinks, ruc = ruc,
    )
}

// ---- Ajustes de notificaciones ----

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: NotificationSettings? = null,
    val error: AppError? = null,
    val isSaving: Boolean = false,
    val notice: PanelNotice? = null,
)

sealed interface SettingsAction {
    data object Retry : SettingsAction
    data class Change(val change: (NotificationSettings) -> NotificationSettings) : SettingsAction
    data object NoticeShown : SettingsAction
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.Retry -> load()
            is SettingsAction.Change -> change(action.change)
            SettingsAction.NoticeShown -> _state.update { it.copy(notice = null) }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getSettings()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, settings = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    /** El interruptor cambia al instante; si el backend rechaza el cambio, vuelve a su valor anterior. */
    private fun change(change: (NotificationSettings) -> NotificationSettings) {
        val previous = _state.value.settings ?: return
        val next = change(previous)
        _state.update { it.copy(settings = next, isSaving = true) }
        viewModelScope.launch {
            when (val result = updateSettings(next)) {
                is AppResult.Success -> _state.update { it.copy(isSaving = false, settings = result.data) }
                is AppResult.Failure -> _state.update {
                    it.copy(isSaving = false, settings = previous, notice = PanelNotice.Failure(result.error.message))
                }
            }
        }
    }
}
