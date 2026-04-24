package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalForeignApi::class, ExperimentalUuidApi::class)
class AlertRepositoryIos : AlertRepository {

    override suspend fun sendAlert(alert: Alert): Result<Unit> = suspendCoroutine { continuation ->
        sendAlertNative(
            id = alert.id.ifEmpty { Uuid.random().toString() },
            senderId = alert.senderId,
            senderName = alert.senderName,
            type = alert.type,
            message = alert.message,
            timestamp = alert.timestamp,
            groupIds = alert.groupIds
        ) { error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }

    override suspend fun getAlertsForUser(userId: String): Result<List<Alert>> = suspendCoroutine { continuation ->
        getAlertsNative(userId) { alerts, error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val list = alerts?.map { it.toAlert() } ?: emptyList()
                continuation.resume(Result.success(list.sortedByDescending { it.timestamp }))
            }
        }
    }

    private fun NSDictionary.toAlert(): Alert {
        return Alert(
            id = (objectForKey("id") as? String) ?: "",
            senderId = (objectForKey("senderId") as? String) ?: "",
            senderName = (objectForKey("senderName") as? String) ?: "",
            type = (objectForKey("type") as? String) ?: "SOS",
            message = (objectForKey("message") as? String) ?: "",
            timestamp = (objectForKey("timestamp") as? NSNumber)?.longLongValue ?: 0L,
            groupIds = (objectForKey("groupIds") as? NSArray)?.let { arr ->
                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
            } ?: emptyList()
        )
    }
}

actual fun getAlertRepository(): AlertRepository = AlertRepositoryIos()
