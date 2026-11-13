package com.tecsup.app_ariz.feature.favorites.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.feature.favorites.domain.usecase.GetFavoritesUseCase
import com.tecsup.app_ariz.feature.favorites.domain.usecase.ObserveFavoriteIdsUseCase
import com.tecsup.app_ariz.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesListViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val getFavorites: GetFavoritesUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    init {
        // Carga la lista cuando hay (o cambia) la sesión y la vacía al cerrar sesión.
        viewModelScope.launch {
            sessionManager.state.distinctUntilChangedBy { (it as? SessionState.SignedIn)?.user?.id ?: it }.collect { session ->
                _state.update { it.copy(session = session) }
                if (session is SessionState.SignedIn) {
                    load()
                } else {
                    _state.update { it.copy(items = emptyList(), hasLoaded = false, isLoading = false, error = null) }
                }
            }
        }
        viewModelScope.launch {
            observeFavoriteIds().collect { ids -> _state.update { it.copy(activeIds = ids) } }
        }
    }

    fun onAction(action: FavoritesAction) {
        when (action) {
            FavoritesAction.Retry -> load()
            is FavoritesAction.Remove -> viewModelScope.launch {
                // Si falla, el id sigue guardado y la fila se queda; no hay nada que revertir.
                toggleFavorite(action.propertyId)
            }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getFavorites()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, hasLoaded = true, items = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}
