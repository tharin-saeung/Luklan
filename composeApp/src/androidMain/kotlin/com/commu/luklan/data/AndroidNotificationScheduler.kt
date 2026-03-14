package com.commu.luklan.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(medicine: Medicine) {
        // Build notification message with dosage and unit
        val message = buildString {
            append("ได้เวลากินยา ${medicine.name}")
            if (medicine.dosage.isNotEmpty()) {
                append(" ${medicine.dosage}")
                if (medicine.unit.isNotEmpty()) {
                    append(" ${medicine.unit}")
                }
            }
            append("แล้วนะครับ")
        }
        
        val intent =
                Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("EXTRA_MESSAGE", message)
                }

        // Use medicine.id.hashCode() as a unique request code
        // Ensure medicine.id is not null or empty. If it's a string, hashCode is fine.
        val requestCode = medicine.id.hashCode()

        val pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val timeParts = medicine.time.split(":")
        if (timeParts.size == 2) {
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val calendar =
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
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
            } catch (e: SecurityException) {
                // Handle permission exception if SCHEDULE_EXACT_ALARM is not granted
                e.printStackTrace()
            }
        }
    }

    override fun cancel(medicine: Medicine) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val requestCode = medicine.id.hashCode()
        val pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        alarmManager.cancel(pendingIntent)
    }
}
