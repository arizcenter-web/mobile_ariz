package com.tecsup.app_ariz.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "ariz_preferences")

/** Preferencias no sensibles (onboarding, etc.). La sesion vive en la cookie cifrada de :core:network. */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val onboardingDone = booleanPreferencesKey("onboarding_done")

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { it[onboardingDone] ?: false }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[onboardingDone] = true }
    }
}
