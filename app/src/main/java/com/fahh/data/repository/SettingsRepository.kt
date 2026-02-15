package com.fahh.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val VOLUME = floatPreferencesKey("volume")
        val FIRST_RUN = androidx.datastore.preferences.core.booleanPreferencesKey("first_run")
    }

    val volumeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VOLUME] ?: 1.0f
    }

    val isFirstRunFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_RUN] ?: true
    }

    suspend fun updateVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOLUME] = volume
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_RUN] = false
        }
    }
}
