package com.commu.luklan.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthDataResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class AuthRepository {
    private val auth = FIRAuth.auth()

    actual suspend fun signUpWithEmail(email: String, password: String, name: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmail(email, password) { authResult: FIRAuthDataResult?, error: NSError? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else if (authResult != null) {
                    continuation.resume(Result.success(Unit))
                } else {
                    continuation.resume(Result.failure(Exception("Sign up failed")))
                }
            }
        }
    }

    actual suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            // ✅ เรียกใช้โดยระบุ parameter name "password:" อย่างชัดเจน
            auth.signInWithEmail(
                email = email,
                password = password
            ) { authResult: FIRAuthDataResult?, error: NSError? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else if (authResult != null) {
                    continuation.resume(Result.success(Unit))
                } else {
                    continuation.resume(Result.failure(Exception("Sign in failed")))
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

    actual fun signOut() {
        try {
            auth.signOut(null)
        } catch (e: Exception) {
            println("Sign out error: ${e.message}")
        }
    }
}

actual fun getAuthRepository(): AuthRepository = AuthRepository()