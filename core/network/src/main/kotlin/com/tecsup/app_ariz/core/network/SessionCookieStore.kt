package com.tecsup.app_ariz.core.network

/** Vista mínima de la cookie de sesión; permite sustituirla en tests. */
interface SessionCookieStore {
    fun hasSession(): Boolean
    fun clear()
}
