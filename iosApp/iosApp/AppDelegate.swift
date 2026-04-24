import Foundation
import UIKit
import composeApp
import FirebaseCore
import FirebaseMessaging
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

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("AppDelegate: didRegisterForRemoteNotificationsWithDeviceToken")
        Messaging.messaging().apnsToken = deviceToken
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("AppDelegate: didFailToRegisterForRemoteNotificationsWithError: \(error.localizedDescription)")
    }

    // Called when a notification is delivered to a foreground app.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        let identifier = notification.request.identifier
        print("AppDelegate: willPresentNotification - id: \(identifier)")
        FirestoreBridge.syncAlert(userInfo: userInfo, alertId: identifier)
        
        completionHandler([.banner, .list, .sound])
    }

    // Called to report a response to an actioned notification.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        let userInfo = response.notification.request.content.userInfo
        let identifier = response.notification.request.identifier
        let medicineId = userInfo["medicineId"] as? String
        let time = userInfo["time"] as? String
        
        print("AppDelegate: didReceive - id: \(identifier), med: \(medicineId ?? "nil")")
        
        FirestoreBridge.syncAlert(userInfo: userInfo, alertId: identifier)
        
        if let medId = medicineId {
            KMPInitializerKt.onDeepLinkReceived(medicineId: medId, time: time)
        }
        
        completionHandler()
    }
}
