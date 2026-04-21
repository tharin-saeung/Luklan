package com.commu.luklan.data

interface GroupRepository {
    suspend fun createDefaultGroup(user: User): Result<CareGroup>
    suspend fun joinGroup(userId: String, inviteCode: String): Result<CareGroup>
    suspend fun getGroupsForUser(userId: String): Result<List<CareGroup>>
    suspend fun getGroupMembers(groupId: String): Result<List<User>>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun getGroupById(groupId: String): Result<CareGroup>
    suspend fun kickMember(groupId: String, userId: String): Result<Unit>
    suspend fun transferOwnership(groupId: String, newOwnerId: String): Result<Unit>
}

expect fun getGroupRepository(): GroupRepository
