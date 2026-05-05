package com.commu.luklan

import cocoapods.FirebaseCore.*
import cocoapods.FirebaseMessaging.*
import kotlinx.cinterop.ExperimentalForeignApi
import androidx.compose.runtime.*
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import com.commu.luklan.data.getAuthRepository
import kotlinx.coroutines.*
import platform.UserNotifications.*

object DeepLinkManager {
    var medicineId by mutableStateOf<String?>(null)
    var time by mutableStateOf<String?>(null)
}

@OptIn(ExperimentalForeignApi::class)
fun onDidFinishLaunchingWithOptions() {
    println("KMP Initializer: Starting setup...")
    
    // Request notification permissions for all users (e.g., caretakers who only receive pushes)
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge or UNAuthorizationOptionTimeSensitive
    ) { granted, error ->
        if (granted) {
            println("Notification permission granted.")
            // Register for remote notifications on main thread
            CoroutineScope(Dispatchers.Main).launch {
                UIApplication.sharedApplication.registerForRemoteNotifications()
            }
        } else {
            println("Notification permission denied or error: $error")
        }
    }
    
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

fun onNewTokenReceived(token: String) {
    val authRepository = getAuthRepository()
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    authRepository.getCurrentUserId()?.let { uid ->
        scope.launch {
            authRepository.updateFcmToken(uid, token)
        }
    }
}

fun onDeepLinkReceived(medicineId: String, time: String?) {
    DeepLinkManager.medicineId = medicineId
    DeepLinkManager.time = time
}
