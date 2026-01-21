package com.commu.luklan.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.FirestoreBridge.FirestoreBridge

@OptIn(ExperimentalForeignApi::class)
internal fun addMedicineNative(
    id: String,
    name: String,
    description: String,
    time: String,
    userId: String,
    taken: Boolean,
    completion: (String?) -> Unit
) {
    FirestoreBridge.addMedicineWithId(
        medicineId = id,
        name = name,
        description = description,
        time = time,
        userId = userId,
        taken = taken,
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
    time: String,
    taken: Boolean,
    completion: (String?) -> Unit
) {
    FirestoreBridge.updateMedicineWithId(
        medicineId = id,
        name = name,
        description = description,
        time = time,
        taken = taken,
        completion = { error -> completion(error) }
    )
}

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