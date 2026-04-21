package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CaretakerRepositoryIos : CaretakerRepository {
    override suspend fun getInviteCode(userId: String): Result<String> = suspendCoroutine { continuation ->
        getInviteCodeNative(userId) { code, error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else if (code != null) {
                continuation.resume(Result.success(code))
            } else {
                // If doc exists but no code, generate one
                continuation.resume(Result.success(""))
            }
        }
    }

    override suspend fun generateInviteCode(userId: String): Result<String> = suspendCoroutine { continuation ->
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val newCode = (1..6).map { chars.random() }.joinToString("")
        
        generateInviteCodeNative(userId, newCode) { error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(newCode))
            }
        }
    }

    override suspend fun connectToPatient(caretakerId: String, inviteCode: String): Result<Unit> = suspendCoroutine { continuation ->
        connectToPatientNative(caretakerId, inviteCode) { error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getCaretakers(userId: String): Result<List<User>> = suspendCoroutine { continuation ->
        // 1. Get current user's caretakers list
        getUsersWithIdsNative(listOf(userId)) { users, error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
                return@getUsersWithIdsNative
            }
            
            val nsArray = users as? NSArray
            if (nsArray != null && nsArray.count > 0.toULong()) {
                val dict = nsArray.objectAtIndex(0.toULong()) as? NSDictionary
                val ids = (dict?.objectForKey("caretakers") as? NSArray)?.let { arr ->
                    (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
                } ?: emptyList()
                
                if (ids.isEmpty()) {
                    continuation.resume(Result.success(emptyList()))
                    return@getUsersWithIdsNative
                }
                
                // 2. Fetch those caretaker profiles
                getUsersWithIdsNative(ids) { profileList, error2 ->
                    if (error2 != null) {
                        continuation.resume(Result.failure(Exception(error2)))
                    } else {
                        val list = mutableListOf<User>()
                        val profileArray = profileList as? NSArray
                        if (profileArray != null) {
                            for (i in 0 until profileArray.count.toInt()) {
                                val d = profileArray.objectAtIndex(i.toULong()) as? NSDictionary
                                if (d != null) list.add(d.toUser())
                            }
                        }
                        continuation.resume(Result.success(list))
                    }
                }
            } else {
                continuation.resume(Result.success(emptyList()))
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getPatients(userId: String): Result<List<User>> = suspendCoroutine { continuation ->
        getUsersWithIdsNative(listOf(userId)) { users, error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
                return@getUsersWithIdsNative
            }
            
            val nsArray = users as? NSArray
            if (nsArray != null && nsArray.count > 0.toULong()) {
                val dict = nsArray.objectAtIndex(0.toULong()) as? NSDictionary
                val ids = (dict?.objectForKey("patients") as? NSArray)?.let { arr ->
                    (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
                } ?: emptyList()
                
                if (ids.isEmpty()) {
                    continuation.resume(Result.success(emptyList()))
                    return@getUsersWithIdsNative
                }
                
                getUsersWithIdsNative(ids) { profileList, error2 ->
                    if (error2 != null) {
                        continuation.resume(Result.failure(Exception(error2)))
                    } else {
                        val list = mutableListOf<User>()
                        val profileArray = profileList as? NSArray
                        if (profileArray != null) {
                            for (i in 0 until profileArray.count.toInt()) {
                                val d = profileArray.objectAtIndex(i.toULong()) as? NSDictionary
                                if (d != null) list.add(d.toUser())
                            }
                        }
                        continuation.resume(Result.success(list))
                    }
                }
            } else {
                continuation.resume(Result.success(emptyList()))
            }
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

actual fun getCaretakerRepository(): CaretakerRepository = CaretakerRepositoryIos()
