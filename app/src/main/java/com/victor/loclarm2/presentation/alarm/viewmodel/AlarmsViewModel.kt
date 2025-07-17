    package com.victor.loclarm2.presentation.alarm.viewmodel


    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.victor.loclarm2.data.model.Alarm
    import com.victor.loclarm2.domain.repository.AuthRepository
    import com.victor.loclarm2.domain.usecase.alarm.GetAlarmsUseCase
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    @HiltViewModel
    class AlarmsViewModel @Inject constructor(
        private val getAlarmsUseCase: GetAlarmsUseCase,
        private val authRepository: AuthRepository
    ) : ViewModel() {

        private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
        val alarms: StateFlow<List<Alarm>> = _alarms

        init {
            viewModelScope.launch {
                val user = authRepository.getCurrentUser() ?: return@launch
                getAlarmsUseCase(user.id).fold(
                    onSuccess = { alarms ->
                        _alarms.value = alarms
                    },
                    onFailure = { }
                )
            }
        }

        fun deleteAlarm(alarmId: String) {
            viewModelScope.launch {
//                getAlarmsUseCase.deleteAlarm(alarmId)
            }
        }

        fun updateAlarm(alarm: Alarm) {
            viewModelScope.launch {
//                getAlarmsUseCase.saveAlarm(alarm)
            }
        }
    }