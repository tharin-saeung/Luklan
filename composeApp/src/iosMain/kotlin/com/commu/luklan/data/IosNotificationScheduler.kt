package com.commu.luklan.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UserNotifications.*
import com.commu.luklan.utils.getCurrentTimeMillis

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
        val dosageDisplay = if (medicine.dosage.isNotEmpty()) {
            "${medicine.dosage} ${medicine.unit}".trim()
        } else ""

        val message = buildString {
            append("ได้เวลาใช้ยา ${medicine.name}")
            if (dosageDisplay.isNotEmpty()) {
                append(" $dosageDisplay")
            }
            append(" แล้วนะครับ")
        }
        
        val timesToSchedule = medicine.times.ifEmpty { listOf("08:00") }
        println("🔔 iOS Scheduling notifications for: ${medicine.name} at $timesToSchedule")

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

                val calendar = NSCalendar.currentCalendar
                val components = NSDateComponents().apply {
                    setHour(hour)
                    setMinute(minute)
                }
                
                // Get absolute base date for adjustment
                val baseDate = calendar.dateFromComponents(components)
                var adjustedDate = baseDate
                
                if (baseDate != null) {
                    when (medicine.mealTiming) {
                        "ก่อนอาหาร" -> adjustedDate = calendar.dateByAddingUnit(
                            NSCalendarUnitMinute, 
                            -(medicine.mealTimingMinutes.toLong()), 
                            baseDate, 
                            0.toULong()
                        )
                        "หลังอาหาร" -> adjustedDate = calendar.dateByAddingUnit(
                            NSCalendarUnitMinute, 
                            medicine.mealTimingMinutes.toLong(), 
                            baseDate, 
                            0.toULong()
                        )
                        "พร้อมอาหาร" -> { /* 0 mins shift */ }
                    }
                }
                
                val adjustedComponents = adjustedDate?.let { 
                    calendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, it) 
                } ?: components

                // Primary Trigger
                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    dateComponents = adjustedComponents,
                    repeats = true
                )

                val identifier = "${medicine.id}_$index"
                val request = UNNotificationRequest.requestWithIdentifier(identifier, content, trigger)
                center.addNotificationRequest(request) { _ -> }
                
                // Check-in Reminder (+ 10 mins from adjusted)
                val checkinDate = adjustedDate?.let {
                    calendar.dateByAddingUnit(NSCalendarUnitMinute, 10, it, 0.toULong())
                }
                val checkinComponents = checkinDate?.let {
                    calendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, it)
                }
                
                if (checkinComponents != null) {
                    val checkinContent = UNMutableNotificationContent().apply {
                        setTitle("⏰ ยังไม่ทานยาใช่ไหม?")
                        setBody("คุณยังไม่ได้บันทึกการกินยา ${medicine.name} เลยนะครับ")
                        setSound(UNNotificationSound.defaultSound())
                        setUserInfo(mapOf("medicineId" to medicine.id, "time" to timeStr, "isCheckin" to true))
                    }
                    val checkinTrigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                        dateComponents = checkinComponents,
                        repeats = true
                    )
                    val checkinIdentifier = "${medicine.id}_${index}_checkin"
                    center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(checkinIdentifier, checkinContent, checkinTrigger)) { _ -> }
                }
            }
        }
    }

    override fun cancelSlot(medicine: Medicine, index: Int) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val identifier = "${medicine.id}_$index"
        val checkinIdentifier = "${medicine.id}_${index}_checkin"
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier, checkinIdentifier))
    }

    override fun cancel(medicine: Medicine) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val timesCount = if (medicine.times.isEmpty()) 1 else medicine.times.size
        val identifiers = mutableListOf<String>()
        for (i in 0 until timesCount) {
            identifiers.add("${medicine.id}_$i")
            identifiers.add("${medicine.id}_${i}_checkin")
        }
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        println("✅ Cancelled notifications for ${medicine.name} (ID: ${medicine.id})")
    }

    override fun cancelAll() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeAllPendingNotificationRequests()
        println("✅ Cancelled all pending notifications")
    }

    override fun showImmediateNotification(title: String, body: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
            setBadge(NSNumber(1))
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)
        val request = UNNotificationRequest.requestWithIdentifier("immediate_${getCurrentTimeMillis()}", content, trigger)
        center.addNotificationRequest(request) { _ -> }
    }
}

actual fun getNotificationScheduler(): NotificationScheduler = IosNotificationScheduler()