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
    expiryDate: String,
    category: String,
    storageInstructions: String,
    notes: String,
    times: List<String>,
    userId: String,
    taken: Boolean,
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
        expiryDate = expiryDate,
        category = category,
        storageInstructions = storageInstructions,
    notes = notes,
    times = times,
        userId = userId,
        taken = taken,
        createdAt = createdAt,
        completion = { error -> completion(error) }
    )
}

@OptIn(ExperimentalForeignApi::class)
internal fun getMedicinesNative(
    userId: String,
    completion: (Any?, String?) -> Unit
) {
    FirestoreBridge.getMedicinesWithUserId(
        userId = userId,
        completion = { medicines, error ->
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
    expiryDate: String,
    category: String,
    storageInstructions: String,
    notes: String,
    times: List<String>,
    taken: Boolean,
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
        expiryDate = expiryDate,
        category = category,
        storageInstructions = storageInstructions,
        notes = notes,
        times = times,
        taken = taken,
        createdAt = createdAt,
        completion = { error -> completion(error) }
    )
}

// Note: the cinterop-generated Kotlin declarations expect Kotlin collections (List) for NSArray parameters,
// so we pass `List<String>` directly. If a conversion to NSMutableArray is ever required, implement it here.

@OptIn(ExperimentalForeignApi::class)
internal fun deleteMedicineNative(
    id: String,
    completion: (String?) -> Unit
) {
    FirestoreBridge.deleteMedicineWithId(
        medicineId = id,
        completion = { error -> completion(error) }
    )
}