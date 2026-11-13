package com.tecsup.app_ariz.feature.auth.ui.resetpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.auth.domain.usecase.ResetPasswordUseCase
import com.tecsup.app_ariz.feature.auth.domain.validation.AuthValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {

    // Argumento `token` de ResetPasswordRoute (llega por el enlace del correo).
    private val token: String = savedStateHandle.get<String>(TOKEN_ARG).orEmpty()

    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state.asStateFlow()

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            is ResetPasswordAction.PasswordChanged ->
                _state.update { it.copy(password = action.value, passwordError = null, error = null) }
            is ResetPasswordAction.ConfirmationChanged ->
                _state.update { it.copy(confirmation = action.value, confirmationError = null, error = null) }
            ResetPasswordAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLoading || current.isDone) return

        val passwordError = AuthValidators.validateNewPassword(current.password)
        val confirmationError = AuthValidators.validatePasswordConfirmation(current.password, current.confirmation)
        if (passwordError != null || confirmationError != null) {
            _state.update { it.copy(passwordError = passwordError, confirmationError = confirmationError) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = resetPasswordUseCase(token, current.password)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, isDone = true) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    companion object {
        const val TOKEN_ARG = "token"
    }
}
