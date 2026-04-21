package com.commu.luklan.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.commu.luklan.LuklanApplication
import java.util.Calendar

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(medicine: Medicine) {
        // Clear existing alarms for this medicine first
        cancel(medicine)

        val timesToSchedule = medicine.times
        
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
                    putExtra("EXTRA_TIME", timeStr)
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
                    
                    // Adjust time based on mealTimingMinutes
                    when (medicine.mealTiming) {
                        "ก่อนอาหาร" -> add(Calendar.MINUTE, -medicine.mealTimingMinutes)
                        "หลังอาหาร" -> add(Calendar.MINUTE, medicine.mealTimingMinutes)
                    }
                }

                // If the time has already passed today, schedule for tomorrow
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    
                    // Schedule Check-in reminder (10 minutes after adjusted dose time)
                    val checkinIntent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("EXTRA_MESSAGE", "คุณยังไม่ได้บันทึกการกินยา ${medicine.name} เลยนะครับ")
                        putExtra("EXTRA_MEDICINE_ID", medicine.id)
                        putExtra("EXTRA_TIME", timeStr)
                        putExtra("EXTRA_IS_CHECKIN", true)
                    }
                    val checkinPendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode + 1000, // Offset for check-in
                        checkinIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis + (10 * 60 * 1000), // +10 mins
                        checkinPendingIntent
                    )
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
        }
        
        // Also cancel check-in
        val checkinReq = requestCode + 1000
        val checkinPI = PendingIntent.getBroadcast(
            context,
            checkinReq,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (checkinPI != null) {
            alarmManager.cancel(checkinPI)
            checkinPI.cancel()
        }
    }

    override fun cancel(medicine: Medicine) {
        val intent = Intent(context, NotificationReceiver::class.java)
        for (index in 0 until 10) {
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
            }
            
            val checkinReq = requestCode + 1000
            val checkinPI = PendingIntent.getBroadcast(
                context,
                checkinReq,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (checkinPI != null) {
                alarmManager.cancel(checkinPI)
                checkinPI.cancel()
            }
        }
    }
}

actual fun getNotificationScheduler(): NotificationScheduler = AndroidNotificationScheduler(LuklanApplication.getAppContext())