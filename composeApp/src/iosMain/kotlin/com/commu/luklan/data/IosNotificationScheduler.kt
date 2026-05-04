package com.commu.luklan.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UserNotifications.*
import com.commu.luklan.utils.getCurrentTimeMillis
import com.commu.luklan.data.AppCache
import com.commu.luklan.data.getAuthRepository

@OptIn(ExperimentalForeignApi::class)
class IosNotificationScheduler : NotificationScheduler {

    override fun schedule(medicine: Medicine) {
        cancel(medicine)

        val amount = medicine.currentAmount.toDoubleOrNull() ?: 0.0
        val dose = medicine.dosage.toDoubleOrNull() ?: 0.0
        if (amount < dose) return

        val center = UNUserNotificationCenter.currentNotificationCenter()
        val calendar = NSCalendar.currentCalendar

        medicine.times.forEach { timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toLong()
                val minute = parts[1].toLong()

                val components = NSDateComponents().apply {
                    setHour(hour)
                    setMinute(minute)
                }

                // Adjust for meal timing
                var adjustedDate = calendar.dateFromComponents(components)
                if (medicine.mealTiming == "ก่อนอาหาร") {
                    adjustedDate = adjustedDate?.let { calendar.dateByAddingUnit(NSCalendarUnitMinute, -medicine.mealTimingMinutes.toLong(), it, 0.toULong()) }
                } else if (medicine.mealTiming == "หลังอาหาร") {
                    adjustedDate = adjustedDate?.let { calendar.dateByAddingUnit(NSCalendarUnitMinute, medicine.mealTimingMinutes.toLong(), it, 0.toULong()) }
                }

                val finalComponents = adjustedDate?.let { 
                    calendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, it) 
                } ?: components

                val currentUserId = getAuthRepository().getCurrentUserId()
                val isOwner = medicine.userId == currentUserId
                val patientName = AppCache.userProfileCache[medicine.userId]?.name ?: "ผู้ป่วย"

                if (isOwner) {
                    val content = UNMutableNotificationContent().apply {
                        setTitle("⏰ ได้เวลาใช้ยาแล้ว")
                        val dosageDisplay = if (medicine.dosage.isNotEmpty()) "${medicine.dosage} ${medicine.unit}" else ""
                        setBody("ได้เวลาใช้ยา ${medicine.name} $dosageDisplay แล้วนะครับ")
                        setSound(UNNotificationSound.defaultSound())
                        setUserInfo(mapOf("medicineId" to medicine.id, "time" to timeStr, "isCheckin" to false))
                    }

                    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(finalComponents, repeats = true)
                    val request = UNNotificationRequest.requestWithIdentifier("${medicine.id}_${timeStr.replace(":", "")}", content, trigger)

                    center.addNotificationRequest(request) { error ->
                        if (error != null) println("❌ iOS Notification Error: ${error.localizedDescription}")
                    }
                }

                // Forgot Reminders (Check-ins)
                for (i in 1..medicine.forgotTimes) {
                    // Caretakers only get the LAST reminder as a watchdog to avoid noise
                    if (!isOwner && i < medicine.forgotTimes) continue

                    val checkinContent = UNMutableNotificationContent().apply {
                        setTitle(if (isOwner) "⏰ ยังไม่ได้ใช้ยาใช่ไหม?" else "⏰ ผู้ป่วยยังไม่ได้ใช้ยา")
                        setBody(if (isOwner) 
                            "คุณยังไม่ได้บันทึกการใช้ยา ${medicine.name} เลยนะครับ" 
                            else "$patientName ยังไม่ได้บันทึกการใช้ยา ${medicine.name} เลยนะครับ")
                        setSound(UNNotificationSound.defaultSound())
                        setUserInfo(mapOf(
                            "medicineId" to medicine.id, 
                            "time" to timeStr, 
                            "isCheckin" to true,
                            "isWatchdog" to !isOwner
                        ))
                    }

                    // Check-in trigger time
                    val checkinDate = adjustedDate?.let {
                        calendar.dateByAddingUnit(NSCalendarUnitMinute, (medicine.forgotDurationMinutes * i).toLong(), it, 0.toULong())
                    }
                    val checkinComponents = checkinDate?.let {
                        calendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, it)
                    }

                    if (checkinComponents != null) {
                        val checkinTrigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(checkinComponents, repeats = true)
                        val checkinRequest = UNNotificationRequest.requestWithIdentifier(
                            "${medicine.id}_${timeStr.replace(":", "")}_checkin_$i", 
                            checkinContent, 
                            checkinTrigger
                        )
                        center.addNotificationRequest(checkinRequest) { _ -> }
                    }
                }
            }
        }
    }

    override fun cancelSlot(medicine: Medicine, index: Int) {
        if (index < medicine.times.size) {
            val timeStr = medicine.times[index]
            val baseId = "${medicine.id}_${timeStr.replace(":", "")}"
            val ids = mutableListOf(baseId)
            for (i in 1..20) {
                ids.add("${baseId}_checkin_$i")
            }
            UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(ids)
        }
    }

    override fun cancel(medicine: Medicine) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            val idsToRemove = (requests as? List<UNNotificationRequest>)?.filter { 
                it.identifier.startsWith(medicine.id) 
            }?.map { it.identifier } ?: emptyList()
            
            if (idsToRemove.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(idsToRemove)
            }
        }
    }

    override fun cancelAll() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
    }

    override fun showImmediateNotification(title: String, body: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier("immediate_${getCurrentTimeMillis()}", content, trigger)
        center.addNotificationRequest(request) { _ -> }
    }
}

actual fun getNotificationScheduler(): NotificationScheduler = IosNotificationScheduler()
