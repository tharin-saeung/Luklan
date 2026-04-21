package com.commu.luklan.data

interface CaretakerRepository {
    suspend fun getInviteCode(userId: String): Result<String>
    suspend fun generateInviteCode(userId: String): Result<String>
    suspend fun connectToPatient(caretakerId: String, inviteCode: String): Result<Unit>
    suspend fun getCaretakers(userId: String): Result<List<User>>
    suspend fun getPatients(userId: String): Result<List<User>>
}

expect fun getCaretakerRepository(): CaretakerRepository
