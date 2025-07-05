package com.victor.loclarm2.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.model.Location
import com.victor.loclarm2.domain.repository.AuthRepository
import com.victor.loclarm2.domain.usecase.alarm.SaveAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val saveAlarmUseCase: SaveAlarmUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        _selectedLocation.value = Location(latitude, longitude)
    }

    fun setShowBottomSheet(show: Boolean) {
        _showBottomSheet.value = show
    }

    fun saveAlarm(name: String, radius: Float) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            val location = _selectedLocation.value ?: return@launch
            val alarm = Alarm(
                id = UUID.randomUUID().toString(),
                name = name,
                latitude = location.latitude,
                longitude = location.longitude,
                radius = radius,
                userId = user.id
            )
            saveAlarmUseCase(alarm)
        }
    }
}