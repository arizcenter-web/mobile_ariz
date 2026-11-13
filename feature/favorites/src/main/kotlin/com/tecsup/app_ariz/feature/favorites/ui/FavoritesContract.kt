package com.tecsup.app_ariz.feature.favorites.ui

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.favorites.domain.Favorite
import com.tecsup.app_ariz.core.session.SessionState

data class FavoritesUiState(
    val session: SessionState = SessionState.Restoring,
    /** Lista tal como la devolvió el backend. */
    val items: List<Favorite> = emptyList(),
    /** Ids que siguen guardados: si el usuario quita uno, desaparece de la lista sin recargar. */
    val activeIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: AppError? = null,
) {
    /** Como el web: una vez cargada la lista, solo se muestran los que siguen guardados. */
    val visibleItems: List<Favorite>
        get() = if (hasLoaded) items.filter { it.propertyId in activeIds } else items
}

sealed interface FavoritesAction {
    data object Retry : FavoritesAction
    data class Remove(val propertyId: String) : FavoritesAction
}
