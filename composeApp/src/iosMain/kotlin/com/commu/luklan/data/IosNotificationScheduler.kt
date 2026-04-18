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
        val center = UNUserNotificationCenter.currentNotificationCenter()
        
        // Build notification message with dosage and unit
        val dosageDisplay = if (medicine.amountPerDose.isNotEmpty()) {
            "${medicine.amountPerDose} ${medicine.unit}".trim()
        } else if (medicine.dosage.isNotEmpty()) {
            "${medicine.dosage} ${medicine.unit}".trim()
        } else ""

        val message = buildString {
            append("ได้เวลากินยา ${medicine.name}")
            if (dosageDisplay.isNotEmpty()) {
                append(" $dosageDisplay")
            }
            append(" แล้วนะครับ")
        }
        
        val timesToSchedule = if (medicine.times.isNotEmpty()) medicine.times else listOf(medicine.time)

        timesToSchedule.forEachIndexed { index, timeStr ->
            val timeParts = timeStr.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toLongOrNull() ?: return@forEachIndexed
                val minute = timeParts[1].toLongOrNull() ?: return@forEachIndexed

                val content = UNMutableNotificationContent().apply {
                    setTitle("⏰ เตือนกินยา")
                    setBody(message)
                    setSound(UNNotificationSound.defaultSound())
                    setBadge(NSNumber(1))
                    setUserInfo(mapOf("medicineId" to medicine.id, "time" to timeStr))
                }

                val dateComponents = NSDateComponents().apply {
                    setHour(hour)
                    setMinute(minute)
                }

                // สร้าง trigger ที่ repeat ทุกวัน
                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    dateComponents = dateComponents,
                    repeats = true
                )

                // Unique ID per dose
                val identifier = "${medicine.id}_$index"

                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = identifier,
                    content = content,
                    trigger = trigger
                )

                center.addNotificationRequest(request) { error ->
                    if (error != null) {
                        println("❌ Error scheduling notification $index: ${error.localizedDescription}")
                    } else {
                        println("✅ Notification $index scheduled for ${medicine.name} at $timeStr")
                    }
                }
            }
        }
    }

    override fun cancelSlot(medicine: Medicine, index: Int) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val identifier = "${medicine.id}_$index"
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
    }

    override fun cancel(medicine: Medicine) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        // Cancel first 10 possible indices
        val identifiers = (0 until 10).map { "${medicine.id}_$it" }
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        println("✅ Cancelled notifications for ${medicine.name} (ID: ${medicine.id})")
    }
}

actual fun getNotificationScheduler(): NotificationScheduler = IosNotificationScheduler()