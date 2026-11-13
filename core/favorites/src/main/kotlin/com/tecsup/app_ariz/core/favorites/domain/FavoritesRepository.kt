package com.tecsup.app_ariz.core.favorites.domain

import com.tecsup.app_ariz.core.common.AppResult
import kotlinx.coroutines.flow.StateFlow

/** Una propiedad guardada por el usuario (`GET /favorites`). */
data class Favorite(
    val id: String,
    val propertyId: String,
    val slug: String,
    val title: String,
    val savedAtMillis: Long?,
)

/**
 * Favoritos del usuario autenticado. Es un singleton: Inicio, Buscar, el detalle y la pestaña
 * Favoritos ven el mismo conjunto de ids, así que el corazón se actualiza en todas a la vez.
 */
interface FavoritesRepository {
    /** Ids de las propiedades favoritas. Vacío si no hay sesión; se recarga solo al iniciar sesión. */
    val favoriteIds: StateFlow<Set<String>>

    suspend fun list(): AppResult<List<Favorite>>

    /**
     * Guarda o quita la propiedad. Devuelve `true` si queda guardada. Sin sesión devuelve
     * `Failure(Unauthorized)`: quien llama debe llevar al usuario a iniciar sesión.
     */
    suspend fun toggle(propertyId: String): AppResult<Boolean>
}
