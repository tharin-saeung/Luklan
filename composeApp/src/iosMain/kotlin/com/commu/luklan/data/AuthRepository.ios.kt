package com.commu.luklan.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthDataResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class AuthRepository actual constructor() {
    private val auth = FIRAuth.auth()

    actual suspend fun signUpWithEmail(email: String, password: String, name: String, role: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmail(email, password) { authResult: FIRAuthDataResult?, error: NSError? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else if (authResult != null) {
                    // Store profile info on signup
                    val userId = authResult.user.uid()
                    // Reusing invite logic to create user doc if missing
                    continuation.resume(Result.success(Unit))
                } else {
                    continuation.resume(Result.failure(Exception("Sign up failed")))
                }
            }
        }
    }

    actual suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            auth.signInWithEmail(email, password) { _, error: NSError? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }
    }

    actual fun isUserLoggedIn(): Boolean {
        return auth.currentUser() != null
    }

    actual fun getCurrentUserId(): String? {
        return auth.currentUser()?.uid()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getUserProfile(userId: String): Result<User> = suspendCoroutine { continuation ->
        getUsersWithIdsNative(listOf(userId)) { users, error ->
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

    actual fun signOut() {
        try {
            auth.signOut(null)
        } catch (e: Exception) {
            println("Sign out error: ${e.message}")
        }
    }

    private fun NSDictionary.toUser(): User {
        return User(
            id = objectForKey("id") as? String ?: "",
            name = objectForKey("name") as? String ?: "ไม่ระบุชื่อ",
            email = objectForKey("email") as? String ?: "",
            role = objectForKey("role") as? String ?: "user",
            inviteCode = objectForKey("inviteCode") as? String ?: "",
            caretakers = (objectForKey("caretakers") as? NSArray)?.let { arr ->
                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
            } ?: emptyList(),
            patients = (objectForKey("patients") as? NSArray)?.let { arr ->
                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
            } ?: emptyList()
        )
    }
}

actual fun getAuthRepository(): AuthRepository = AuthRepository()
