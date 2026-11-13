package com.tecsup.app_ariz.feature.auth.ui.registerprofessional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData
import com.tecsup.app_ariz.feature.auth.domain.usecase.RegisterProfessionalUseCase
import com.tecsup.app_ariz.feature.auth.domain.validation.AuthValidators
import com.tecsup.app_ariz.feature.auth.domain.validation.FieldError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterProfessionalViewModel @Inject constructor(
    private val registerProfessionalUseCase: RegisterProfessionalUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterProfessionalUiState())
    val state: StateFlow<RegisterProfessionalUiState> = _state.asStateFlow()

    private val _events = Channel<RegisterProfessionalEvent>(Channel.BUFFERED)
    val events: Flow<RegisterProfessionalEvent> = _events.receiveAsFlow()

    fun onAction(action: RegisterProfessionalAction) {
        when (action) {
            is RegisterProfessionalAction.TypeChanged ->
                _state.update { it.copy(type = action.type, fieldErrors = emptyMap(), error = null) }
            is RegisterProfessionalAction.FieldChanged -> _state.update {
                it.copy(
                    values = it.values + (action.field to action.value),
                    fieldErrors = it.fieldErrors - action.field,
                    error = null,
                )
            }
            is RegisterProfessionalAction.TermsChanged ->
                _state.update { it.copy(acceptTerms = action.accepted, termsError = null) }
            RegisterProfessionalAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLoading) return

        val fieldErrors = buildMap {
            current.visibleFields.forEach { field ->
                validate(field, current.valueOf(field))?.let { put(field, it) }
            }
        }
        val termsError = AuthValidators.validateTerms(current.acceptTerms)
        if (fieldErrors.isNotEmpty() || termsError != null) {
            _state.update { it.copy(fieldErrors = fieldErrors, termsError = termsError) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = registerProfessionalUseCase(current.toData())) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(RegisterProfessionalEvent.Registered)
                }
                is AppResult.Failure ->
                    _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun validate(field: ProfessionalField, value: String): FieldError? = when (field) {
        ProfessionalField.FIRST_NAME,
        ProfessionalField.LAST_NAME,
        ProfessionalField.BUSINESS_NAME,
        ProfessionalField.CITY -> AuthValidators.validateRequired(value)
        ProfessionalField.DNI -> AuthValidators.validateDni(value)
        ProfessionalField.RUC -> AuthValidators.validateRuc(value)
        ProfessionalField.EMAIL -> AuthValidators.validateEmail(value)
        ProfessionalField.PHONE,
        ProfessionalField.WHATSAPP -> AuthValidators.validatePhone(value)
        ProfessionalField.PASSWORD -> AuthValidators.validateNewPassword(value)
        ProfessionalField.DISTRICTS,
        ProfessionalField.ADDRESS,
        ProfessionalField.WEBSITE,
        ProfessionalField.SOCIAL_LINKS -> null
    }

    private fun RegisterProfessionalUiState.toData(): RegisterProfessionalData {
        // Solo se envían los campos que aplican al tipo elegido.
        fun valueIfVisible(field: ProfessionalField): String? =
            if (field in visibleFields) valueOf(field) else null

        return RegisterProfessionalData(
            type = type,
            firstName = valueIfVisible(ProfessionalField.FIRST_NAME),
            lastName = valueIfVisible(ProfessionalField.LAST_NAME),
            dni = valueIfVisible(ProfessionalField.DNI),
            businessName = valueIfVisible(ProfessionalField.BUSINESS_NAME),
            ruc = valueIfVisible(ProfessionalField.RUC),
            email = valueOf(ProfessionalField.EMAIL),
            phone = valueOf(ProfessionalField.PHONE),
            whatsapp = valueOf(ProfessionalField.WHATSAPP),
            city = valueOf(ProfessionalField.CITY),
            districts = valueOf(ProfessionalField.DISTRICTS),
            address = valueOf(ProfessionalField.ADDRESS),
            website = valueOf(ProfessionalField.WEBSITE),
            socialLinks = valueOf(ProfessionalField.SOCIAL_LINKS),
            password = valueOf(ProfessionalField.PASSWORD),
        )
    }
}
