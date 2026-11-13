package com.tecsup.app_ariz.core.session

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.network.SessionCookieStore
import com.tecsup.app_ariz.core.session.dto.AuthResponseDto
import com.tecsup.app_ariz.core.session.dto.AuthUserDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DefaultSessionManagerTest {

    private val userDto = AuthUserDto("u1", "Ana", "ana@correo.com", "USER", null)

    private class FakeCookies(var hasCookie: Boolean) : SessionCookieStore {
        var cleared = false
        override fun hasSession() = hasCookie
        override fun clear() {
            cleared = true
            hasCookie = false
        }
    }

    private class FakeSessionApi(
        private val onMe: () -> AuthResponseDto = { error("no se esperaba /auth/me") },
        private val onLogout: () -> Unit = {},
    ) : SessionApi {
        var meCalls = 0
        override suspend fun me(): AuthResponseDto {
            meCalls++
            return onMe()
        }

        override suspend fun logout() = onLogout()
    }

    @Test
    fun `restore sin cookie queda SignedOut sin llamar a la red`() = runTest {
        val api = FakeSessionApi()
        val manager = DefaultSessionManager(api, FakeCookies(hasCookie = false))

        manager.restore()

        assertEquals(SessionState.SignedOut, manager.state.value)
        assertEquals(0, api.meCalls)
    }

    @Test
    fun `restore con cookie valida deja SignedIn`() = runTest {
        val manager = DefaultSessionManager(
            FakeSessionApi(onMe = { AuthResponseDto(userDto) }),
            FakeCookies(hasCookie = true),
        )

        manager.restore()

        val state = manager.state.value
        assertTrue(state is SessionState.SignedIn)
        assertEquals(Role.USER, (state as SessionState.SignedIn).user.role)
    }

    @Test
    fun `restore sin red no borra la cookie`() = runTest {
        val cookies = FakeCookies(hasCookie = true)
        val manager = DefaultSessionManager(
            FakeSessionApi(onMe = { throw IOException("sin conexion") }),
            cookies,
        )

        manager.restore()

        assertEquals(SessionState.SignedOut, manager.state.value)
        assertFalse(cookies.cleared)
    }

    @Test
    fun `signOut cierra la sesion local aunque falle la red`() = runTest {
        val cookies = FakeCookies(hasCookie = true)
        val manager = DefaultSessionManager(
            FakeSessionApi(onLogout = { throw IOException("sin conexion") }),
            cookies,
        )
        manager.onSignedIn(User("u1", "Ana", "ana@correo.com", Role.USER, null))

        val result = manager.signOut()

        assertTrue(result is AppResult.Failure)
        assertTrue(cookies.cleared)
        assertEquals(SessionState.SignedOut, manager.state.value)
    }

    @Test
    fun `onSessionExpired limpia cookie y estado`() {
        val cookies = FakeCookies(hasCookie = true)
        val manager = DefaultSessionManager(FakeSessionApi(), cookies)
        manager.onSignedIn(User("u1", "Ana", "ana@correo.com", Role.USER, null))

        manager.onSessionExpired()

        assertTrue(cookies.cleared)
        assertEquals(SessionState.SignedOut, manager.state.value)
    }
}
