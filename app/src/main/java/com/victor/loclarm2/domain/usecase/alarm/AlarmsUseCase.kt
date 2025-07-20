package com.victor.loclarm2.domain.usecase.alarm

import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.repository.AlarmRepository
import javax.inject.Inject

class AlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend fun getAlarms(userId: String): Result<List<Alarm>> {
        return alarmRepository.getAlarms(userId)
    }

    suspend fun saveAlarm(alarm: Alarm): Result<Unit> {
        return alarmRepository.saveAlarm(alarm)
    }

    suspend fun deleteAlarm(alarmId: String): Result<Unit> {
        return alarmRepository.deleteAlarm(alarmId)
    }
}
