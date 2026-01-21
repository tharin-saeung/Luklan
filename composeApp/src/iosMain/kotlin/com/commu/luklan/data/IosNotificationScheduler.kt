package com.commu.luklan.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UserNotifications.*

@OptIn(ExperimentalForeignApi::class)
class IosNotificationScheduler : NotificationScheduler {

    override fun schedule(medicine: Medicine) {
        // ขอ permission
        val center = UNUserNotificationCenter.currentNotificationCenter()

        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            completionHandler = { granted, error ->
                if (granted) {
                    scheduleLocalNotification(medicine)
                } else {
                    println("Notification permission denied")
                }
            }
        )
    }

    private fun scheduleLocalNotification(medicine: Medicine) {
        val content = UNMutableNotificationContent().apply {
            setTitle("⏰ เตือนกินยา")
            setBody("ได้เวลากินยา ${medicine.name} ${medicine.description} แล้วนะครับ")
            setSound(UNNotificationSound.defaultSound())
            setBadge(NSNumber(1))
        }

        // Parse เวลา
        val timeParts = medicine.time.split(":")
        if (timeParts.size == 2) {
            val hour = timeParts[0].toLongOrNull() ?: 0L
            val minute = timeParts[1].toLongOrNull() ?: 0L

            val dateComponents = NSDateComponents().apply {
                setHour(hour)
                setMinute(minute)
            }

            // สร้าง trigger ที่ repeat ทุกวัน
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                dateComponents = dateComponents,
                repeats = true
            )

            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = medicine.id,
                content = content,
                trigger = trigger
            )

            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("❌ Error scheduling notification: ${error.localizedDescription}")
                } else {
                    println("✅ Notification scheduled for ${medicine.name} at ${medicine.time}")
                }
            }
        }
    }

    override fun cancel(medicine: Medicine) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(medicine.id))
        println("🗑️ Cancelled notification for ${medicine.name}")
    }
}
