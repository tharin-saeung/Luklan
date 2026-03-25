package com.commu.luklan.data

expect class AuthRepository() {
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    fun signOut()
}

expect fun getAuthRepository(): AuthRepository