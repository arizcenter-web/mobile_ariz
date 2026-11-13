package com.tecsup.app_ariz.feature.favorites.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.favorites.domain.Favorite
import com.tecsup.app_ariz.core.favorites.domain.FavoritesRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Lista de propiedades guardadas del usuario (`GET /favorites`). */
class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(): AppResult<List<Favorite>> = repository.list()
}

/** Ids favoritos en vivo: cuando se quita uno desde cualquier pantalla, la lista lo refleja. */
class ObserveFavoriteIdsUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(): StateFlow<Set<String>> = repository.favoriteIds
}

/** Quita (o vuelve a guardar) una propiedad. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(propertyId: String): AppResult<Boolean> = repository.toggle(propertyId)
}
