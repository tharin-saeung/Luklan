package com.commu.luklan.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MedicineRepositoryAndroid : MedicineRepository {
    private val db = FirebaseFirestore.getInstance()
    private val medicinesCollection = db.collection("medicines")

    override suspend fun addMedicine(medicine: Medicine): Result<Unit> {
        return try {
            medicinesCollection.document(medicine.id).set(medicine).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedicines(userId: String): Result<List<Medicine>> {
        return try {
            val snapshot = medicinesCollection.whereEqualTo("userId", userId).get().await()
            val medicines = snapshot.toObjects(Medicine::class.java)
            Result.success(medicines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMedicine(medicine: Medicine): Result<Unit> {
        return try {
            medicinesCollection.document(medicine.id).set(medicine).await()
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
