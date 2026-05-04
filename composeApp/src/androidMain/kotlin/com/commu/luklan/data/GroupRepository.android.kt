package com.commu.luklan.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class GroupRepositoryAndroid : GroupRepository {
    private val db = FirebaseFirestore.getInstance()
    private val groupsCollection = db.collection("care_groups")
    private val usersCollection = db.collection("users")

    override suspend fun createDefaultGroup(user: User): Result<CareGroup> {
        return createGroup("กลุ่มของ ${user.name}", user)
    }

    override suspend fun createGroup(name: String, owner: User): Result<CareGroup> {
        return try {
            val inviteCode = generateUnique5DigitCode()
            val groupId = groupsCollection.document().id
            
            val group = CareGroup(
                id = groupId,
                name = name,
                patientId = if (owner.role == "patient") owner.id else "",
                ownerId = owner.id,
                inviteCode = inviteCode,
                memberIds = listOf(owner.id),
                createdAt = System.currentTimeMillis()
            )

            db.runBatch { batch ->
                batch.set(groupsCollection.document(groupId), group.toMap())
                batch.update(usersCollection.document(owner.id), "groupIds", FieldValue.arrayUnion(groupId))
            }.await()

            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinGroup(userId: String, inviteCode: String): Result<CareGroup> {
        return try {
            val snapshot = groupsCollection.whereEqualTo("inviteCode", inviteCode).get().await()
            if (snapshot.isEmpty) return Result.failure(Exception("ไม่พบกลุ่มด้วยรหัสเชิญนี้"))
            
            val doc = snapshot.documents.first()
            val group = doc.toCareGroup() ?: return Result.failure(Exception("ข้อมูลกลุ่มไม่ถูกต้อง"))
            
            if (group.memberIds.contains(userId)) {
                return Result.success(group) // Already in group
            }

            db.runBatch { batch ->
                batch.update(groupsCollection.document(group.id), "memberIds", FieldValue.arrayUnion(userId))
                batch.update(usersCollection.document(userId), "groupIds", FieldValue.arrayUnion(group.id))
            }.await()

            Result.success(group.copy(memberIds = group.memberIds + userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupsForUser(userId: String): Result<List<CareGroup>> {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            val groupIds = (userDoc.get("groupIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            
            if (groupIds.isEmpty()) return Result.success(emptyList())
            
            // Firestore 'in' query limited to 10 items. For more, we'd need to chunk or query differently.
            val snapshot = groupsCollection.whereIn("id", groupIds).get().await()
            val groups = snapshot.documents.mapNotNull { it.toCareGroup() }
            Result.success(groups.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupMembers(groupId: String): Result<List<User>> {
        return try {
            val groupDoc = groupsCollection.document(groupId).get().await()
            val memberIds = (groupDoc.get("memberIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            
            if (memberIds.isEmpty()) return Result.success(emptyList())
            
            val snapshot = usersCollection.whereIn(com.google.firebase.firestore.FieldPath.documentId(), memberIds).get().await()
            val members = snapshot.documents.map { it.toUser() }
            Result.success(members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            val groupDoc = groupsCollection.document(groupId).get().await()
            val memberIds = (groupDoc.get("memberIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            
            db.runBatch { batch ->
                memberIds.forEach { memberId ->
                    batch.update(usersCollection.document(memberId), "groupIds", FieldValue.arrayRemove(groupId))
                }
                batch.delete(groupsCollection.document(groupId))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupById(groupId: String): Result<CareGroup> {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            val group = doc.toCareGroup() ?: throw Exception("Group not found")
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun kickMember(groupId: String, userId: String): Result<Unit> {
        return try {
            db.runBatch { batch ->
                batch.update(groupsCollection.document(groupId), "memberIds", FieldValue.arrayRemove(userId))
                batch.update(usersCollection.document(userId), "groupIds", FieldValue.arrayRemove(groupId))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun transferOwnership(groupId: String, newOwnerId: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId).update("ownerId", newOwnerId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGroupPhoto(groupId: String, photoUrl: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId).update("photoUrl", photoUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGroupName(groupId: String, name: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId).update("name", name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateUnique5DigitCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        do {
            code = (1..5).map { chars.random() }.joinToString("")
            val snapshot = groupsCollection.whereEqualTo("inviteCode", code).get().await()
        } while (!snapshot.isEmpty)
        return code
    }

    private fun CareGroup.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "patientId" to patientId,
        "ownerId" to ownerId,
        "inviteCode" to inviteCode,
        "memberIds" to memberIds,
        "createdAt" to createdAt,
        "photoUrl" to photoUrl
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toCareGroup() = try {
        CareGroup(
            id = getString("id") ?: id,
            name = getString("name") ?: "",
            patientId = getString("patientId") ?: "",
            ownerId = getString("ownerId") ?: getString("patientId") ?: "", // fallback to patientId for legacy
            inviteCode = getString("inviteCode") ?: "",
            memberIds = (get("memberIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            createdAt = getLong("createdAt") ?: 0L,
            photoUrl = getString("photoUrl") ?: ""
        )
    } catch (e: Exception) { null }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser() = User(
        id = id,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        role = getString("role") ?: "user",
        groupIds = (get("groupIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        photoUrl = getString("photoUrl") ?: ""
    )
}

actual fun getGroupRepository(): GroupRepository = GroupRepositoryAndroid()
