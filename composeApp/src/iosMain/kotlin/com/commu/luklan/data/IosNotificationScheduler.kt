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
        val center = UNUserNotificationCenter.currentNotificationCenter()
        
        // Use completion handler to ensure we cancel existing ones BEFORE scheduling new ones
        // This prevents the race condition where cancel() might delete the newly scheduled ones
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            val idsToRemove = (requests as? List<UNNotificationRequest>)?.filter { 
                it.identifier.startsWith(medicine.id) 
            }?.map { it.identifier } ?: emptyList()
            
            if (idsToRemove.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(idsToRemove)
            }

            val amount = medicine.currentAmount.toDoubleOrNull() ?: 0.0
            val dose = medicine.dosage.toDoubleOrNull() ?: 0.0
            if (amount < dose) return@getPendingNotificationRequestsWithCompletionHandler

            medicine.times.forEach { timeStr ->
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val baseHour = parts[0].toIntOrNull() ?: return@forEach
                    val baseMinute = parts[1].toIntOrNull() ?: return@forEach

                    // Use mathematical calculation to avoid NSCalendar Year 1 timezone/LMT bug
                    var totalMinutes = baseHour * 60 + baseMinute
                    when (medicine.mealTiming) {
                        "ก่อนอาหาร" -> totalMinutes -= medicine.mealTimingMinutes
                        "หลังอาหาร" -> totalMinutes += medicine.mealTimingMinutes
                    }
                    
                    // Normalize to 24h range
                    val normalizedMinutes = (totalMinutes + 1440) % 1440
                    val finalHour = normalizedMinutes / 60
                    val finalMinute = normalizedMinutes % 60

                    val finalComponents = NSDateComponents().apply {
                        setHour(finalHour.toLong())
                        setMinute(finalMinute.toLong())
                    }

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
                        if (!isOwner && i < medicine.forgotTimes) continue

                        val checkinContent = UNMutableNotificationContent().apply {
                            setTitle(if (isOwner) "⏰ ลืมใช้ยาหรือเปล่าครับ?" else "⏰ ผู้ป่วยยังไม่ได้ใช้ยา")
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

                        // Mathematical shift for check-ins as well
                        val checkinTotalMinutes = totalMinutes + (medicine.forgotDurationMinutes * i)
                        val normalizedCheckinMinutes = (checkinTotalMinutes + 1440) % 1440
                        val checkinHour = normalizedCheckinMinutes / 60
                        val checkinMinute = normalizedCheckinMinutes % 60

                        val checkinComponents = NSDateComponents().apply {
                            setHour(checkinHour.toLong())
                            setMinute(checkinMinute.toLong())
                        }

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
