package com.tecsup.app_ariz.core.favorites.data.repository

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.favorites.data.remote.FavoritesApi
import com.tecsup.app_ariz.core.favorites.data.remote.ToggleRequest
import com.tecsup.app_ariz.core.favorites.domain.Favorite
import com.tecsup.app_ariz.core.favorites.domain.FavoritesRepository
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultFavoritesRepository(
    private val api: FavoritesApi,
    private val session: SessionManager,
    // Vive lo que el proceso: solo observa la sesión para cargar o vaciar los ids.
    scope: CoroutineScope,
) : FavoritesRepository {

    @Inject
    constructor(api: FavoritesApi, session: SessionManager) :
        this(api, session, CoroutineScope(SupervisorJob() + Dispatchers.Default))

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        scope.launch {
            // Importa quién está dentro, no cada emisión: así no se recarga al renovar el estado.
            session.state.distinctUntilChangedBy { (it as? SessionState.SignedIn)?.user?.id }.collect { state ->
                if (state is SessionState.SignedIn) refreshIds() else _favoriteIds.value = emptySet()
            }
        }
    }

    override suspend fun list(): AppResult<List<Favorite>> =
        safeApiCall { api.list() }
            .map { dtos ->
                dtos.map {
                    Favorite(
                        id = it.id,
                        propertyId = it.propertyId,
                        slug = it.property.slug,
                        title = it.property.title,
                        savedAtMillis = it.createdAt.isoToEpochMillis(),
                    )
                }
            }
            .also { result -> if (result is AppResult.Success) _favoriteIds.value = result.data.map { it.propertyId }.toSet() }

    override suspend fun toggle(propertyId: String): AppResult<Boolean> {
        if (session.state.value !is SessionState.SignedIn) {
            return AppResult.Failure(AppError.Unauthorized())
        }
        return safeApiCall { api.toggle(ToggleRequest(propertyId)) }
            .map { it.active }
            .also { result ->
                if (result is AppResult.Success) {
                    _favoriteIds.value = if (result.data) _favoriteIds.value + propertyId else _favoriteIds.value - propertyId
                }
            }
    }

    private suspend fun refreshIds() {
        list()
    }
}
