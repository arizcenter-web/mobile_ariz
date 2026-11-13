package com.tecsup.app_ariz.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.feature.search.domain.usecase.SearchPropertiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProperties: SearchPropertiesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        search(_state.value.filters)
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.DistrictChanged -> _state.update { it.copy(districtText = action.value) }
            SearchAction.DistrictSubmitted -> applyFilters { it }
            is SearchAction.OperationChanged -> applyFilters { it.copy(operation = action.value) }
            is SearchAction.TypeChanged -> applyFilters { it.copy(type = action.value) }
            is SearchAction.RoomsChanged -> applyFilters { it.copy(roomsMin = action.value) }
            is SearchAction.SortChanged -> applyFilters { it.copy(sort = action.value) }
            is SearchAction.PriceMinChanged -> _state.update { it.copy(priceMinText = action.value.digitsOnly()) }
            is SearchAction.PriceMaxChanged -> _state.update { it.copy(priceMaxText = action.value.digitsOnly()) }
            is SearchAction.AreaMinChanged -> _state.update { it.copy(areaMinText = action.value.digitsOnly()) }
            SearchAction.ToggleMoreFilters -> _state.update { it.copy(showMoreFilters = !it.showMoreFilters) }
            SearchAction.ApplyMoreFilters -> applyFilters { it }
            SearchAction.LoadMore -> loadMore()
            SearchAction.Retry -> if (_state.value.items.isEmpty()) search(_state.value.filters) else loadMore()
        }
    }

    /** Combina el cambio pedido con los textos escritos y lanza una búsqueda nueva desde la página 1. */
    private fun applyFilters(change: (PropertyFilters) -> PropertyFilters) {
        val current = _state.value
        val next = change(current.filters).copy(
            district = current.districtText.trim().ifEmpty { null },
            priceMin = current.priceMinText.toIntOrNull(),
            priceMax = current.priceMaxText.toIntOrNull(),
            areaMin = current.areaMinText.toIntOrNull(),
        )
        search(next)
    }

    private fun search(filters: PropertyFilters) {
        searchJob?.cancel()
        _state.update {
            it.copy(filters = filters, isLoading = true, isLoadingMore = false, error = null, items = emptyList(), page = 0, total = 0, hasMore = false)
        }
        searchJob = viewModelScope.launch {
            when (val result = searchProperties(filters, page = 1)) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        items = result.data.items,
                        total = result.data.total,
                        page = result.data.page,
                        hasMore = result.data.hasMore,
                    )
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
            when (val result = searchProperties(current.filters, page = current.page + 1)) {
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

    private fun String.digitsOnly() = filter { it.isDigit() }.take(9)
}
