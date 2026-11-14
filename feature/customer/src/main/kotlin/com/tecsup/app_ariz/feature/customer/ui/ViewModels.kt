package com.tecsup.app_ariz.feature.customer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.customer.domain.ChangeCustomerPasswordUseCase
import com.tecsup.app_ariz.feature.customer.domain.CustomerProfile
import com.tecsup.app_ariz.feature.customer.domain.CustomerProfileUpdate
import com.tecsup.app_ariz.feature.customer.domain.GetCustomerProfileUseCase
import com.tecsup.app_ariz.feature.customer.domain.GetMembershipUseCase
import com.tecsup.app_ariz.feature.customer.domain.GetPropertyStatsUseCase
import com.tecsup.app_ariz.feature.customer.domain.MembershipPackage
import com.tecsup.app_ariz.feature.customer.domain.ObserveFavoriteCountUseCase
import com.tecsup.app_ariz.feature.customer.domain.PropertyStats
import com.tecsup.app_ariz.feature.customer.domain.MembershipSummary
import com.tecsup.app_ariz.feature.customer.domain.RequestMembershipUseCase
import com.tecsup.app_ariz.feature.customer.domain.UpdateCustomerProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Resultado de la última acción, mostrado una vez en un aviso. */
sealed interface CustomerNotice {
    data class Success(val message: String) : CustomerNotice
    data class Failure(val message: String) : CustomerNotice
}

// ---- Dashboard + membresía (comparten la misma carga) ----

data class MembershipUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val summary: MembershipSummary? = null,
    val error: AppError? = null,
    val isRequesting: Boolean = false,
    val notice: CustomerNotice? = null,
)

@HiltViewModel
class MembershipViewModel @Inject constructor(
    private val getMembership: GetMembershipUseCase,
    private val getProfile: GetCustomerProfileUseCase,
    private val requestMembership: RequestMembershipUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MembershipUiState())
    val state: StateFlow<MembershipUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val name = (getProfile() as? AppResult.Success)?.data?.firstName.orEmpty()
            when (val result = getMembership()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, summary = result.data, firstName = name) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error, firstName = name) }
            }
        }
    }

    fun request(pack: MembershipPackage, receiptUri: String, successMessage: String) {
        if (_state.value.isRequesting) return
        _state.update { it.copy(isRequesting = true) }
        viewModelScope.launch {
            when (val result = requestMembership(pack, receiptUri)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isRequesting = false, notice = CustomerNotice.Success(successMessage)) }
                    load()
                }
                is AppResult.Failure -> _state.update { it.copy(isRequesting = false, notice = CustomerNotice.Failure(result.error.message)) }
            }
        }
    }

    fun noticeShown() = _state.update { it.copy(notice = null) }
}

// ---- Dashboard ----

data class CustomerDashboardUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val summary: MembershipSummary? = null,
    val stats: PropertyStats? = null,
    val favorites: Int = 0,
    val error: AppError? = null,
)

@HiltViewModel
class CustomerDashboardViewModel @Inject constructor(
    private val getMembership: GetMembershipUseCase,
    private val getProfile: GetCustomerProfileUseCase,
    private val getStats: GetPropertyStatsUseCase,
    private val favoriteCount: ObserveFavoriteCountUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(CustomerDashboardUiState())
    val state: StateFlow<CustomerDashboardUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch { favoriteCount().collect { count -> _state.update { it.copy(favorites = count) } } }
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val name = (getProfile() as? AppResult.Success)?.data?.firstName.orEmpty()
            val stats = (getStats() as? AppResult.Success)?.data
            when (val result = getMembership()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, firstName = name, summary = result.data, stats = stats) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, firstName = name, error = result.error) }
            }
        }
    }
}

// ---- Perfil ----

data class CustomerProfileUiState(
    val isLoading: Boolean = true,
    val profile: CustomerProfile? = null,
    val form: CustomerProfileUpdate? = null,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val notice: CustomerNotice? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val isChangingPassword: Boolean = false,
)

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    private val getProfile: GetCustomerProfileUseCase,
    private val updateProfile: UpdateCustomerProfileUseCase,
    private val changePasswordUseCase: ChangeCustomerPasswordUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(CustomerProfileUiState())
    val state: StateFlow<CustomerProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getProfile()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, profile = result.data, form = result.data.toForm()) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    fun edit(change: (CustomerProfileUpdate) -> CustomerProfileUpdate) = _state.update { s -> s.form?.let { s.copy(form = change(it)) } ?: s }
    fun currentPasswordChanged(value: String) = _state.update { it.copy(currentPassword = value) }
    fun newPasswordChanged(value: String) = _state.update { it.copy(newPassword = value) }
    fun noticeShown() = _state.update { it.copy(notice = null) }

    fun save(successMessage: String) {
        val form = _state.value.form ?: return
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = updateProfile(form)) {
                is AppResult.Success -> _state.update {
                    it.copy(isSaving = false, profile = result.data, form = result.data.toForm(), notice = CustomerNotice.Success(successMessage))
                }
                is AppResult.Failure -> _state.update { it.copy(isSaving = false, notice = CustomerNotice.Failure(result.error.message)) }
            }
        }
    }

    fun changePassword(missingMessage: String, doneMessage: String) {
        val current = _state.value
        if (current.isChangingPassword) return
        if (current.currentPassword.isBlank() || current.newPassword.isBlank()) {
            _state.update { it.copy(notice = CustomerNotice.Failure(missingMessage)) }
            return
        }
        _state.update { it.copy(isChangingPassword = true) }
        viewModelScope.launch {
            when (val result = changePasswordUseCase(current.currentPassword, current.newPassword)) {
                is AppResult.Success -> _state.update {
                    it.copy(isChangingPassword = false, currentPassword = "", newPassword = "", notice = CustomerNotice.Success(doneMessage))
                }
                is AppResult.Failure -> _state.update { it.copy(isChangingPassword = false, notice = CustomerNotice.Failure(result.error.message)) }
            }
        }
    }

    private fun CustomerProfile.toForm() = CustomerProfileUpdate(firstName, lastName, phone, biography)
}
