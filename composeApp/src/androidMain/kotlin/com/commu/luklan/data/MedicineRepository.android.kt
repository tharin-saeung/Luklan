package com.commu.luklan.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class MedicineRepositoryAndroid : MedicineRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("medicines")

    override suspend fun addMedicine(medicine: Medicine): Result<Unit> {
        return try {
            collection.document(medicine.id).set(medicine.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMedicines(userId: String): kotlinx.coroutines.flow.Flow<Result<List<Medicine>>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = collection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toMedicine() }
                    trySend(Result.success(list.sortedBy { it.order }))
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun updateMedicine(medicine: Medicine): Result<Unit> {
        return try {
            collection.document(medicine.id).set(medicine.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedicine(medicineId: String): Result<Unit> {
        return try {
            collection.document(medicineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Medicine.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "dosage" to dosage,
        "unit" to unit,
        "times" to times,
        "startDate" to startDate,
        "expiryDate" to expiryDate,
        "category" to category,
        "mealTiming" to mealTiming,
        "mealTimingMinutes" to mealTimingMinutes,
        "currentAmount" to currentAmount,
        "photoUrl" to photoUrl,
        "userId" to userId,
        "takenHistory" to takenHistory,
        "forgotTimes" to forgotTimes,
        "forgotDurationMinutes" to forgotDurationMinutes,
        "createdAt" to createdAt,
        "order" to order
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toMedicine(): Medicine? {
        return try {
            Medicine(
                id = id,
                name = getString("name") ?: "",
                dosage = getString("dosage") ?: "",
                unit = getString("unit") ?: "เม็ด",
                times = (get("times") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                startDate = getString("startDate") ?: "",
                expiryDate = getString("expiryDate") ?: "",
                category = getString("category") ?: "เม็ด",
                mealTiming = getString("mealTiming") ?: "ก่อนอาหาร",
                mealTimingMinutes = getLong("mealTimingMinutes")?.toInt() ?: 30,
                currentAmount = getString("currentAmount") ?: "",
                photoUrl = getString("photoUrl") ?: "",
                userId = getString("userId") ?: "",
                takenHistory = (get("takenHistory") as? Map<*, *>)?.filter { it.key is String && it.value is Long } as? Map<String, Long> ?: emptyMap(),
                forgotTimes = getLong("forgotTimes")?.toInt() ?: 1,
                forgotDurationMinutes = getLong("forgotDurationMinutes")?.toInt() ?: 10,
                createdAt = getLong("createdAt") ?: 0L,
                order = getLong("order")?.toInt() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }
}

actual fun getMedicineRepository(): MedicineRepository = MedicineRepositoryAndroid()
