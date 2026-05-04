package com.commu.luklan.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.commu.luklan.LuklanApplication
import java.util.Calendar

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("luklan_notifications", Context.MODE_PRIVATE)

    private fun trackRequestCode(code: Int) {
        val codes = prefs.getStringSet("scheduled_codes", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        codes.add(code.toString())
        prefs.edit().putStringSet("scheduled_codes", codes).apply()
    }

    private fun untrackRequestCode(code: Int) {
        val codes = prefs.getStringSet("scheduled_codes", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        codes.remove(code.toString())
        prefs.edit().putStringSet("scheduled_codes", codes).apply()
    }

    override fun schedule(medicine: Medicine) {
        // Clear existing alarms for this medicine first
        cancel(medicine)

        val amount = medicine.currentAmount.toDoubleOrNull() ?: 0.0
        val dose = medicine.dosage.toDoubleOrNull() ?: 0.0
        if (amount < dose) {
            println("🚫 Skipping notifications for ${medicine.name} (Out of stock)")
            return
        }

        val timesToSchedule = medicine.times
        println("🔔 Scheduling notifications for: ${medicine.name} at $timesToSchedule")
        
        timesToSchedule.forEachIndexed { index, timeStr ->
            val timeParts = timeStr.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: return@forEachIndexed
                val minute = timeParts[1].toIntOrNull() ?: return@forEachIndexed

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
                
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("EXTRA_MESSAGE", message)
                    putExtra("EXTRA_MEDICINE_ID", medicine.id)
                    putExtra("EXTRA_MEDICINE_NAME", medicine.name)
                    putExtra("EXTRA_TIME", timeStr)
                    putExtra("EXTRA_USER_ID", medicine.userId)
                }

                // Unique request code for each dose time
                val requestCode = medicine.id.hashCode() + index

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    
                    // Adjust time based on mealTiming
                    when (medicine.mealTiming) {
                        "ก่อนอาหาร" -> add(Calendar.MINUTE, -medicine.mealTimingMinutes)
                        "หลังอาหาร" -> add(Calendar.MINUTE, medicine.mealTimingMinutes)
                        "พร้อมอาหาร" -> { /* 0 minutes shift */ }
                        "ก่อนนอน" -> { /* No shift unless specified */ }
                    }
                }

                // If the time has already passed today, schedule for tomorrow
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                // Check if already taken for the scheduled date
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val targetDateStr = sdf.format(calendar.time)
                val expectedKey = "${targetDateStr}_$timeStr"
                if (medicine.takenHistory.containsKey(expectedKey)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val currentUserId = getAuthRepository().getCurrentUserId()
                val isOwner = medicine.userId == currentUserId
                val patientName = AppCache.userProfileCache[medicine.userId]?.name ?: "ผู้ป่วย"

                try {
                    if (isOwner) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                        trackRequestCode(requestCode)
                    }
                    
                    // Schedule Forgot reminders (Check-ins)
                    for (i in 1..medicine.forgotTimes) {
                        // Caretakers only get the LAST reminder as a watchdog to avoid noise
                        if (!isOwner && i < medicine.forgotTimes) continue

                        val checkinMessage = if (isOwner) {
                            "คุณยังไม่ได้บันทึกการใช้ยา ${medicine.name} เลยนะครับ"
                        } else {
                            "$patientName ยังไม่ได้บันทึกการใช้ยา ${medicine.name} เลยนะครับ"
                        }

                        val checkinIntent = Intent(context, NotificationReceiver::class.java).apply {
                            putExtra("EXTRA_MESSAGE", checkinMessage)
                            putExtra("EXTRA_MEDICINE_ID", medicine.id)
                            putExtra("EXTRA_MEDICINE_NAME", medicine.name)
                            putExtra("EXTRA_TIME", timeStr)
                            putExtra("EXTRA_USER_ID", medicine.userId)
                            putExtra("EXTRA_IS_CHECKIN", true)
                            putExtra("EXTRA_IS_WATCHDOG", !isOwner)
                        }
                        val checkinReq = requestCode + (1000 * i)
                        val checkinPendingIntent = PendingIntent.getBroadcast(
                            context,
                            checkinReq,
                            checkinIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        
                        val checkinTime = calendar.timeInMillis + (medicine.forgotDurationMinutes * i * 60 * 1000L)
                        if (checkinTime > System.currentTimeMillis()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    checkinTime,
                                    checkinPendingIntent
                                )
                            } else {
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    checkinTime,
                                    checkinPendingIntent
                                )
                            }
                            trackRequestCode(checkinReq)
                        }
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun cancelSlot(medicine: Medicine, index: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val requestCode = medicine.id.hashCode() + index
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            untrackRequestCode(requestCode)
        }
        
        // Cancel all possible forgot reminders for this slot
        for (i in 1..20) {
            val checkinReq = requestCode + (1000 * i)
            val checkinPI = PendingIntent.getBroadcast(
                context,
                checkinReq,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (checkinPI != null) {
                alarmManager.cancel(checkinPI)
                checkinPI.cancel()
                untrackRequestCode(checkinReq)
            }
        }
    }

    override fun cancel(medicine: Medicine) {
        val intent = Intent(context, NotificationReceiver::class.java)
        // Try to cancel up to 10 slots to be safe
        for (index in 0 until 10) {
            cancelSlot(medicine, index)
        }
        println("✅ Cancelled notifications for ${medicine.name} (ID: ${medicine.id})")
    }

    override fun cancelAll() {
        val codes = prefs.getStringSet("scheduled_codes", emptySet()) ?: emptySet()
        val intent = Intent(context, NotificationReceiver::class.java)
        
        codes.forEach { codeStr ->
            val code = codeStr.toIntOrNull() ?: return@forEach
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        
        prefs.edit().remove("scheduled_codes").apply()
        println("✅ Cancelled all tracked Android notifications")
    }

    override fun showImmediateNotification(title: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val intent = Intent(context, com.commu.luklan.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = androidx.core.app.NotificationCompat.Builder(context, "medicine_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

actual fun getNotificationScheduler(): NotificationScheduler = AndroidNotificationScheduler(LuklanApplication.getAppContext())
