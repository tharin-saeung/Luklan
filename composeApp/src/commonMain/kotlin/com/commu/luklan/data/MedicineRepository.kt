    package com.commu.luklan.data

    interface MedicineRepository {
        suspend fun addMedicine(medicine: Medicine): Result<Unit>
        suspend fun getMedicines(userId: String): Result<List<Medicine>>
        suspend fun updateMedicine(medicine: Medicine): Result<Unit>
        suspend fun deleteMedicine(medicineId: String): Result<Unit>  // เพิ่มบรรทัดนี้
    }

    expect fun getMedicineRepository(): MedicineRepository
