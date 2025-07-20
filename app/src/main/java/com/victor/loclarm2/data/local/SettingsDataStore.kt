package com.victor.loclarm2.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings_prefs")

object SettingsKeys {
    val LANGUAGE = stringPreferencesKey("language")
    val UNITS = stringPreferencesKey("units")
    val RINGTONE = stringPreferencesKey("ringtone")
    val VOLUME = floatPreferencesKey("volume")
    val VIBRATION = booleanPreferencesKey("vibration")
}

class SettingsDataStore(private val context: Context) {

    val language: Flow<String> = context.dataStore.data.map { it[SettingsKeys.LANGUAGE] ?: "English" }
    val units: Flow<String> = context.dataStore.data.map { it[SettingsKeys.UNITS] ?: "Metric" }
    val ringtone: Flow<String> = context.dataStore.data.map { it[SettingsKeys.RINGTONE] ?: "Select Ringtone" }
    val volume: Flow<Float> = context.dataStore.data.map { it[SettingsKeys.VOLUME] ?: 5f }
    val vibration: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.VIBRATION] ?: true }

    suspend fun saveSettings(language: String, units: String, ringtone: String, volume: Float, vibration: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.LANGUAGE] = language
            prefs[SettingsKeys.UNITS] = units
            prefs[SettingsKeys.RINGTONE] = ringtone
            prefs[SettingsKeys.VOLUME] = volume
            prefs[SettingsKeys.VIBRATION] = vibration
        }
    }
}
