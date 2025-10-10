package com.victor.loclarm2.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

private object SettingsKeys {
    val LANGUAGE = stringPreferencesKey("language")
    val UNITS = stringPreferencesKey("units")
    val RINGTONE = stringPreferencesKey("ringtone")
    val VOLUME = floatPreferencesKey("volume")
    val VIBRATION = booleanPreferencesKey("vibration")
}

class SettingsDataStore(private val context: Context) {


    val language: Flow<String> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[SettingsKeys.LANGUAGE] ?: "English" }

    val units: Flow<String> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[SettingsKeys.UNITS] ?: "Metric" }

    val ringtone: Flow<String> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[SettingsKeys.RINGTONE] ?: "default_1" }

    val volume: Flow<Float> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[SettingsKeys.VOLUME] ?: 5f }

    val vibration: Flow<Boolean> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[SettingsKeys.VIBRATION] ?: true }


    suspend fun saveSettings(
        language: String,
        units: String,
        ringtone: String,
        volume: Float,
        vibration: Boolean
    ) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.LANGUAGE] = language
            prefs[SettingsKeys.UNITS] = units
            prefs[SettingsKeys.RINGTONE] = ringtone
            prefs[SettingsKeys.VOLUME] = volume
            prefs[SettingsKeys.VIBRATION] = vibration
        }
    }

    suspend fun setRingtone(ringtone: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.RINGTONE] = ringtone
        }
    }

    suspend fun setVolume(volume: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.VOLUME] = volume
        }
    }

    suspend fun setVibration(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.VIBRATION] = enabled
        }
    }
}
