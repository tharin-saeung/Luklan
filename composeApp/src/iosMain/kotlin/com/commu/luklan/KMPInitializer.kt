package com.commu.luklan

import cocoapods.FirebaseCore.*
import cocoapods.FirebaseMessaging.*
import kotlinx.cinterop.ExperimentalForeignApi
import androidx.compose.runtime.*
import com.commu.luklan.data.getAuthRepository
import kotlinx.coroutines.*

object DeepLinkManager {
    var medicineId by mutableStateOf<String?>(null)
    var time by mutableStateOf<String?>(null)
}

@OptIn(ExperimentalForeignApi::class)
fun onDidFinishLaunchingWithOptions() {
    println("KMP Initializer: Starting setup...")
    FIRApp.configure()
    
    // Register for remote notifications
    platform.UIKit.UIApplication.sharedApplication.registerForRemoteNotifications()
    
    // Initial token fetch
    val authRepository = getAuthRepository()
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    FIRMessaging.messaging().tokenWithCompletion { token, error ->
        if (token != null && error == null) {
            println("FCM iOS Token: $token")
            authRepository.getCurrentUserId()?.let { uid ->
                scope.launch {
                    authRepository.updateFcmToken(uid, token)
                }
            }
        }
    }
    
    println("KMP Initializer: Firebase project ID: ${FIRApp.defaultApp()?.options!!.projectID()}")
}

fun onDeepLinkReceived(medicineId: String, time: String?) {
    DeepLinkManager.medicineId = medicineId
    DeepLinkManager.time = time
}
