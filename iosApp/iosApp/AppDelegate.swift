import Foundation
import UIKit
import composeApp
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        print("AppDelegate: didFinishLaunchingWithOptions - Calling KMP init.")
        
        // Call your kmp initializer kotlin code!
        KMPInitializerKt.onDidFinishLaunchingWithOptions()
        return true
    }

    // You can add other AppDelegate methods here if needed (like push notifications)
}
