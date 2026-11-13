package com.tecsup.app_ariz.core.favorites.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Lo que una pantalla necesita para dibujar corazones: qué ids están guardados y qué hacer al tocar uno. */
@Immutable
data class FavoritesUi(
    val ids: Set<String> = emptySet(),
    val onToggle: (propertyId: String) -> Unit = {},
) {
    fun isFavorite(propertyId: String): Boolean = propertyId in ids
}

/**
 * Conecta una pantalla con los favoritos del usuario.
 *
 * @param onLoginRequired se invoca al tocar un corazón sin sesión (normalmente navegar al login).
 * @param onError se invoca si el backend no pudo guardar el cambio, con el texto a mostrar.
 */
@Composable
fun rememberFavorites(
    onLoginRequired: () -> Unit,
    onError: (message: String) -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel(),
): FavoritesUi {
    val ids by viewModel.favoriteIds.collectAsStateWithLifecycle()
    // Los callbacks pueden cambiar entre recomposiciones; el collect solo se arranca una vez.
    val currentOnLoginRequired by rememberUpdatedState(onLoginRequired)
    val currentOnError by rememberUpdatedState(onError)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                FavoritesEvent.LoginRequired -> currentOnLoginRequired()
                is FavoritesEvent.Failed -> currentOnError(event.error.message)
            }
        }
    }
    return remember(ids, viewModel) { FavoritesUi(ids = ids, onToggle = viewModel::onToggle) }
}
