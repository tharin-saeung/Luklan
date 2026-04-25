package com.commu.luklan.data

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user", // "patient", "caretaker"
    val groupIds: List<String> = emptyList(), // Groups this user belongs to
    val inviteCode: String = "", // Legacy
    val caretakers: List<String> = emptyList(), // Legacy
    val patients: List<String> = emptyList(), // Legacy
    val fcmToken: String = "",
    val photoUrl: String = ""
)
