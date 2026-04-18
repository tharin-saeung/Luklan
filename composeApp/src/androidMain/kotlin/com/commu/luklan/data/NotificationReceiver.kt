package com.commu.luklan.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.commu.luklan.MainActivity
import com.commu.luklan.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "ได้เวลากินยาแล้วนะครับ"
        val medicineId = intent.getStringExtra("EXTRA_MEDICINE_ID")
        val time = intent.getStringExtra("EXTRA_TIME")
        val title = "เตือนกินยา"

        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create an intent to open the app when the notification is tapped
        val contentIntent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("DEEP_LINK_MEDICINE_ID", medicineId)
                    putExtra("DEEP_LINK_TIME", time)
                }
        val contentPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notification =
                NotificationCompat.Builder(context, "medicine_channel")
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        ) // Replace with your app icon
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentPendingIntent)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
