package com.tecsup.app_ariz.core.network.di

import android.content.Context
import com.tecsup.app_ariz.core.network.BuildConfig
import com.tecsup.app_ariz.core.network.SessionCookieJar
import com.tecsup.app_ariz.core.network.SessionCookieStore
import com.tecsup.app_ariz.core.network.SessionEvents
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideCookieJar(@ApplicationContext context: Context): SessionCookieJar =
        SessionCookieJar(context)

    @Provides
    @Singleton
    fun provideSessionCookieStore(cookieJar: SessionCookieJar): SessionCookieStore = cookieJar

    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: SessionCookieJar, events: SessionEvents): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                // /auth/login y /auth/register devuelven 401 por credenciales; no son "sesion expirada".
                val isAuthEndpoint = chain.request().url.encodedPath.startsWith("/auth/")
                if (response.code == 401 && !isAuthEndpoint) events.notifyExpired()
                response
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()
}
