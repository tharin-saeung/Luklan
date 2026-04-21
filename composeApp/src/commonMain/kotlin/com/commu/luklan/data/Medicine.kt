package com.commu.luklan.data

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
    val userId: String = "",
    val takenHistory: Map<String, Long> = emptyMap(), // "yyyy-MM-dd_HH:mm" -> actualTakenTimestamp
    val createdAt: Long = 0L,
    val order: Int = 0
)
