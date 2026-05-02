package com.commu.luklan.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.FirestoreBridge.*

@OptIn(ExperimentalForeignApi::class)
fun addMedicineNative(
    id: String,
    name: String,
    dosage: String,
    unit: String,
    times: List<String>,
    startDate: String,
    expiryDate: String,
    category: String,
    mealTiming: String,
    mealTimingMinutes: Int,
    currentAmount: String,
    userId: String,
    takenHistory: Map<String, Long>,
    createdAt: Long,
    order: Int,
    completion: (String?) -> Unit
) {
    FirestoreBridge.addMedicineWithId(
        medicineId = id,
        name = name,
        dosage = dosage,
        unit = unit,
        times = times,
        startDate = startDate,
        expiryDate = expiryDate,
        category = category,
        mealTiming = mealTiming,
        mealTimingMinutes = mealTimingMinutes,
        currentAmount = currentAmount,
        userId = userId,
        takenHistory = takenHistory as Map<Any?, *>,
        createdAt = createdAt,
        order = order,
        completion = { error: String? -> completion(error) }
    )
}

@OptIn(ExperimentalForeignApi::class)
fun listenMedicinesNative(
    userId: String,
    completion: (Any?, String?) -> Unit
): Any? {
    return FirestoreBridge.listenMedicinesWithUserId(
        userId = userId,
        completion = { medicines: Any?, error: String? ->
            completion(medicines, error)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
fun removeListenerNative(listener: Any?) {
    if (listener != null) {
        FirestoreBridge.removeListener(listener)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun getMedicinesNative(
    userId: String,
    completion: (Any?, String?) -> Unit
) {
    FirestoreBridge.getMedicinesWithUserId(
        userId = userId,
        completion = { medicines: Any?, error: String? ->
            completion(medicines, error)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
fun updateMedicineNative(
    id: String,
    name: String,
    dosage: String,
    unit: String,
    times: List<String>,
    startDate: String,
    expiryDate: String,
    category: String,
    mealTiming: String,
    mealTimingMinutes: Int,
    currentAmount: String,
    takenHistory: Map<String, Long>,
    createdAt: Long,
    order: Int,
    completion: (String?) -> Unit
) {
    FirestoreBridge.updateMedicineWithId(
        medicineId = id,
        name = name,
        dosage = dosage,
        unit = unit,
        times = times,
        startDate = startDate,
        expiryDate = expiryDate,
        category = category,
        mealTiming = mealTiming,
        mealTimingMinutes = mealTimingMinutes,
        currentAmount = currentAmount,
        takenHistory = takenHistory as Map<Any?, *>,
        createdAt = createdAt,
        order = order,
        completion = { error: String? -> completion(error) }
    )
}

@OptIn(ExperimentalForeignApi::class)
fun deleteMedicineNative(
    id: String,
    completion: (String?) -> Unit
) {
    FirestoreBridge.deleteMedicineWithId(
        medicineId = id,
        completion = { error: String? -> completion(error) }
    )
}

// Caretaker Native Bridge
@OptIn(ExperimentalForeignApi::class)
fun getInviteCodeNative(userId: String, completion: (String?, String?) -> Unit) {
    FirestoreBridge.getInviteCodeWithUserId(userId) { code, error ->
        completion(code, error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun generateInviteCodeNative(userId: String, code: String, completion: (String?) -> Unit) {
    FirestoreBridge.generateInviteCodeWithUserId(userId, code) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun connectToPatientNative(caretakerId: String, inviteCode: String, completion: (String?) -> Unit) {
    FirestoreBridge.connectToPatientWithCaretakerId(caretakerId, inviteCode) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun getUsersWithIdsNative(userIds: List<String>, completion: (Any?, String?) -> Unit) {
    FirestoreBridge.getUsersWithIds(userIds) { users, error ->
        completion(users, error)
    }
}

// CareGroup Native Bridge
@OptIn(ExperimentalForeignApi::class)
fun createGroupNative(name: String, owner: Map<String, Any>, completion: (NSDictionary?, String?) -> Unit) {
    FirestoreBridge.createGroupWithName(name, owner as Map<Any?, *>) { group, error ->
        completion(group as? NSDictionary, error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun saveUserProfileNative(userId: String, name: String, email: String, role: String, completion: (String?) -> Unit) {
    FirestoreBridge.saveUserProfileWithId(userId, name, email, role) { error ->
        completion(error)
    }
}


@OptIn(ExperimentalForeignApi::class)
fun joinGroupNative(userId: String, inviteCode: String, completion: (Any?, String?) -> Unit) {
    FirestoreBridge.joinGroupWithUserId(userId, inviteCode) { group, error ->
        completion(group, error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun getGroupsForUserNative(userId: String, completion: (Any?, String?) -> Unit) {
    FirestoreBridge.getGroupsForUserId(userId) { groups, error ->
        completion(groups, error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun getGroupMembersNative(groupId: String, completion: (Any?, String?) -> Unit) {
    FirestoreBridge.getGroupMembersWithGroupId(groupId) { members, error ->
        completion(members, error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun deleteGroupNative(groupId: String, completion: (String?) -> Unit) {
    FirestoreBridge.deleteGroupWithGroupId(groupId) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun getGroupByIdNative(groupId: String, completion: (Any?, String?) -> Unit) {
    FirestoreBridge.getGroupWithGroupId(groupId) { group, error ->
        completion(group, error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun kickMemberNative(groupId: String, userId: String, completion: (String?) -> Unit) {
    FirestoreBridge.kickMemberWithGroupId(groupId, userId) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun transferOwnershipNative(groupId: String, newOwnerId: String, completion: (String?) -> Unit) {
    FirestoreBridge.transferOwnershipWithGroupId(groupId, newOwnerId) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun updateFcmTokenNative(userId: String, token: String, completion: (String?) -> Unit) {
    FirestoreBridge.updateFcmTokenWithUserId(userId, token) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun sendAlertNative(id: String, senderId: String, senderName: String, type: String, message: String, timestamp: Long, groupIds: List<String>, completion: (String?) -> Unit) {
    FirestoreBridge.sendAlertWithId(id, senderId, senderName, type, message, timestamp, groupIds, completion)
}

@OptIn(ExperimentalForeignApi::class)
fun getAlertsNative(userId: String, completion: (List<NSDictionary>?, String?) -> Unit) {
    FirestoreBridge.getAlertsForUserId(userId) { alerts, error ->
        completion(alerts as? List<NSDictionary>, error)
    }
}


