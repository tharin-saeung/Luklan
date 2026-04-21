package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class GroupRepositoryIos : GroupRepository {

    override suspend fun createDefaultGroup(user: User): Result<CareGroup> = suspendCoroutine { continuation ->
        createDefaultGroupNative(user = user.toMap()) { group: Any?, error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val dict = group as? NSDictionary
                if (dict != null) {
                    continuation.resume(Result.success(dict.toCareGroup()))
                } else {
                    continuation.resume(Result.failure(Exception("Failed to create group")))
                }
            }
        }
    }

    override suspend fun joinGroup(userId: String, inviteCode: String): Result<CareGroup> = suspendCoroutine { continuation ->
        joinGroupNative(userId = userId, inviteCode = inviteCode) { group: Any?, error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val dict = group as? NSDictionary
                if (dict != null) {
                    continuation.resume(Result.success(dict.toCareGroup()))
                } else {
                    continuation.resume(Result.failure(Exception("Failed to join group")))
                }
            }
        }
    }

    override suspend fun getGroupsForUser(userId: String): Result<List<CareGroup>> = suspendCoroutine { continuation ->
        getGroupsForUserNative(userId = userId) { groups: Any?, error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val nsArray = groups as? NSArray
                if (nsArray != null) {
                    val list = (0 until nsArray.count.toInt()).mapNotNull {
                        (nsArray.objectAtIndex(it.toULong()) as? NSDictionary)?.toCareGroup()
                    }
                    continuation.resume(Result.success(list))
                } else {
                    continuation.resume(Result.success(emptyList()))
                }
            }
        }
    }

    override suspend fun getGroupMembers(groupId: String): Result<List<User>> = suspendCoroutine { continuation ->
        getGroupMembersNative(groupId = groupId) { members: Any?, error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val nsArray = members as? NSArray
                if (nsArray != null) {
                    val list = (0 until nsArray.count.toInt()).mapNotNull {
                        (nsArray.objectAtIndex(it.toULong()) as? NSDictionary)?.toUser()
                    }
                    continuation.resume(Result.success(list))
                } else {
                    continuation.resume(Result.success(emptyList()))
                }
            }
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = suspendCoroutine { continuation ->
        deleteGroupNative(groupId = groupId) { error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }

    override suspend fun getGroupById(groupId: String): Result<CareGroup> = suspendCoroutine { continuation ->
        getGroupByIdNative(groupId = groupId) { group: Any?, error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val dict = group as? NSDictionary
                if (dict != null) {
                    continuation.resume(Result.success(dict.toCareGroup()))
                } else {
                    continuation.resume(Result.failure(Exception("Group not found")))
                }
            }
        }
    }

    override suspend fun kickMember(groupId: String, userId: String): Result<Unit> = suspendCoroutine { continuation ->
        kickMemberNative(groupId = groupId, userId = userId) { error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }

    override suspend fun transferOwnership(groupId: String, newOwnerId: String): Result<Unit> = suspendCoroutine { continuation ->
        transferOwnershipNative(groupId = groupId, newOwnerId = newOwnerId) { error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }

    private fun User.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "role" to role,
        "groupIds" to groupIds
    )

    private fun NSDictionary.toCareGroup(): CareGroup {
        return CareGroup(
            id = (objectForKey("id") as? String) ?: "",
            name = (objectForKey("name") as? String) ?: "",
            patientId = (objectForKey("patientId") as? String) ?: "",
            ownerId = (objectForKey("ownerId") as? String) ?: "",
            inviteCode = (objectForKey("inviteCode") as? String) ?: "",
            memberIds = (objectForKey("memberIds") as? NSArray)?.let { arr ->
                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
            } ?: emptyList(),
            createdAt = (objectForKey("createdAt") as? NSNumber)?.longValue ?: 0L
        )
    }

    private fun NSDictionary.toUser(): User {
        return User(
            id = (objectForKey("id") as? String) ?: "",
            name = (objectForKey("name") as? String) ?: "ไม่ระบุชื่อ",
            email = (objectForKey("email") as? String) ?: "",
            role = (objectForKey("role") as? String) ?: "user",
            groupIds = (objectForKey("groupIds") as? NSArray)?.let { arr ->
                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
            } ?: emptyList()
        )
    }
}

actual fun getGroupRepository(): GroupRepository = GroupRepositoryIos()
