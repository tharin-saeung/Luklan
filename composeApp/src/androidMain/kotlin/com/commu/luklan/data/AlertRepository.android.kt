package com.commu.luklan.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AlertRepositoryAndroid : AlertRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("alerts")

    override suspend fun sendAlert(alert: Alert): Result<Unit> {
        return try {
            collection.document(alert.id.ifEmpty { java.util.UUID.randomUUID().toString() })
                .set(alert.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlertsForUser(userId: String): Result<List<Alert>> {
        return try {
            // Get user to know their groups
            val userDoc = db.collection("users").document(userId).get().await()
            val groupIds = (userDoc.get("groupIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            
            if (groupIds.isEmpty()) return Result.success(emptyList())

            val snapshot = collection.whereArrayContainsAny("groupIds", groupIds).get().await()
            val list = snapshot.documents.mapNotNull { it.toAlert() }
            Result.success(list.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Alert.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "senderId" to senderId,
        "senderName" to senderName,
        "type" to type,
        "message" to message,
        "timestamp" to timestamp,
        "groupIds" to groupIds
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toAlert(): Alert? {
        return try {
            Alert(
                id = getString("id") ?: id,
                senderId = getString("senderId") ?: "",
                senderName = getString("senderName") ?: "",
                type = getString("type") ?: "SOS",
                message = getString("message") ?: "",
                timestamp = getLong("timestamp") ?: 0L,
                groupIds = (get("groupIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }
}

actual fun getAlertRepository(): AlertRepository = AlertRepositoryAndroid()
