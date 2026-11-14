package com.tecsup.app_ariz.feature.map.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.feature.map.domain.LoadMapProjectsUseCase
import com.tecsup.app_ariz.feature.map.domain.filterBy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = true,
    val all: List<ProjectSummary> = emptyList(),
    val query: String = "",
    val category: ProjectCategory? = null,
    val selectedId: String? = null,
    val error: AppError? = null,
) {
    val visible: List<ProjectSummary> get() = all.filterBy(query, category)
    val selected: ProjectSummary? get() = visible.firstOrNull { it.id == selectedId }
}

@HiltViewModel
class MapViewModel @Inject constructor(private val loadProjects: LoadMapProjectsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loadProjects()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, all = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    fun onQuery(value: String) = _state.update { it.copy(query = value.take(80), selectedId = null) }

    fun onCategory(value: ProjectCategory?) = _state.update { it.copy(category = value, selectedId = null) }

    fun select(id: String?) = _state.update { it.copy(selectedId = id) }
}
