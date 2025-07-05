package com.victor.loclarm2.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.repository.AlarmRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlarmRepository {

    override suspend fun saveAlarm(alarm: Alarm): Result<Unit> {
        return try {
            firestore.collection("alarms").document(alarm.id).set(alarm).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlarms(userId: String): Result<List<Alarm>> {
        return try {
            val snapshot = firestore.collection("alarms")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val alarms = snapshot.toObjects(Alarm::class.java)
            Result.success(alarms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}