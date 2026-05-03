package com.commu.luklan.data

import kotlinx.datetime.LocalDate

data class Medicine(
    val id: String = "",
    val name: String = "",
    val dosage: String = "", // e.g., "1", "2"
    val unit: String = "เม็ด", // e.g., "เม็ด", "ml"
    val times: List<String> = emptyList(), // e.g., ["08:00", "20:00"]
    val startDate: String = "",
    val expiryDate: String = "",
    val category: String = "เม็ด",
    val mealTiming: String = "ก่อนอาหาร",
    val mealTimingMinutes: Int = 30,
    val currentAmount: String = "",
    val photoUrl: String = "",
    val userId: String = "",
    val takenHistory: Map<String, Long> = emptyMap(), // "yyyy-MM-dd_HH:mm" -> actualTakenTimestamp
    val forgotTimes: Int = 1,
    val forgotDurationMinutes: Int = 10,
    val createdAt: Long = 0L,
    val order: Int = 0
) {
    fun calculateDaysRemaining(): Int {
        val amount = currentAmount.toDoubleOrNull() ?: 0.0
        val dose = dosage.toDoubleOrNull() ?: 0.0
        if (dose <= 0.0 || amount <= 0.0 || times.isEmpty()) return 0
        
        val totalDosesLeft = amount / dose
        return (totalDosesLeft / times.size).toInt()
    }

    fun isAvailableOnDate(targetDateStr: String, todayStr: String): Boolean {
        if (targetDateStr < startDate) return false
        if (targetDateStr <= todayStr) return true // Show history and today
        
        val daysRemaining = calculateDaysRemaining()
        val amount = currentAmount.toDoubleOrNull() ?: 0.0
        val dose = dosage.toDoubleOrNull() ?: 0.0
        if (dose <= 0.0 || amount < dose) return false
        
        return try {
            val today = LocalDate.parse(todayStr)
            val target = LocalDate.parse(targetDateStr)
            val diff = target.toEpochDays() - today.toEpochDays()
            diff <= daysRemaining
        } catch (e: Exception) { true }
    }
}
