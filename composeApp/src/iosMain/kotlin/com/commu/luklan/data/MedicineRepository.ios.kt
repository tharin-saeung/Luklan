package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSArray
import platform.Foundation.NSDictionary
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class MedicineRepositoryIos : MedicineRepository {

    override suspend fun addMedicine(medicine: Medicine): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            addMedicineNative(
                id = medicine.id,
                name = medicine.name,
                description = medicine.description,
                dosage = medicine.dosage,
                time = medicine.time,
                times = medicine.times,
                frequency = medicine.frequency,
                quantity = medicine.quantity,
                unit = medicine.unit,
                expiryDate = medicine.expiryDate,
                category = medicine.category,
                storageInstructions = medicine.storageInstructions,
                notes = medicine.notes,
                userId = medicine.userId,
                taken = medicine.taken,
                createdAt = medicine.createdAt
            ) { error: String? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }
    }

    override suspend fun getMedicines(userId: String): Result<List<Medicine>> {
        return suspendCancellableCoroutine { continuation ->
            getMedicinesNative(userId) { medicines: Any?, error: String? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error)))
                } else if (medicines != null) {
                    val medicineList = mutableListOf<Medicine>()
                    val array = medicines as NSArray

                    for (i in 0 until array.count.toInt()) {
                        val dict = array.objectAtIndex(i.toULong()) as NSDictionary
                        val timesListForMedicine = run {
                            val arr = (dict.objectForKey("times") as? NSArray)
                            val list = mutableListOf<String>()
                            if (arr != null) {
                                for (j in 0 until arr.count.toInt()) {
                                    val v = arr.objectAtIndex(j.toULong()) as? String
                                    if (v != null) list.add(v)
                                }
                            }
                            list
                        }

                        val medicine = Medicine(
                            id = (dict.objectForKey("id") as? String) ?: "",
                            name = (dict.objectForKey("name") as? String) ?: "",
                            description = (dict.objectForKey("description") as? String) ?: "",
                            dosage = (dict.objectForKey("dosage") as? String) ?: "",
                            time = timesListForMedicine.firstOrNull() ?: (dict.objectForKey("time") as? String) ?: "",
                            times = timesListForMedicine,
                            frequency = (dict.objectForKey("frequency") as? String) ?: "",
                            quantity = (dict.objectForKey("quantity") as? Number)?.toInt() ?: 0,
                            unit = (dict.objectForKey("unit") as? String) ?: "เม็ด",
                            expiryDate = (dict.objectForKey("expiryDate") as? String) ?: "",
                            category = (dict.objectForKey("category") as? String) ?: "",
                            storageInstructions = (dict.objectForKey("storageInstructions") as? String) ?: "",
                            notes = (dict.objectForKey("notes") as? String) ?: "",
                            userId = (dict.objectForKey("userId") as? String) ?: "",
                            taken = (dict.objectForKey("taken") as? Boolean) ?: false,
                            createdAt = (dict.objectForKey("createdAt") as? Number)?.toLong() ?: 0L
                        )
                        medicineList.add(medicine)
                    }
                    continuation.resume(Result.success(medicineList))
                } else {
                    continuation.resume(Result.success(emptyList()))
                }
            }
        }
    }

    override suspend fun updateMedicine(medicine: Medicine): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            updateMedicineNative(
                id = medicine.id,
                name = medicine.name,
                description = medicine.description,
                dosage = medicine.dosage,
                time = medicine.time,
                times = medicine.times,
                frequency = medicine.frequency,
                quantity = medicine.quantity,
                unit = medicine.unit,
                expiryDate = medicine.expiryDate,
                category = medicine.category,
                storageInstructions = medicine.storageInstructions,
                notes = medicine.notes,
                taken = medicine.taken,
                createdAt = medicine.createdAt
            ) { error: String? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }
    }

    override suspend fun deleteMedicine(medicineId: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            deleteMedicineNative(medicineId) { error: String? ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }
    }
}

actual fun getMedicineRepository(): MedicineRepository = MedicineRepositoryIos()