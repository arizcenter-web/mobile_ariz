package com.tecsup.app_ariz.feature.rewards.ui.referral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralAvailability
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralInterest
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralRequest
import com.tecsup.app_ariz.feature.rewards.domain.model.Referrer
import com.tecsup.app_ariz.feature.rewards.domain.usecase.SubmitReferralUseCase
import com.tecsup.app_ariz.feature.rewards.domain.validation.ReferralFieldError
import com.tecsup.app_ariz.feature.rewards.domain.validation.ReferralValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferralFormUiState(
    /** Datos de quien refiere (solo cuando aún no es miembro del club). */
    val referrerName: String = "",
    val referrerEmail: String = "",
    val referrerPhone: String = "",
    /** Datos de la persona referida. */
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val interest: ReferralInterest? = null,
    val availability: ReferralAvailability? = null,

    val errors: Map<ReferralField, ReferralFieldError> = emptyMap(),
    val isSending: Boolean = false,
    val error: AppError? = null,
    val sent: Boolean = false,
)

enum class ReferralField { REFERRER_NAME, REFERRER_EMAIL, REFERRER_PHONE, NAME, EMAIL, PHONE, INTEREST }

sealed interface ReferralFormAction {
    data class FieldChanged(val field: ReferralField, val value: String) : ReferralFormAction
    data class InterestChanged(val value: ReferralInterest) : ReferralFormAction
    data class AvailabilityChanged(val value: ReferralAvailability?) : ReferralFormAction

    /** [referrer] != null: quien refiere ya es miembro y no se piden sus datos (el backend usa los suyos). */
    data class Submit(val referrer: Referrer?) : ReferralFormAction
}

@HiltViewModel
class ReferralFormViewModel @Inject constructor(
    private val submitReferral: SubmitReferralUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReferralFormUiState())
    val state: StateFlow<ReferralFormUiState> = _state.asStateFlow()

    fun onAction(action: ReferralFormAction) {
        when (action) {
            is ReferralFormAction.FieldChanged -> _state.update { it.withField(action.field, action.value) }
            is ReferralFormAction.InterestChanged ->
                _state.update { it.copy(interest = action.value, errors = it.errors - ReferralField.INTEREST, error = null) }
            is ReferralFormAction.AvailabilityChanged -> _state.update { it.copy(availability = action.value) }
            is ReferralFormAction.Submit -> submit(action.referrer)
        }
    }

    private fun ReferralFormUiState.withField(field: ReferralField, value: String): ReferralFormUiState {
        val digitsOnly = field == ReferralField.PHONE || field == ReferralField.REFERRER_PHONE
        val clean = if (digitsOnly) value.filter { it.isDigit() }.take(PHONE_MAX_DIGITS) else value
        val updated = when (field) {
            ReferralField.REFERRER_NAME -> copy(referrerName = clean)
            ReferralField.REFERRER_EMAIL -> copy(referrerEmail = clean)
            ReferralField.REFERRER_PHONE -> copy(referrerPhone = clean)
            ReferralField.NAME -> copy(name = clean)
            ReferralField.EMAIL -> copy(email = clean)
            ReferralField.PHONE -> copy(phone = clean)
            ReferralField.INTEREST -> this
        }
        return updated.copy(errors = errors - field, error = null)
    }

    private fun submit(referrer: Referrer?) {
        val current = _state.value
        if (current.isSending) return

        val isNewMember = referrer == null
        val errors = buildMap {
            if (isNewMember) {
                ReferralValidator.validateRequired(current.referrerName)?.let { put(ReferralField.REFERRER_NAME, it) }
                ReferralValidator.validateEmail(current.referrerEmail)?.let { put(ReferralField.REFERRER_EMAIL, it) }
                ReferralValidator.validatePhone(current.referrerPhone)?.let { put(ReferralField.REFERRER_PHONE, it) }
            }
            ReferralValidator.validateRequired(current.name)?.let { put(ReferralField.NAME, it) }
            ReferralValidator.validateEmail(current.email)?.let { put(ReferralField.EMAIL, it) }
            ReferralValidator.validatePhone(current.phone)?.let { put(ReferralField.PHONE, it) }
            if (current.interest == null) put(ReferralField.INTEREST, ReferralFieldError.INTEREST_REQUIRED)
        }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }

        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            val request = ReferralRequest(
                referrerCode = referrer?.code,
                referrerName = referrer?.name ?: current.referrerName,
                referrerEmail = referrer?.email ?: current.referrerEmail,
                referrerPhone = referrer?.phone ?: ReferralValidator.toInternationalPhone(current.referrerPhone),
                name = current.name,
                email = current.email,
                phone = ReferralValidator.toInternationalPhone(current.phone),
                interest = current.interest!!,
                availability = current.availability,
            )
            when (val result = submitReferral(request)) {
                is AppResult.Success -> _state.update { ReferralFormUiState(sent = true) }
                is AppResult.Failure -> _state.update { it.copy(isSending = false, error = result.error) }
            }
        }
    }

    companion object {
        const val PHONE_MAX_DIGITS = 9
    }
}
