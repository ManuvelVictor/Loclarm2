package com.victor.loclarm2.presentation.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.repository.AuthRepository
import com.victor.loclarm2.domain.usecase.alarm.AlarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val useCase: AlarmsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            useCase.getAlarms(user.id).onSuccess {
                _alarms.value = it
            }
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            useCase.deleteAlarm(alarmId)
            loadAlarms()
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            useCase.saveAlarm(alarm)
            loadAlarms()
        }
    }
}
