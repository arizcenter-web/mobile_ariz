package com.tecsup.app_ariz.core.favorites.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.favorites.domain.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Eventos de una sola vez que el corazón le pide a la pantalla. */
sealed interface FavoritesEvent {
    /** No hay sesión: llevar al usuario a iniciar sesión. */
    data object LoginRequired : FavoritesEvent

    /** El backend no pudo guardar el cambio (red, servidor...). */
    data class Failed(val error: AppError) : FavoritesEvent
}

/**
 * ViewModel compartido por las pantallas que muestran un corazón (Inicio, Buscar, detalle).
 * Expone los ids favoritos y el "guardar o quitar"; el estado real vive en [FavoritesRepository].
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository,
) : ViewModel() {

    val favoriteIds: StateFlow<Set<String>> = repository.favoriteIds

    private val _events = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events: Flow<FavoritesEvent> = _events.receiveAsFlow()

    fun onToggle(propertyId: String) {
        viewModelScope.launch {
            when (val result = repository.toggle(propertyId)) {
                is AppResult.Success -> Unit
                is AppResult.Failure -> _events.send(
                    if (result.error is AppError.Unauthorized) FavoritesEvent.LoginRequired else FavoritesEvent.Failed(result.error),
                )
            }
        }
    }
}
