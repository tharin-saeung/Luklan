package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MedicineRepositoryIos : MedicineRepository {

    override suspend fun addMedicine(medicine: Medicine): Result<Unit> = suspendCoroutine { continuation ->
        addMedicineNative(
            id = medicine.id,
            userId = medicine.userId,
            name = medicine.name,
            dosage = medicine.dosage,
            unit = medicine.unit,
            times = medicine.times,
            startDate = medicine.startDate,
            expiryDate = medicine.expiryDate,
            category = medicine.category,
            mealTiming = medicine.mealTiming,
            mealTimingMinutes = medicine.mealTimingMinutes,
            takenHistory = medicine.takenHistory,
            createdAt = medicine.createdAt,
            order = medicine.order,
            completion = { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getMedicines(userId: String): Result<List<Medicine>> = suspendCoroutine { continuation ->
        getMedicinesNative(userId) { medicines, error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                val list = mutableListOf<Medicine>()
                val nsArray = medicines as? NSArray
                if (nsArray != null) {
                    for (i in 0 until nsArray.count.toInt()) {
                        val dict = nsArray.objectAtIndex(i.toULong()) as? NSDictionary
                        if (dict != null) {
                            val timesList = (dict.objectForKey("times") as? NSArray)?.let { arr ->
                                (0 until arr.count.toInt()).mapNotNull { arr.objectAtIndex(it.toULong()) as? String }
                            } ?: emptyList()

                            val takenHistoryMap = (dict.objectForKey("takenHistory") as? Map<Any?, *>)?.let { d ->
                                d.entries.mapNotNull { 
                                    val k = it.key as? String
                                    val v = (it.value as? NSNumber)?.longValue
                                    if (k != null && v != null) k to v else null
                                }.toMap()
                            } ?: emptyMap()

                            val medicine = Medicine(
                                id = dict.objectForKey("id") as? String ?: "",
                                name = dict.objectForKey("name") as? String ?: "",
                                dosage = dict.objectForKey("dosage") as? String ?: "",
                                unit = dict.objectForKey("unit") as? String ?: "เม็ด",
                                times = timesList,
                                startDate = dict.objectForKey("startDate") as? String ?: "",
                                expiryDate = dict.objectForKey("expiryDate") as? String ?: "",
                                category = dict.objectForKey("category") as? String ?: "เม็ด",
                                mealTiming = dict.objectForKey("mealTiming") as? String ?: "ก่อนอาหาร",
                                mealTimingMinutes = (dict.objectForKey("mealTimingMinutes") as? NSNumber)?.intValue ?: 30,
                                userId = dict.objectForKey("userId") as? String ?: "",
                                takenHistory = takenHistoryMap,
                                createdAt = (dict.objectForKey("createdAt") as? NSNumber)?.longValue ?: 0L,
                                order = (dict.objectForKey("order") as? NSNumber)?.intValue ?: 0
                            )
                            list.add(medicine)
                        }
                    }
                }
                continuation.resume(Result.success(list.sortedBy { it.order }))
            }
        }
    }

    override suspend fun updateMedicine(medicine: Medicine): Result<Unit> = suspendCoroutine { continuation ->
        updateMedicineNative(
            id = medicine.id,
            name = medicine.name,
            dosage = medicine.dosage,
            unit = medicine.unit,
            times = medicine.times,
            startDate = medicine.startDate,
            expiryDate = medicine.expiryDate,
            category = medicine.category,
            mealTiming = medicine.mealTiming,
            mealTimingMinutes = medicine.mealTimingMinutes,
            takenHistory = medicine.takenHistory,
            createdAt = medicine.createdAt,
            order = medicine.order,
            completion = { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        )
    }

    override suspend fun deleteMedicine(medicineId: String): Result<Unit> = suspendCoroutine { continuation ->
        deleteMedicineNative(medicineId) { error ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }
}

actual fun getMedicineRepository(): MedicineRepository = MedicineRepositoryIos()
