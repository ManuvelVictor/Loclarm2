package com.victor.loclarm2.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victor.loclarm2.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    val language = dataStore.language.stateIn(viewModelScope, SharingStarted.Lazily, "English")
    val units = dataStore.units.stateIn(viewModelScope, SharingStarted.Lazily, "Metric")

    private val _ringtone = MutableStateFlow("Select Ringtone")
    val ringtone: StateFlow<String> = _ringtone

    private val _volume = MutableStateFlow(5f)
    val volume: StateFlow<Float> = _volume

    private val _vibration = MutableStateFlow(true)
    val vibration: StateFlow<Boolean> = _vibration

    init {
        viewModelScope.launch {
            dataStore.ringtone.collect {
                _ringtone.value = it
            }
            dataStore.volume.collect {
                _volume.value = it
            }
            dataStore.vibration.collect {
                _vibration.value = it
            }
        }
    }

    fun setRingtone(newUri: String) {
        _ringtone.value = newUri
    }

    fun setVolume(newVolume: Float) {
        _volume.value = newVolume
    }

    fun setVibration(enabled: Boolean) {
        _vibration.value = enabled
    }

    fun saveSettings(
        language: String,
        units: String,
        ringtone: String = _ringtone.value,
        volume: Float = _volume.value,
        vibration: Boolean = _vibration.value
    ) {
        viewModelScope.launch {
            dataStore.saveSettings(language, units, ringtone, volume, vibration)
        }
    }
}

