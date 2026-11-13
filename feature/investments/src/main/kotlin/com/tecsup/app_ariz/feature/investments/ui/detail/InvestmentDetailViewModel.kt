package com.tecsup.app_ariz.feature.investments.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.usecase.GetInvestmentOpportunityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestmentDetailUiState(
    val isLoading: Boolean = true,
    val opportunity: InvestmentOpportunity? = null,
    val error: AppError? = null,
)

sealed interface InvestmentDetailAction {
    data object Retry : InvestmentDetailAction
}

@HiltViewModel
class InvestmentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOpportunity: GetInvestmentOpportunityUseCase,
) : ViewModel() {

    // Argumento `slug` de InvestmentDetailRoute.
    private val slug: String = savedStateHandle.get<String>(SLUG_ARG).orEmpty()

    private val _state = MutableStateFlow(InvestmentDetailUiState())
    val state: StateFlow<InvestmentDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: InvestmentDetailAction) {
        when (action) {
            InvestmentDetailAction.Retry -> load()
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getOpportunity(slug)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, opportunity = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    companion object {
        const val SLUG_ARG = "slug"
    }
}
