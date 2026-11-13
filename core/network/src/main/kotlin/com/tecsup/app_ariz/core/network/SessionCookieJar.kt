package com.tecsup.app_ariz.core.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Guarda la cookie de sesion `ariz_session` (httpOnly) que emite el backend en /auth/login y la
 * reenvia en cada request. Cifrada en disco. Es la "opcion A": no requiere cambios en el backend.
 *
 * Si mas adelante el backend acepta `Authorization: Bearer`, solo se cambia esta pieza por un
 * interceptor; ninguna feature se entera.
 */
class SessionCookieJar(context: Context) : CookieJar, SessionCookieStore {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "ariz_session_cookies",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = prefs.edit()
        cookies.forEach { cookie ->
            val key = cookie.name
            if (cookie.expiresAt < System.currentTimeMillis()) editor.remove(key)
            else editor.putString(key, cookie.toString())
        }
        editor.apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        return prefs.all.values.mapNotNull { raw ->
            Cookie.parse(url, raw as String)?.takeIf { it.expiresAt > now && it.matches(url) }
        }
    }

    @Synchronized
    override fun clear() {
        prefs.edit().clear().apply()
    }

    @Synchronized
    override fun hasSession(): Boolean = prefs.all.isNotEmpty()
}
