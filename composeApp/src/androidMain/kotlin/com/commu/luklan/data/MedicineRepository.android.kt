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
                "frequency" to medicine.frequency,
                "quantity" to medicine.quantity,
                "unit" to medicine.unit,
                "expiryDate" to medicine.expiryDate,
                "category" to medicine.category,
                "storageInstructions" to medicine.storageInstructions,
                "notes" to medicine.notes,
                "userId" to medicine.userId,
                "taken" to medicine.taken,
                "createdAt" to medicine.createdAt
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
                    frequency = doc.getString("frequency") ?: "",
                    quantity = (doc.getLong("quantity") ?: 0).toInt(),
                    unit = doc.getString("unit") ?: "เม็ด",
                    expiryDate = doc.getString("expiryDate") ?: "",
                    category = doc.getString("category") ?: "",
                    storageInstructions = doc.getString("storageInstructions") ?: "",
                    notes = doc.getString("notes") ?: "",
                    userId = doc.getString("userId") ?: "",
                    taken = doc.getBoolean("taken") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
            Result.success(medicines)
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
                "frequency" to medicine.frequency,
                "quantity" to medicine.quantity,
                "unit" to medicine.unit,
                "expiryDate" to medicine.expiryDate,
                "category" to medicine.category,
                "storageInstructions" to medicine.storageInstructions,
                "notes" to medicine.notes,
                "userId" to medicine.userId,
                "taken" to medicine.taken,
                "createdAt" to medicine.createdAt
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
