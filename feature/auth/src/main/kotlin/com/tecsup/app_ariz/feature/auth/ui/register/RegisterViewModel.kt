package com.tecsup.app_ariz.feature.auth.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterCustomerData
import com.tecsup.app_ariz.feature.auth.domain.usecase.RegisterCustomerUseCase
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
class RegisterViewModel @Inject constructor(
    private val registerCustomerUseCase: RegisterCustomerUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events: Flow<RegisterEvent> = _events.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.FieldChanged -> _state.update {
                it.copy(
                    values = it.values + (action.field to action.value),
                    fieldErrors = it.fieldErrors - action.field,
                    error = null,
                )
            }
            is RegisterAction.TermsChanged ->
                _state.update { it.copy(acceptTerms = action.accepted, termsError = null) }
            RegisterAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLoading) return

        val fieldErrors = validate(current)
        val termsError = AuthValidators.validateTerms(current.acceptTerms)
        if (fieldErrors.isNotEmpty() || termsError != null) {
            _state.update { it.copy(fieldErrors = fieldErrors, termsError = termsError) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val data = RegisterCustomerData(
                firstName = current.valueOf(RegisterField.FIRST_NAME),
                lastName = current.valueOf(RegisterField.LAST_NAME),
                email = current.valueOf(RegisterField.EMAIL),
                phone = current.valueOf(RegisterField.PHONE),
                password = current.valueOf(RegisterField.PASSWORD),
            )
            when (val result = registerCustomerUseCase(data)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.Registered)
                }
                is AppResult.Failure ->
                    _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun validate(state: RegisterUiState): Map<RegisterField, FieldError> = buildMap {
        fun check(field: RegisterField, error: FieldError?) {
            if (error != null) put(field, error)
        }
        check(RegisterField.FIRST_NAME, AuthValidators.validateRequired(state.valueOf(RegisterField.FIRST_NAME)))
        check(RegisterField.LAST_NAME, AuthValidators.validateRequired(state.valueOf(RegisterField.LAST_NAME)))
        check(RegisterField.EMAIL, AuthValidators.validateEmail(state.valueOf(RegisterField.EMAIL)))
        check(RegisterField.PHONE, AuthValidators.validatePhone(state.valueOf(RegisterField.PHONE)))
        check(RegisterField.PASSWORD, AuthValidators.validateNewPassword(state.valueOf(RegisterField.PASSWORD)))
    }
}
