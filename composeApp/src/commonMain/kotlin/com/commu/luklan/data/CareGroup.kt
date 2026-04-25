package com.commu.luklan.data

data class CareGroup(
    val id: String = "",
    val name: String = "",
    val patientId: String = "", // The primary patient for this group
    val ownerId: String = "", // The UID of the group owner (usually the creator/patient)
    val inviteCode: String = "", // unique 5-digit code
    val memberIds: List<String> = emptyList(), // List of all member UIDs (patient + caretakers)
    val createdAt: Long = 0L,
    val photoUrl: String = ""
)
