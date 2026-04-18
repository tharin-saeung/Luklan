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

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        let userInfo = response.notification.request.content.userInfo
        let medicineId = userInfo["medicineId"] as? String
        let time = userInfo["time"] as? String
        
        print("AppDelegate: Received deep link - medicineId: \(medicineId ?? "nil"), time: \(time ?? "nil")")
        
        if let medId = medicineId {
            KMPInitializerKt.onDeepLinkReceived(medicineId: medId, time: time)
        }
        
        completionHandler()
    }
}
