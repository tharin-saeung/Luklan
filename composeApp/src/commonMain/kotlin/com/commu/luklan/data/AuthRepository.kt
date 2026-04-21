package com.commu.luklan.data

expect class AuthRepository() {
    suspend fun signUpWithEmail(email: String, password: String, name: String, role: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    suspend fun getUserProfile(userId: String): Result<User>
    fun signOut()
}

expect fun getAuthRepository(): AuthRepository