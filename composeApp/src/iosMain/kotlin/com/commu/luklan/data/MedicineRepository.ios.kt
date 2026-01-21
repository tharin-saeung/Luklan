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
                time = medicine.time,
                userId = medicine.userId,
                taken = medicine.taken
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
                        val medicine = Medicine(
                            id = (dict.objectForKey("id") as? String) ?: "",
                            name = (dict.objectForKey("name") as? String) ?: "",
                            description = (dict.objectForKey("description") as? String) ?: "",
                            time = (dict.objectForKey("time") as? String) ?: "",
                            userId = (dict.objectForKey("userId") as? String) ?: "",
                            taken = (dict.objectForKey("taken") as? Boolean) ?: false
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
                time = medicine.time,
                taken = medicine.taken
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