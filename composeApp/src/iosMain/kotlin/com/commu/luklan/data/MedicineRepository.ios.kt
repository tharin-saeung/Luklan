package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MedicineRepositoryIos : MedicineRepository {

    override suspend fun addMedicine(medicine: Medicine): Result<Unit> = suspendCoroutine { continuation ->
        addMedicineNative(
            id = medicine.id,
            name = medicine.name,
            description = medicine.description,
            dosage = medicine.dosage,
            time = medicine.time,
            frequency = medicine.frequency,
            quantity = medicine.quantity,
            unit = medicine.unit,
            startDate = medicine.startDate,
            expiryDate = medicine.expiryDate,
            category = medicine.category,
            mealTiming = medicine.mealTiming,
            storageInstructions = medicine.storageInstructions,
            notes = medicine.notes,
            times = medicine.times,
            userId = medicine.userId,
            taken = medicine.taken,
            takenRecords = medicine.takenRecords,
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
                            val timesList = run {
                                val arr = dict.objectForKey("times") as? NSArray
                                val list = mutableListOf<String>()
                                if (arr != null) {
                                    for (j in 0 until arr.count.toInt()) {
                                        val v = arr.objectAtIndex(j.toULong()) as? String
                                        if (v != null) list.add(v)
                                    }
                                }
                                list
                            }

                            val takenRecordsMap = run {
                                val d = dict.objectForKey("takenRecords") as? Map<Any?, *>
                                val map = mutableMapOf<String, Boolean>()
                                if (d != null) {
                                    for ((k, v) in d) {
                                        val keyStr = k as? String
                                        val valBool = v as? Boolean
                                        if (keyStr != null && valBool != null) map[keyStr] = valBool
                                    }
                                }
                                map
                            }

                            val medicine = Medicine(
                                id = dict.objectForKey("id") as? String ?: "",
                                name = dict.objectForKey("name") as? String ?: "",
                                description = dict.objectForKey("description") as? String ?: "",
                                dosage = dict.objectForKey("dosage") as? String ?: "",
                                time = dict.objectForKey("time") as? String ?: "",
                                times = timesList,
                                frequency = dict.objectForKey("frequency") as? String ?: "",
                                timeUnit = dict.objectForKey("timeUnit") as? String ?: "วัน",
                                frequencyCount = (dict.objectForKey("frequencyCount") as? NSNumber)?.intValue ?: 1,
                                amountPerDose = dict.objectForKey("amountPerDose") as? String ?: "",
                                quantity = (dict.objectForKey("quantity") as? NSNumber)?.intValue ?: 0,
                                unit = dict.objectForKey("unit") as? String ?: "เม็ด",
                                startDate = dict.objectForKey("startDate") as? String ?: "",
                                expiryDate = dict.objectForKey("expiryDate") as? String ?: "",
                                category = dict.objectForKey("category") as? String ?: "",
                                mealTiming = dict.objectForKey("mealTiming") as? String ?: "",
                                storageInstructions = dict.objectForKey("storageInstructions") as? String ?: "",
                                notes = dict.objectForKey("notes") as? String ?: "",
                                userId = dict.objectForKey("userId") as? String ?: "",
                                taken = (dict.objectForKey("taken") as? NSNumber)?.boolValue ?: false,
                                takenRecords = takenRecordsMap,
                                createdAt = (dict.objectForKey("createdAt") as? NSNumber)?.longValue ?: 0L,
                                order = (dict.objectForKey("order") as? NSNumber)?.intValue ?: 0
                            )
                            list.add(medicine)
                        }
                    }
                }
                continuation.resume(Result.success(list.sortedWith(compareBy({ it.order }, { it.times.firstOrNull() ?: it.time }))))
            }
        }
    }

    override suspend fun updateMedicine(medicine: Medicine): Result<Unit> = suspendCoroutine { continuation ->
        updateMedicineNative(
            id = medicine.id,
            name = medicine.name,
            description = medicine.description,
            dosage = medicine.dosage,
            time = medicine.time,
            frequency = medicine.frequency,
            quantity = medicine.quantity,
            unit = medicine.unit,
            startDate = medicine.startDate,
            expiryDate = medicine.expiryDate,
            category = medicine.category,
            mealTiming = medicine.mealTiming,
            storageInstructions = medicine.storageInstructions,
            notes = medicine.notes,
            times = medicine.times,
            taken = medicine.taken,
            takenRecords = medicine.takenRecords,
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
