package com.commu.luklan.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.commu.luklan.MainActivity
import com.google.firebase.firestore.FirebaseFirestore

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "ได้เวลาใช้ยาแล้วนะครับ"
        val medicineId = intent.getStringExtra("EXTRA_MEDICINE_ID")
        val medicineName = intent.getStringExtra("EXTRA_MEDICINE_NAME") ?: "ยา"
        val userId = intent.getStringExtra("EXTRA_USER_ID")
        val time = intent.getStringExtra("EXTRA_TIME")
        val isCheckin = intent.getBooleanExtra("EXTRA_IS_CHECKIN", false)

        val db = FirebaseFirestore.getInstance()

        if (isCheckin && medicineId != null && time != null) {
            // Check if taken before notifying
            db.collection("medicines").document(medicineId).get().addOnSuccessListener { doc ->
                val takenHistory = doc.get("takenHistory") as? Map<String, Long> ?: emptyMap()
                
                // Format for history key usually "yyyy-MM-dd_HH:mm"
                // For simplicity here, we just check if any key ends with the time slot
                val isTaken = takenHistory.keys.any { it.endsWith("_$time") }
                
                if (!isTaken) {
                    showNotification(context, "เตือนกินยา (ยังไม่ได้ทาน)", message, medicineId, time)
                    if (userId != null) syncAlertToFirebase(db, userId, "CHECKIN", "ยังไม่ได้บันทึกการกินยา $medicineName ($time)")
                }
            }
        } else {
            showNotification(context, "เตือนกินยา", message, medicineId, time)
            if (userId != null) syncAlertToFirebase(db, userId, "MEDICINE", "ได้เวลาใช้ยา $medicineName ($time)")
        }
    }

    private fun syncAlertToFirebase(db: FirebaseFirestore, userId: String, type: String, message: String) {
        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val groupIds = (userDoc.get("groupIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val name = userDoc.getString("name") ?: "ผู้ป่วย"
            
            val alert = mapOf(
                "id" to java.util.UUID.randomUUID().toString(),
                "senderId" to userId,
                "senderName" to name,
                "type" to type,
                "message" to message,
                "timestamp" to System.currentTimeMillis(),
                "groupIds" to groupIds
            )
            
            db.collection("alerts").document(alert["id"] as String).set(alert)
        }
    }

    private fun showNotification(context: Context, title: String, message: String, medicineId: String?, time: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("DEEP_LINK_MEDICINE_ID", medicineId)
            putExtra("DEEP_LINK_TIME", time)
        }
        val contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "medicine_channel")
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
