package com.victor.loclarm2.domain.repository

import com.victor.loclarm2.data.model.Alarm

interface AlarmRepository {
    suspend fun saveAlarm(alarm: Alarm): Result<Unit>
    suspend fun getAlarms(userId: String): Result<List<Alarm>>
}