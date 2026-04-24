package com.commu.luklan.data

expect class AuthRepository() {
    suspend fun signUpWithEmail(email: String, password: String, name: String, role: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun signOut()
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>
    suspend fun registerFcmToken(userId: String): Result<Unit>
    }
expect fun getAuthRepository(): AuthRepository