package com.commu.luklan.data

interface GroupRepository {
    suspend fun createDefaultGroup(user: User): Result<CareGroup>
    suspend fun createGroup(name: String, owner: User): Result<CareGroup>
    suspend fun joinGroup(userId: String, inviteCode: String): Result<CareGroup>
    suspend fun getGroupsForUser(userId: String): Result<List<CareGroup>>
    suspend fun getGroupMembers(groupId: String): Result<List<User>>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun getGroupById(groupId: String): Result<CareGroup>
    suspend fun kickMember(groupId: String, userId: String): Result<Unit>
    suspend fun transferOwnership(groupId: String, newOwnerId: String): Result<Unit>
    suspend fun updateGroupPhoto(groupId: String, photoUrl: String): Result<Unit>
    suspend fun updateGroupName(groupId: String, name: String): Result<Unit>
}

expect fun getGroupRepository(): GroupRepository
