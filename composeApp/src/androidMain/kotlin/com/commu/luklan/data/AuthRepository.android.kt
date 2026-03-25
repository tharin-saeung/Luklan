package com.commu.luklan.data

import android.util.Log
import com.commu.luklan.LuklanApplication
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay

actual class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    actual suspend fun signUpWithEmail(email: String, password: String, name: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "Starting signup for email: $email")
            
            // Create authentication account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("ไม่สามารถสร้างบัญชีได้")
            
            Log.d("AuthRepository", "Auth account created with UID: $userId")
            
            // Save user data to Firestore with a small retry loop to tolerate transient errors.
            val userData = hashMapOf(
                "email" to email,
                "name" to name,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "role" to "user"
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
                    // If non-transient (permission denied), don't keep retrying.
                    val msg = e.message ?: ""
                    if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("permission denied", ignoreCase = true)) {
                        Log.w("AuthRepository", "Firestore permission denied when writing user document: ${e.message}")
                        break
                    }
                    if (attempt < maxAttempts) delay(500L * attempt)
                }
            }

            if (!saved) {
                // Auth account was created but writing user profile failed. This can confuse users if we show
                // a raw error. We'll treat signup as successful (so user can sign in) but log the failure.
                Log.e("AuthRepository", "User created but Firestore write failed after $maxAttempts attempts: ${lastError?.message}")
                // Optionally you could attempt to delete the auth user here to avoid orphaned accounts.
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

    actual fun signOut() {
        auth.signOut()
    }
}

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}

actual fun getAuthRepository(): AuthRepository = AuthRepository()