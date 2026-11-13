package com.tecsup.app_ariz.feature.projects.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.projects.domain.usecase.GetProjectDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectDetail: GetProjectDetailUseCase,
) : ViewModel() {

    // Argumento `slug` de ProjectDetailRoute.
    private val slug: String = savedStateHandle.get<String>(SLUG_ARG).orEmpty()

    private val _state = MutableStateFlow(ProjectDetailUiState())
    val state: StateFlow<ProjectDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: ProjectDetailAction) {
        when (action) {
            ProjectDetailAction.Retry -> load()
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getProjectDetail(slug)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, detail = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    companion object {
        const val SLUG_ARG = "slug"
    }
}
