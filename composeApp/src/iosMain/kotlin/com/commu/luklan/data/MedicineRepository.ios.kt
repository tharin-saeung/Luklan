package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MedicineRepositoryIos : MedicineRepository {

    override suspend fun addMedicine(medicine: Medicine): Result<Unit> = suspendCoroutine { continuation ->
        addMedicineNative(
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
            currentAmount = medicine.currentAmount,
            photoUrl = medicine.photoUrl,
            userId = medicine.userId,
            takenHistory = medicine.takenHistory,
            forgotTimes = medicine.forgotTimes,
            forgotDurationMinutes = medicine.forgotDurationMinutes,
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
    override fun observeMedicines(userId: String): kotlinx.coroutines.flow.Flow<Result<List<Medicine>>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = listenMedicinesNative(userId) { medicines, error ->
            if (error != null) {
                trySend(Result.failure(Exception(error)))
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
                                currentAmount = dict.objectForKey("currentAmount") as? String ?: "",
                                photoUrl = dict.objectForKey("photoUrl") as? String ?: "",
                                userId = dict.objectForKey("userId") as? String ?: "",
                                takenHistory = takenHistoryMap,
                                forgotTimes = (dict.objectForKey("forgotTimes") as? NSNumber)?.intValue ?: 1,
                                forgotDurationMinutes = (dict.objectForKey("forgotDurationMinutes") as? NSNumber)?.intValue ?: 10,
                                createdAt = (dict.objectForKey("createdAt") as? NSNumber)?.longValue ?: 0L,
                                order = (dict.objectForKey("order") as? NSNumber)?.intValue ?: 0
                            )
                            list.add(medicine)
                        }
                    }
                }
                trySend(Result.success(list.sortedBy { it.order }))
            }
        }
        
        awaitClose { removeListenerNative(listener) }
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
            currentAmount = medicine.currentAmount,
            photoUrl = medicine.photoUrl,
            takenHistory = medicine.takenHistory,
            forgotTimes = medicine.forgotTimes,
            forgotDurationMinutes = medicine.forgotDurationMinutes,
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
