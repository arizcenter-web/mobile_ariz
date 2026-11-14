package com.tecsup.app_ariz.feature.info.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.info.domain.ClaimDraft
import com.tecsup.app_ariz.feature.info.domain.ClaimError
import com.tecsup.app_ariz.feature.info.domain.ClaimField
import com.tecsup.app_ariz.feature.info.domain.ClaimReceipt
import com.tecsup.app_ariz.feature.info.domain.ClaimRepository
import com.tecsup.app_ariz.feature.info.domain.ClaimValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClaimUiState(
    val draft: ClaimDraft = ClaimDraft(),
    val errors: Map<ClaimField, ClaimError> = emptyMap(),
    val isSending: Boolean = false,
    val error: AppError? = null,
    /** Tras enviar se muestra el código del reclamo en lugar del formulario. */
    val receipt: ClaimReceipt? = null,
)

@HiltViewModel
class ClaimViewModel @Inject constructor(private val repository: ClaimRepository) : ViewModel() {
    private val _state = MutableStateFlow(ClaimUiState())
    val state: StateFlow<ClaimUiState> = _state.asStateFlow()

    fun edit(change: (ClaimDraft) -> ClaimDraft) = _state.update { it.copy(draft = change(it.draft), errors = emptyMap(), error = null) }

    fun submit() {
        val current = _state.value
        if (current.isSending) return
        val errors = ClaimValidator.validate(current.draft)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.submit(current.draft)) {
                is AppResult.Success -> _state.update { ClaimUiState(receipt = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isSending = false, error = result.error) }
            }
        }
    }

    /** Vuelve a un formulario limpio (para registrar otro reclamo). */
    fun reset() = _state.update { ClaimUiState() }
}
