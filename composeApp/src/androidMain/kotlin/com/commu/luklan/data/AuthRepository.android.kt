package com.commu.luklan.data

import android.util.Log
import com.commu.luklan.LuklanApplication
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.delay

actual class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    actual suspend fun signUpWithEmail(email: String, password: String, name: String, role: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "Starting signup for email: $email with role: $role")
            
            // Create authentication account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("ไม่สามารถสร้างบัญชีได้")
            
            Log.d("AuthRepository", "Auth account created with UID: $userId")
            
            // Save user data to Firestore with a small retry loop to tolerate transient errors.
            val userData = hashMapOf(
                "email" to email,
                "name" to name,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "role" to role,
                "groupIds" to emptyList<String>(),
                "caretakers" to emptyList<String>(),
                "patients" to emptyList<String>()
            )

            var saved = false
            var lastError: Exception? = null
            val maxAttempts = 3
            for (attempt in 1..maxAttempts) {
                try {
                    firestore.collection("users")
                        .document(userId)
                        .set(userData)
                        .await()
                    
                    Log.d("AuthRepository", "User data saved to Firestore successfully (attempt $attempt)")
                    saved = true
                    break
                } catch (e: Exception) {
                    lastError = e
                    Log.w("AuthRepository", "Failed to save user data to Firestore (attempt $attempt): ${e.message}")
                    val msg = e.message ?: ""
                    if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("permission denied", ignoreCase = true)) {
                        break
                    }
                    if (attempt < maxAttempts) delay(500L * attempt)
                }
            }

            if (!saved) {
                // Critical: if we can't save the profile, the app will break later.
                // We should delete the auth account to prevent orphaned entries and inform the user.
                auth.currentUser?.delete()?.await()
                throw lastError ?: Exception("ไม่สามารถบันทึกข้อมูลโปรไฟล์ได้ กรุณาลองใหม่อีกครั้ง")
            }

            Result.success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("AuthRepository", "Email already exists: ${e.message}")
            Result.failure(Exception("อีเมลนี้ถูกใช้งานแล้ว กรุณาใช้อีเมลอื่น"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    actual suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "Attempting login for email: $email")
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "Login successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}", e)
            when {
                e.message?.contains("password") == true -> 
                    Result.failure(Exception("รหัสผ่านไม่ถูกต้อง"))
                e.message?.contains("user") == true -> 
                    Result.failure(Exception("ไม่พบบัญชีผู้ใช้นี้"))
                else -> Result.failure(Exception("เข้าสู่ระบบไม่สำเร็จ: ${e.message}"))
            }
        }
    }

    actual fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    actual fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    actual suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                Result.success(doc.toUser())
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun signOut() {
        auth.signOut()
    }

    actual suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            println("🔔 Updating FCM Token for $userId: $token")
            firestore.collection("users").document(userId)
                .update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Failed to update FCM Token: ${e.message}")
            Result.failure(e)
        }
    }

    actual suspend fun registerFcmToken(userId: String): Result<Unit> {
        return try {
            val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
            updateFcmToken(userId, token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun updateUserPhoto(userId: String, photoUrl: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("photoUrl", photoUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User {
        return User(
            id = id,
            name = getString("name") ?: "",
            email = getString("email") ?: "",
            role = getString("role") ?: "user",
            groupIds = (get("groupIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            inviteCode = getString("inviteCode") ?: "",
            caretakers = (get("caretakers") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            patients = (get("patients") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            fcmToken = getString("fcmToken") ?: "",
            photoUrl = getString("photoUrl") ?: ""
        )
    }
}

actual fun getAuthRepository(): AuthRepository = AuthRepository()