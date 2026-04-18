package com.commu.luklan.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MedicineRepositoryAndroid : MedicineRepository {
    private val db = FirebaseFirestore.getInstance()
    private val medicinesCollection = db.collection("medicines")

    override suspend fun addMedicine(medicine: Medicine): Result<Unit> {
        return try {
            val medicineMap = mapOf(
                "id" to medicine.id,
                "name" to medicine.name,
                "description" to medicine.description,
                "dosage" to medicine.dosage,
                "time" to medicine.time,
                "times" to medicine.times,
                "frequency" to medicine.frequency,
                "timeUnit" to medicine.timeUnit,
                "frequencyCount" to medicine.frequencyCount,
                "amountPerDose" to medicine.amountPerDose,
                "quantity" to medicine.quantity,
                "unit" to medicine.unit,
                "startDate" to medicine.startDate,
                "expiryDate" to medicine.expiryDate,
                "category" to medicine.category,
                "mealTiming" to medicine.mealTiming,
                "storageInstructions" to medicine.storageInstructions,
                "notes" to medicine.notes,
                "userId" to medicine.userId,
                "taken" to medicine.taken,
                "takenRecords" to medicine.takenRecords,
                "createdAt" to medicine.createdAt,
                "order" to medicine.order
            )
            medicinesCollection.document(medicine.id).set(medicineMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedicines(userId: String): Result<List<Medicine>> {
        return try {
            val snapshot = medicinesCollection.whereEqualTo("userId", userId).get().await()
            val medicines = snapshot.documents.mapNotNull { doc ->
                Medicine(
                    id = doc.getString("id") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    dosage = doc.getString("dosage") ?: "",
                    time = doc.getString("time") ?: "",
                    times = (doc.get("times") as? List<*>)?.filterIsInstance<String>()
                        ?: listOfNotNull(doc.getString("time")),
                    frequency = doc.getString("frequency") ?: "",
                    timeUnit = doc.getString("timeUnit") ?: "วัน",
                    frequencyCount = (doc.getLong("frequencyCount") ?: 1).toInt(),
                    amountPerDose = doc.getString("amountPerDose") ?: "",
                    quantity = (doc.getLong("quantity") ?: 0).toInt(),
                    unit = doc.getString("unit") ?: "เม็ด",
                    startDate = doc.getString("startDate") ?: "",
                    expiryDate = doc.getString("expiryDate") ?: "",
                    category = doc.getString("category") ?: "",
                    mealTiming = doc.getString("mealTiming") ?: "",
                    storageInstructions = doc.getString("storageInstructions") ?: "",
                    notes = doc.getString("notes") ?: "",
                    userId = doc.getString("userId") ?: "",
                    taken = doc.getBoolean("taken") ?: false,
                    takenRecords = (doc.get("takenRecords") as? Map<*, *>)?.map { (k, v) -> k.toString() to (v as? Boolean ?: false) }?.toMap() ?: emptyMap(),
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    order = (doc.getLong("order") ?: 0).toInt()
                )
            }
            Result.success(medicines.sortedWith(compareBy({ it.order }, { it.times.firstOrNull() ?: it.time })))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMedicine(medicine: Medicine): Result<Unit> {
        return try {
            val medicineMap = mapOf(
                "id" to medicine.id,
                "name" to medicine.name,
                "description" to medicine.description,
                "dosage" to medicine.dosage,
                "time" to medicine.time,
                "times" to medicine.times,
                "frequency" to medicine.frequency,
                "timeUnit" to medicine.timeUnit,
                "frequencyCount" to medicine.frequencyCount,
                "amountPerDose" to medicine.amountPerDose,
                "quantity" to medicine.quantity,
                "unit" to medicine.unit,
                "startDate" to medicine.startDate,
                "expiryDate" to medicine.expiryDate,
                "category" to medicine.category,
                "mealTiming" to medicine.mealTiming,
                "storageInstructions" to medicine.storageInstructions,
                "notes" to medicine.notes,
                "userId" to medicine.userId,
                "taken" to medicine.taken,
                "takenRecords" to medicine.takenRecords,
                "createdAt" to medicine.createdAt,
                "order" to medicine.order
            )
            medicinesCollection.document(medicine.id).set(medicineMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedicine(medicineId: String): Result<Unit> {
        return try {
            medicinesCollection.document(medicineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

actual fun getMedicineRepository(): MedicineRepository = MedicineRepositoryAndroid()
