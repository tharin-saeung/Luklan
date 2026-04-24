import Foundation
import UIKit
import composeApp
import FirebaseCore
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        print("AppDelegate: didFinishLaunchingWithOptions - Calling KMP init.")
        
        UNUserNotificationCenter.current().delegate = self
        
        // Call your kmp initializer kotlin code!
        KMPInitializerKt.onDidFinishLaunchingWithOptions()
        return true
    }

    // Called when a notification is delivered to a foreground app.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        print("AppDelegate: willPresentNotification - syncing to Firebase")
        FirestoreBridge.syncAlert(userInfo: userInfo)
        
        completionHandler([.banner, .list, .sound])
    }

    // Called to report a response to an actioned notification.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        let userInfo = response.notification.request.content.userInfo
        let medicineId = userInfo["medicineId"] as? String
        let time = userInfo["time"] as? String
        
        print("AppDelegate: Received deep link/interaction - medicineId: \(medicineId ?? "nil"), time: \(time ?? "nil")")
        
        // Also sync on interaction just in case willPresent missed it or was background
        FirestoreBridge.syncAlert(userInfo: userInfo)
        
        if let medId = medicineId {
            KMPInitializerKt.onDeepLinkReceived(medicineId: medId, time: time)
        }
        
        completionHandler()
    }
}
