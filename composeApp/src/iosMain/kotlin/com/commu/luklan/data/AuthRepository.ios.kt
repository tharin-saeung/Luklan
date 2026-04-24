package com.commu.luklan.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthDataResult
import cocoapods.FirebaseAuth.FIRUser
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual class AuthRepository actual constructor() {
    private val auth = FIRAuth.auth()

    actual suspend fun signUpWithEmail(email: String, password: String, name: String, role: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val callback: (FIRAuthDataResult?, NSError?) -> Unit = { result, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else if (result != null) {
                    val userId = result.user()?.uid()
                    if (userId != null) {
                        saveUserProfileNative(userId, name, email, role) { firestoreError ->
                            if (firestoreError != null) {
                                continuation.resume(Result.failure(Exception(firestoreError)))
                            } else {
                                continuation.resume(Result.success(Unit))
                            }
                        }
                    } else {
                        continuation.resume(Result.success(Unit))
                    }
                } else {
                    continuation.resume(Result.failure(Exception("Sign up failed")))
                }
            }
            auth.createUserWithEmail(email = email, password = password, completion = callback)
        }
    }

    actual suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val callback: (FIRAuthDataResult?, NSError?) -> Unit = { result, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
            auth.signInWithEmail(email = email, password = password, completion = callback)
        }
    }

    actual fun isUserLoggedIn(): Boolean {
        return auth.currentUser() != null
    }

    actual fun getCurrentUserId(): String? {
        return auth.currentUser()?.uid()
    }

    actual suspend fun getUserProfile(userId: String): Result<User> = suspendCoroutine { continuation ->
        getUsersWithIdsNative(listOf(userId)) { users: Any?, error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val nsArray = users as? NSArray
                if (nsArray != null && nsArray.count > 0.toULong()) {
                    val dict = nsArray.objectAtIndex(0.toULong()) as? NSDictionary
                    if (dict != null) {
                        continuation.resume(Result.success(dict.toUser()))
                    } else {
                        continuation.resume(Result.failure(Exception("User not found")))
                    }
                } else {
                    continuation.resume(Result.failure(Exception("User not found")))
                }
            }
        }
    }

    actual suspend fun signOut() {
        try {
            auth.signOut(null)
        } catch (e: Exception) {
            println("Sign out error: ${e.message}")
        }
    }

    actual suspend fun updateFcmToken(userId: String, token: String): Result<Unit> = suspendCoroutine { continuation ->
        updateFcmTokenNative(userId, token) { error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }

    private fun NSDictionary.toUser(): User {
        return User(
            id = (objectForKey("id") as? String) ?: "",
            name = (objectForKey("name") as? String) ?: "ไม่ระบุชื่อ",
            email = (objectForKey("email") as? String) ?: "",
            role = (objectForKey("role") as? String) ?: "user",
            inviteCode = (objectForKey("inviteCode") as? String) ?: "",
            caretakers = (objectForKey("caretakers") as? NSArray)?.let { arr ->
                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
            } ?: emptyList(),
            patients = (objectForKey("patients") as? NSArray)?.let { arr ->
                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
            } ?: emptyList(),
            fcmToken = (objectForKey("fcmToken") as? String) ?: ""
        )
    }
}

actual fun getAuthRepository(): AuthRepository = AuthRepository()
