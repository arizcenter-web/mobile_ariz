package com.tecsup.app_ariz.feature.investments.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentGoal
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentSimulation
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentType
import com.tecsup.app_ariz.feature.investments.domain.usecase.GetInvestmentCatalogUseCase
import com.tecsup.app_ariz.feature.investments.domain.usecase.SimulateInvestmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Valores iniciales y límites del evaluador, los mismos del web. */
object SimulatorDefaults {
    const val CAPITAL = 50_000
    const val CAPITAL_MIN = 3_000
    const val CAPITAL_MAX = 200_000
    const val CAPITAL_STEP = 1_000
    const val TERM_MONTHS = 24
    const val TERM_MIN = 6
    const val TERM_MAX = 48
    const val TERM_STEP = 6
}

data class InvestmentListUiState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val opportunities: List<InvestmentOpportunity> = emptyList(),
    val types: List<InvestmentType> = emptyList(),
    val selectedTypeId: String? = null,
    val capital: Int = SimulatorDefaults.CAPITAL,
    val termMonths: Int = SimulatorDefaults.TERM_MONTHS,
    val goal: InvestmentGoal = InvestmentGoal.PLUSVALIA,
    val simulation: InvestmentSimulation = InvestmentSimulation(emptyList(), 0),
) {
    /** Oportunidades de la categoría elegida (todas si no hay ninguna elegida). */
    val visible: List<InvestmentOpportunity>
        get() = if (selectedTypeId == null) opportunities else opportunities.filter { it.typeId == selectedTypeId }
}

sealed interface InvestmentListAction {
    data class TypeSelected(val typeId: String?) : InvestmentListAction
    data class CapitalChanged(val value: Int) : InvestmentListAction
    data class TermChanged(val value: Int) : InvestmentListAction
    data class GoalChanged(val value: InvestmentGoal) : InvestmentListAction
    data object Retry : InvestmentListAction
}

@HiltViewModel
class InvestmentListViewModel @Inject constructor(
    private val getCatalog: GetInvestmentCatalogUseCase,
    private val simulate: SimulateInvestmentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(InvestmentListUiState())
    val state: StateFlow<InvestmentListUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: InvestmentListAction) {
        when (action) {
            is InvestmentListAction.TypeSelected -> update { it.copy(selectedTypeId = action.typeId) }
            is InvestmentListAction.CapitalChanged -> update { it.copy(capital = action.value) }
            is InvestmentListAction.TermChanged -> update { it.copy(termMonths = action.value) }
            is InvestmentListAction.GoalChanged -> update { it.copy(goal = action.value) }
            InvestmentListAction.Retry -> load()
        }
    }

    /** Aplica el cambio y recalcula el evaluador sobre las oportunidades visibles. */
    private fun update(change: (InvestmentListUiState) -> InvestmentListUiState) {
        _state.update { current ->
            val next = change(current)
            next.copy(simulation = simulate(next.visible, next.capital, next.termMonths, next.goal))
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getCatalog()) {
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
                is AppResult.Success -> update {
                    it.copy(isLoading = false, opportunities = result.data.opportunities, types = result.data.types)
                }
            }
        }
    }
}
