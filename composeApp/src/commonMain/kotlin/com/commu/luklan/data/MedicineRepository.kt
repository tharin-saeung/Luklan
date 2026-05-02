    package com.commu.luklan.data

    interface MedicineRepository {
        suspend fun addMedicine(medicine: Medicine): Result<Unit>
        fun observeMedicines(userId: String): kotlinx.coroutines.flow.Flow<Result<List<Medicine>>>
        suspend fun updateMedicine(medicine: Medicine): Result<Unit>
        suspend fun deleteMedicine(medicineId: String): Result<Unit>
    }

    expect fun getMedicineRepository(): MedicineRepository
