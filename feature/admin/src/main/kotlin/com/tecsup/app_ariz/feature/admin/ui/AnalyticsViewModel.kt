package com.tecsup.app_ariz.feature.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsRaw
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsView
import com.tecsup.app_ariz.feature.admin.domain.LoadAdminAnalyticsUseCase
import com.tecsup.app_ariz.feature.admin.domain.ProfileFilter
import com.tecsup.app_ariz.feature.admin.domain.RangeFilter
import com.tecsup.app_ariz.feature.admin.domain.buildAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val profile: ProfileFilter = ProfileFilter.ALL,
    val range: RangeFilter = RangeFilter.SIX,
    val view: AnalyticsView? = null,
    val error: AppError? = null,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(private val loadAnalytics: LoadAdminAnalyticsUseCase) : ViewModel() {
    private var raw: AnalyticsRaw? = null
    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loadAnalytics()) {
                is AppResult.Success -> {
                    raw = result.data
                    _state.update { it.copy(isLoading = false, view = buildAnalytics(result.data, it.profile, it.range)) }
                }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    fun select(profile: ProfileFilter? = null, range: RangeFilter? = null) {
        _state.update { current ->
            val p = profile ?: current.profile
            val r = range ?: current.range
            current.copy(profile = p, range = r, view = raw?.let { buildAnalytics(it, p, r) } ?: current.view)
        }
    }
}
