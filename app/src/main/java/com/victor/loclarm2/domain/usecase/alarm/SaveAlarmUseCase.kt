package com.victor.loclarm2.domain.usecase.alarm

import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.repository.AlarmRepository
import javax.inject.Inject

class SaveAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm): Result<Unit> {
        return alarmRepository.saveAlarm(alarm)
    }
}