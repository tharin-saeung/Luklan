package com.commu.luklan

import cocoapods.FirebaseCore.*
import kotlinx.cinterop.ExperimentalForeignApi
import androidx.compose.runtime.*

object DeepLinkManager {
    var medicineId by mutableStateOf<String?>(null)
    var time by mutableStateOf<String?>(null)
}

@OptIn(ExperimentalForeignApi::class)
fun onDidFinishLaunchingWithOptions() {
    println("KMP Initializer: Starting setup...")
    FIRApp.configure() // Call Firebase configure
    println("KMP Initializer: Firebase project ID: ${FIRApp.defaultApp()?.options!!.projectID()}")
}

fun onDeepLinkReceived(medicineId: String, time: String?) {
    DeepLinkManager.medicineId = medicineId
    DeepLinkManager.time = time
}
