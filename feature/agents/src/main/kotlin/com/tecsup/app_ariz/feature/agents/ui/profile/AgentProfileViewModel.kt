package com.tecsup.app_ariz.feature.agents.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.agents.domain.model.AgentProfile
import com.tecsup.app_ariz.feature.agents.domain.usecase.GetAgentProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentProfileUiState(
    val isLoading: Boolean = true,
    val profile: AgentProfile? = null,
    val error: AppError? = null,
)

sealed interface AgentProfileAction {
    data object Retry : AgentProfileAction
}

@HiltViewModel
class AgentProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAgentProfile: GetAgentProfileUseCase,
) : ViewModel() {

    // Argumento `id` de AgentProfileRoute: el id (o slug) del perfil profesional.
    private val agentId: String = savedStateHandle.get<String>(ID_ARG).orEmpty()

    private val _state = MutableStateFlow(AgentProfileUiState())
    val state: StateFlow<AgentProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: AgentProfileAction) {
        when (action) {
            AgentProfileAction.Retry -> load()
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getAgentProfile(agentId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, profile = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    companion object {
        const val ID_ARG = "id"
    }
}
