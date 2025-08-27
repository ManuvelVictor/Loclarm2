package com.victor.loclarm2.presentation.settings.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victor.loclarm2.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    val language: StateFlow<String> = dataStore.language.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = "English"
    )

    val units: StateFlow<String> = dataStore.units.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = "Metric"
    )

    val ringtone: StateFlow<String> = dataStore.ringtone.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = "Select Ringtone"
    )

    val volume: StateFlow<Float> = dataStore.volume.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 5f
    )

    val vibration: StateFlow<Boolean> = dataStore.vibration.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = true
    )

    fun setRingtone(newUri: String) {
        viewModelScope.launch {
            dataStore.saveSettings(
                language = language.value,
                units = units.value,
                ringtone = newUri,
                volume = volume.value,
                vibration = vibration.value
            )
        }
    }

    fun setVolume(newVolume: Float) {
        viewModelScope.launch {
            dataStore.saveSettings(
                language = language.value,
                units = units.value,
                ringtone = ringtone.value,
                volume = newVolume,
                vibration = vibration.value
            )
        }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.saveSettings(
                language = language.value,
                units = units.value,
                ringtone = ringtone.value,
                volume = volume.value,
                vibration = enabled
            )
        }
    }

    fun saveSettings(
        language: String,
        units: String,
        ringtone: String,
        volume: Float,
        vibration: Boolean
    ) {
        viewModelScope.launch {
            dataStore.saveSettings(language, units, ringtone, volume, vibration)
        }
    }
}