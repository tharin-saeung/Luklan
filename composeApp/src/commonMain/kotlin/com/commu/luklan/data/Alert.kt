package com.commu.luklan.data

data class Alert(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: String = "SOS", // "SOS", "MISSED_MED"
    val message: String = "",
    val timestamp: Long = 0L,
    val groupIds: List<String> = emptyList()
)
