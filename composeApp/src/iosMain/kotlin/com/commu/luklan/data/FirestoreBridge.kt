package com.commu.luklan.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.FirestoreBridge.FirestoreBridge

@OptIn(ExperimentalForeignApi::class)
internal fun addMedicineNative(
    id: String,
    name: String,
    description: String,
    dosage: String,
    time: String,
    frequency: String,
    quantity: Int,
    unit: String,
    startDate: String,
    expiryDate: String,
    category: String,
    mealTiming: String,
    storageInstructions: String,
    notes: String,
    times: List<String>,
    userId: String,
    taken: Boolean,
    takenRecords: Map<String, Boolean>,
    createdAt: Long,
    completion: (String?) -> Unit
) {
    FirestoreBridge.addMedicineWithId(
        medicineId = id,
        name = name,
        description = description,
        dosage = dosage,
        time = time,
        frequency = frequency,
        quantity = quantity.toLong(),
        unit = unit,
        startDate = startDate,
        expiryDate = expiryDate,
        category = category,
        mealTiming = mealTiming,
        storageInstructions = storageInstructions,
        notes = notes,
        times = times,
        userId = userId,
        taken = taken,
        takenRecords = takenRecords as Map<Any?, *>,
        createdAt = createdAt,
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
    description: String,
    dosage: String,
    time: String,
    frequency: String,
    quantity: Int,
    unit: String,
    startDate: String,
    expiryDate: String,
    category: String,
    mealTiming: String,
    storageInstructions: String,
    notes: String,
    times: List<String>,
    taken: Boolean,
    takenRecords: Map<String, Boolean>,
    createdAt: Long,
    completion: (String?) -> Unit
) {
    FirestoreBridge.updateMedicineWithId(
        medicineId = id,
        name = name,
        description = description,
        dosage = dosage,
        time = time,
        frequency = frequency,
        quantity = quantity.toLong(),
        unit = unit,
        startDate = startDate,
        expiryDate = expiryDate,
        category = category,
        mealTiming = mealTiming,
        storageInstructions = storageInstructions,
        notes = notes,
        times = times,
        taken = taken,
        takenRecords = takenRecords as Map<Any?, *>,
        createdAt = createdAt,
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
