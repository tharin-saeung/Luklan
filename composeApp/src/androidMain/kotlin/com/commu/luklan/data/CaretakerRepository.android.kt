package com.commu.luklan.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class CaretakerRepositoryAndroid : CaretakerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val inviteCodesCollection = db.collection("invite_codes")

    override suspend fun getInviteCode(userId: String): Result<String> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (!doc.exists()) return generateInviteCode(userId)
            val code = doc.getString("inviteCode")
            if (code != null && code.isNotEmpty()) Result.success(code) else generateInviteCode(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateInviteCode(userId: String): Result<String> {
        return try {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            var newCode: String
            var exists: Boolean
            
            // Loop until unique code found
            do {
                newCode = (1..6).map { chars.random() }.joinToString("")
                val doc = inviteCodesCollection.document(newCode).get().await()
                exists = doc.exists()
            } while (exists)

            inviteCodesCollection.document(newCode).set(mapOf("userId" to userId, "createdAt" to com.google.firebase.Timestamp.now())).await()
            usersCollection.document(userId).set(mapOf("inviteCode" to newCode), SetOptions.merge()).await()
            Result.success(newCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun connectToPatient(caretakerId: String, inviteCode: String): Result<Unit> {
        return try {
            val codeDoc = inviteCodesCollection.document(inviteCode).get().await()
            if (!codeDoc.exists()) return Result.failure(Exception("ไม่พบรหัสเชิญนี้"))
            
            val patientId = codeDoc.getString("userId") ?: return Result.failure(Exception("รหัสไม่ถูกต้อง"))
            if (patientId == caretakerId) return Result.failure(Exception("คุณไม่สามารถเป็นผู้ดูแลตัวเองได้"))

            // Get roles to ensure correct list mapping
            val caretakerDoc = usersCollection.document(caretakerId).get().await()
            val patientDoc = usersCollection.document(patientId).get().await()
            
            val caretakerRole = caretakerDoc.getString("role") ?: "user"
            val patientRole = patientDoc.getString("role") ?: "user"

            // Logic: The "Code Owner" (patientId) is usually the patient. The "Code Enterer" (caretakerId) is usually the caretaker.
            // But we check roles to be sure.
            
            if (caretakerRole == "caretaker") {
                // Code enterer is caretaker -> Add patient to their 'patients' list
                usersCollection.document(caretakerId).set(mapOf("patients" to FieldValue.arrayUnion(patientId)), SetOptions.merge()).await()
                // Code owner should get caretaker in their 'caretakers' list
                usersCollection.document(patientId).set(mapOf("caretakers" to FieldValue.arrayUnion(caretakerId)), SetOptions.merge()).await()
            } else {
                // Code enterer is patient (or user) -> Swap
                usersCollection.document(caretakerId).set(mapOf("caretakers" to FieldValue.arrayUnion(patientId)), SetOptions.merge()).await()
                usersCollection.document(patientId).set(mapOf("patients" to FieldValue.arrayUnion(caretakerId)), SetOptions.merge()).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCaretakers(userId: String): Result<List<User>> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            val ids = (doc.get("caretakers") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val list = ids.mapNotNull { id -> usersCollection.document(id).get().await().takeIf { it.exists() }?.toUser() }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getPatients(userId: String): Result<List<User>> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            val ids = (doc.get("patients") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val list = ids.mapNotNull { id -> usersCollection.document(id).get().await().takeIf { it.exists() }?.toUser() }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toUser() = User(
        id = id,
        name = getString("name") ?: "ไม่ระบุชื่อ",
        email = getString("email") ?: "",
        role = getString("role") ?: "user",
        inviteCode = getString("inviteCode") ?: "",
        caretakers = (get("caretakers") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        patients = (get("patients") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )
}

actual fun getCaretakerRepository(): CaretakerRepository = CaretakerRepositoryAndroid()
