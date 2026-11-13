package com.tecsup.app_ariz.feature.projects.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.feature.projects.domain.usecase.SearchProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val searchProjects: SearchProjectsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectListUiState())
    val state: StateFlow<ProjectListUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        search(_state.value.filters)
    }

    fun onAction(action: ProjectListAction) {
        when (action) {
            is ProjectListAction.StageChanged -> applyFilters { it.copy(stage = action.value) }
            is ProjectListAction.DistrictChanged -> _state.update { it.copy(districtText = action.value) }
            ProjectListAction.DistrictSubmitted -> applyFilters { it }
            is ProjectListAction.OriginChanged -> applyFilters { it.copy(origin = action.value) }
            is ProjectListAction.CurrencyChanged -> applyFilters { it.copy(currency = action.value) }
            is ProjectListAction.BudgetChanged -> applyFilters { it.copy(priceMax = action.value) }
            is ProjectListAction.SortChanged -> applyFilters { it.copy(sort = action.value) }
            ProjectListAction.LoadMore -> loadMore()
            ProjectListAction.Retry -> if (_state.value.items.isEmpty()) search(_state.value.filters) else loadMore()
        }
    }

    private fun applyFilters(change: (ProjectFilters) -> ProjectFilters) {
        val current = _state.value
        search(change(current.filters).copy(district = current.districtText.trim().ifEmpty { null }))
    }

    private fun search(filters: ProjectFilters) {
        searchJob?.cancel()
        _state.update {
            it.copy(filters = filters, isLoading = true, isLoadingMore = false, error = null, items = emptyList(), page = 0, total = 0, hasMore = false)
        }
        searchJob = viewModelScope.launch {
            when (val result = searchProjects(filters, page = 1)) {
                is AppResult.Success -> _state.update {
                    it.copy(isLoading = false, items = result.data.items, total = result.data.total, page = result.data.page, hasMore = result.data.hasMore)
                }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        _state.update { it.copy(isLoadingMore = true, error = null) }
        searchJob = viewModelScope.launch {
            when (val result = searchProjects(current.filters, page = current.page + 1)) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoadingMore = false,
                        items = it.items + result.data.items,
                        total = result.data.total,
                        page = result.data.page,
                        hasMore = result.data.hasMore,
                    )
                }
                is AppResult.Failure -> _state.update { it.copy(isLoadingMore = false, error = result.error) }
            }
        }
    }
}
