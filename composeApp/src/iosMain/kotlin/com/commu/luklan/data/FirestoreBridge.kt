package com.commu.luklan.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.FirestoreBridge.*

@OptIn(ExperimentalForeignApi::class)
internal fun addMedicineNative(
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
        userId = userId,
        takenHistory = takenHistory as Map<Any?, *>,
        createdAt = createdAt,
        order = order,
        completion = { error: String? -> completion(error) }
    )
}

@OptIn(ExperimentalForeignApi::class)
internal fun getMedicinesNative(
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
internal fun updateMedicineNative(
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
        takenHistory = takenHistory as Map<Any?, *>,
        createdAt = createdAt,
        order = order,
        completion = { error: String? -> completion(error) }
    )
}

@OptIn(ExperimentalForeignApi::class)
internal fun deleteMedicineNative(
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
internal fun getInviteCodeNative(userId: String, completion: (String?, String?) -> Unit) {
    FirestoreBridge.getInviteCodeWithUserId(userId) { code, error ->
        completion(code, error)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun generateInviteCodeNative(userId: String, code: String, completion: (String?) -> Unit) {
    FirestoreBridge.generateInviteCodeWithUserId(userId, code) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun connectToPatientNative(caretakerId: String, inviteCode: String, completion: (String?) -> Unit) {
    FirestoreBridge.connectToPatientWithCaretakerId(caretakerId, inviteCode) { error ->
        completion(error)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun getUsersWithIdsNative(userIds: List<String>, completion: (Any?, String?) -> Unit) {
    FirestoreBridge.getUsersWithIds(userIds) { users, error ->
        completion(users, error)
    }
}
