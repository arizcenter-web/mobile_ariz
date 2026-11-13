package com.tecsup.app_ariz.feature.favorites

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.favorites.domain.Favorite
import com.tecsup.app_ariz.core.favorites.domain.FavoritesRepository
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.feature.favorites.domain.usecase.GetFavoritesUseCase
import com.tecsup.app_ariz.feature.favorites.domain.usecase.ObserveFavoriteIdsUseCase
import com.tecsup.app_ariz.feature.favorites.domain.usecase.ToggleFavoriteUseCase
import com.tecsup.app_ariz.feature.favorites.ui.FavoritesAction
import com.tecsup.app_ariz.feature.favorites.ui.FavoritesListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesListViewModelTest {

    private val user = User("u1", "Ana", "ana@correo.com", Role.USER, null)

    private fun favorite(id: String) = Favorite("f-$id", id, "slug-$id", "Título $id", null)

    private class FakeSession(initial: SessionState) : SessionManager {
        private val _state = MutableStateFlow(initial)
        override val state: StateFlow<SessionState> = _state
        fun set(state: SessionState) { _state.value = state }
        override suspend fun restore() = Unit
        override fun onSignedIn(user: User) = Unit
        override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
        override fun onSessionExpired() = Unit
    }

    private class FakeFavorites(initial: List<Favorite>) : FavoritesRepository {
        var listResult: AppResult<List<Favorite>> = AppResult.Success(initial)
        private val _ids = MutableStateFlow(initial.map { it.propertyId }.toSet())
        override val favoriteIds: StateFlow<Set<String>> = _ids
        override suspend fun list() = listResult
        override suspend fun toggle(propertyId: String): AppResult<Boolean> {
            val active = propertyId !in _ids.value
            _ids.value = if (active) _ids.value + propertyId else _ids.value - propertyId
            return AppResult.Success(active)
        }
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(session: FakeSession, repo: FakeFavorites) = FavoritesListViewModel(
        session, GetFavoritesUseCase(repo), ObserveFavoriteIdsUseCase(repo), ToggleFavoriteUseCase(repo),
    )

    @Test
    fun `sin sesion no carga la lista`() {
        val viewModel = viewModel(FakeSession(SessionState.SignedOut), FakeFavorites(listOf(favorite("p1"))))

        assertEquals(SessionState.SignedOut, viewModel.state.value.session)
        assertTrue(viewModel.state.value.items.isEmpty())
        assertFalse(viewModel.state.value.hasLoaded)
    }

    @Test
    fun `con sesion carga la lista y al quitar una desaparece sin recargar`() {
        val viewModel = viewModel(FakeSession(SessionState.SignedIn(user)), FakeFavorites(listOf(favorite("p1"), favorite("p2"))))
        assertEquals(listOf("p1", "p2"), viewModel.state.value.visibleItems.map { it.propertyId })

        viewModel.onAction(FavoritesAction.Remove("p1"))

        assertEquals(listOf("p2"), viewModel.state.value.visibleItems.map { it.propertyId })
    }

    @Test
    fun `iniciar y cerrar sesion carga y vacia la lista`() {
        val session = FakeSession(SessionState.SignedOut)
        val viewModel = viewModel(session, FakeFavorites(listOf(favorite("p1"))))

        session.set(SessionState.SignedIn(user))
        assertEquals(1, viewModel.state.value.visibleItems.size)

        session.set(SessionState.SignedOut)
        assertTrue(viewModel.state.value.items.isEmpty())
        assertFalse(viewModel.state.value.hasLoaded)
    }

    @Test
    fun `un error al cargar se muestra y reintentar lo recupera`() {
        val repo = FakeFavorites(listOf(favorite("p1")))
        repo.listResult = AppResult.Failure(AppError.Network())
        val viewModel = viewModel(FakeSession(SessionState.SignedIn(user)), repo)
        assertNotNull(viewModel.state.value.error)

        repo.listResult = AppResult.Success(listOf(favorite("p1")))
        viewModel.onAction(FavoritesAction.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.visibleItems.size)
    }
}
