package com.tecsup.app_ariz.core.favorites

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.favorites.data.remote.FavoriteDto
import com.tecsup.app_ariz.core.favorites.data.remote.FavoritePropertyDto
import com.tecsup.app_ariz.core.favorites.data.remote.FavoritesApi
import com.tecsup.app_ariz.core.favorites.data.remote.ToggleRequest
import com.tecsup.app_ariz.core.favorites.data.remote.ToggleResponse
import com.tecsup.app_ariz.core.favorites.data.repository.DefaultFavoritesRepository
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultFavoritesRepositoryTest {

    private class FakeApi : FavoritesApi {
        var stored = mutableListOf("p1", "p2")
        var listCalls = 0

        override suspend fun list(): List<FavoriteDto> {
            listCalls++
            return stored.map {
                FavoriteDto(id = "f-$it", propertyId = it, createdAt = "2023-11-14T22:13:20.000Z", property = FavoritePropertyDto(it, "slug-$it", "Título $it"))
            }
        }

        override suspend fun toggle(body: ToggleRequest): ToggleResponse {
            val active = if (body.propertyId in stored) { stored.remove(body.propertyId); false } else { stored.add(body.propertyId); true }
            return ToggleResponse(active)
        }
    }

    private class FakeSession(initial: SessionState) : SessionManager {
        private val _state = MutableStateFlow(initial)
        override val state: StateFlow<SessionState> = _state
        fun set(state: SessionState) { _state.value = state }
        override suspend fun restore() = Unit
        override fun onSignedIn(user: User) = Unit
        override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
        override fun onSessionExpired() = Unit
    }

    private val user = User("u1", "Ana", "ana@correo.com", Role.USER, null)

    private fun repo(api: FavoritesApi, session: SessionManager, scope: TestScope) =
        DefaultFavoritesRepository(api, session, scope)

    @Test
    fun `al iniciar sesion carga los ids y al cerrarla los vacia`() = runTest {
        val session = FakeSession(SessionState.SignedOut)
        val repo = repo(FakeApi(), session, TestScope(UnconfinedTestDispatcher(testScheduler)))
        assertTrue(repo.favoriteIds.value.isEmpty())

        session.set(SessionState.SignedIn(user))
        assertEquals(setOf("p1", "p2"), repo.favoriteIds.value)

        session.set(SessionState.SignedOut)
        assertTrue(repo.favoriteIds.value.isEmpty())
    }

    @Test
    fun `sin sesion guardar devuelve Unauthorized y no llama al backend`() = runTest {
        val api = FakeApi()
        val repo = repo(api, FakeSession(SessionState.SignedOut), TestScope(UnconfinedTestDispatcher(testScheduler)))

        val result = repo.toggle("p3")

        assertTrue((result as AppResult.Failure).error is AppError.Unauthorized)
        assertEquals(listOf("p1", "p2"), api.stored)
    }

    @Test
    fun `guardar y quitar actualizan los ids con la respuesta del backend`() = runTest {
        val session = FakeSession(SessionState.SignedIn(user))
        val repo = repo(FakeApi(), session, TestScope(UnconfinedTestDispatcher(testScheduler)))

        assertEquals(AppResult.Success(true), repo.toggle("p3"))
        assertEquals(setOf("p1", "p2", "p3"), repo.favoriteIds.value)

        assertEquals(AppResult.Success(false), repo.toggle("p1"))
        assertEquals(setOf("p2", "p3"), repo.favoriteIds.value)
    }

    @Test
    fun `la lista trae titulo slug y fecha de guardado`() = runTest {
        val repo = repo(FakeApi(), FakeSession(SessionState.SignedIn(user)), TestScope(UnconfinedTestDispatcher(testScheduler)))

        val favorites = (repo.list() as AppResult.Success).data

        assertEquals(listOf("slug-p1", "slug-p2"), favorites.map { it.slug })
        assertEquals("Título p1", favorites.first().title)
        assertEquals(1_700_000_000_000L, favorites.first().savedAtMillis)
    }
}
