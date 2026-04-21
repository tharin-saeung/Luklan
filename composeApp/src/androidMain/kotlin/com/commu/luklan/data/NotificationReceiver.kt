package com.commu.luklan.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.commu.luklan.MainActivity
import com.commu.luklan.R
import com.google.firebase.firestore.FirebaseFirestore

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "ได้เวลาใช้ยาแล้วนะครับ"
        val medicineId = intent.getStringExtra("EXTRA_MEDICINE_ID")
        val time = intent.getStringExtra("EXTRA_TIME")
        val isCheckin = intent.getBooleanExtra("EXTRA_IS_CHECKIN", false)

        if (isCheckin && medicineId != null && time != null) {
            // Check Firestore before notifying
            val db = FirebaseFirestore.getInstance()
            db.collection("medicines").document(medicineId).get().addOnSuccessListener { doc ->
                val takenRecords = doc.get("takenRecords") as? Map<String, Boolean> ?: emptyMap()
                val isTaken = takenRecords[time] ?: false
                if (!isTaken) {
                    showNotification(context, "เตือนกินยา (ยังไม่ได้ทาน)", message, medicineId, time)
                }
            }
        } else {
            showNotification(context, "เตือนกินยา", message, medicineId, time)
        }
    }

    private fun showNotification(context: Context, title: String, message: String, medicineId: String?, time: String?) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentPendingIntent)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
