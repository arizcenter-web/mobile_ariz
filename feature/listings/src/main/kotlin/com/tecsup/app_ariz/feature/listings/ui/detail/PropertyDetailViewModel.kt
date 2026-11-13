package com.tecsup.app_ariz.feature.listings.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.feature.listings.domain.usecase.CreatePriceAlertUseCase
import com.tecsup.app_ariz.feature.listings.domain.usecase.GetPropertyDetailUseCase
import com.tecsup.app_ariz.feature.listings.domain.usecase.GetSimilarPropertiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPropertyDetail: GetPropertyDetailUseCase,
    private val getSimilarProperties: GetSimilarPropertiesUseCase,
    private val createPriceAlert: CreatePriceAlertUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    // Argumento `slug` de PropertyDetailRoute.
    private val slug: String = savedStateHandle.get<String>(SLUG_ARG).orEmpty()

    private val _state = MutableStateFlow(PropertyDetailUiState())
    val state: StateFlow<PropertyDetailUiState> = _state.asStateFlow()

    private val _events = Channel<PropertyDetailEvent>(Channel.BUFFERED)
    val events: Flow<PropertyDetailEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: PropertyDetailAction) {
        when (action) {
            PropertyDetailAction.Retry -> load()
            PropertyDetailAction.CreatePriceAlert -> requestPriceAlert()
        }
    }

    private fun requestPriceAlert() {
        val detail = _state.value.detail ?: return
        if (_state.value.priceAlert == PriceAlertStatus.CREATING || _state.value.priceAlert == PriceAlertStatus.CREATED) return
        if (sessionManager.state.value !is SessionState.SignedIn) {
            viewModelScope.launch { _events.send(PropertyDetailEvent.LoginRequired) }
            return
        }
        _state.update { it.copy(priceAlert = PriceAlertStatus.CREATING) }
        viewModelScope.launch {
            val status = when (createPriceAlert(detail.id)) {
                is AppResult.Success -> PriceAlertStatus.CREATED
                is AppResult.Failure -> PriceAlertStatus.FAILED
            }
            _state.update { it.copy(priceAlert = status) }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getPropertyDetail(slug)) {
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
                is AppResult.Success -> {
                    _state.update { it.copy(isLoading = false, detail = result.data) }
                    // Los similares llegan después y no bloquean la ficha.
                    val similar = getSimilarProperties(result.data)
                    _state.update { it.copy(similar = similar) }
                }
            }
        }
    }

    companion object {
        const val SLUG_ARG = "slug"
    }
}
