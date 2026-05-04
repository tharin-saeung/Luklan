package com.commu.luklan.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.commu.luklan.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "ได้เวลาใช้ยาแล้วนะครับ"
        val medicineId = intent.getStringExtra("EXTRA_MEDICINE_ID")
        val medicineName = intent.getStringExtra("EXTRA_MEDICINE_NAME") ?: "ยา"
        val userId = intent.getStringExtra("EXTRA_USER_ID")
        val time = intent.getStringExtra("EXTRA_TIME")
        val isCheckin = intent.getBooleanExtra("EXTRA_IS_CHECKIN", false)
        val isWatchdog = intent.getBooleanExtra("EXTRA_IS_WATCHDOG", false)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()

                if (isCheckin && medicineId != null && time != null) {
                    // Check if taken before notifying user
                    val doc = db.collection("medicines").document(medicineId).get().await()
                    val takenHistory = doc.get("takenHistory") as? Map<*, *> ?: emptyMap<Any, Any>()
                    
                    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val expectedKey = "${dateStr}_$time"
                    val isTaken = takenHistory.containsKey(expectedKey)
                    
                    if (!isTaken) {
                        // Avoid double notification: Check if someone already logged a MISSED_MED alert
                        val dateAlertStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
                        val alertId = "${medicineId}_${dateAlertStr}_${time.replace(":", "")}_MISSED_MED"
                        val alertDoc = db.collection("alerts").document(alertId).get().await()

                        if (!alertDoc.exists()) {
                            // Alert user locally
                            showNotification(context, "เตือนใช้ยา (ยังไม่ได้ใช้)", message, medicineId, time)
                            
                            // Log to DB only if NOT a watchdog (to prevent push feedback loop)
                            if (!isWatchdog && userId != null) {
                                logActivityToDb(db, userId, "MISSED_MED", "ยังไม่ได้บันทึกการใช้ยา $medicineName ($time)", medicineId, time)
                            }
                        }
                    }
                } else {
                    // Initial alarm: Notify user locally
                    showNotification(context, "เตือนใช้ยา", message, medicineId, time)
                    // Log to DB for caretaker/history
                    if (userId != null) logActivityToDb(db, userId, "MEDICINE", "ได้เวลาใช้ยา $medicineName ($time)", medicineId, time)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun logActivityToDb(db: FirebaseFirestore, userId: String, type: String, message: String, medicineId: String?, time: String?) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val groupIds = (userDoc.get("groupIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val name = userDoc.getString("name") ?: "ผู้ป่วย"
            
            // Include date in alert ID to prevent daily overwrites
            val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
            val alertId = "${medicineId ?: "manual"}_${dateStr}_${time?.replace(":", "") ?: "now"}_${type}"
            
            val activity = mapOf(
                "id" to alertId,
                "senderId" to userId,
                "senderName" to name,
                "type" to type,
                "message" to message,
                "timestamp" to System.currentTimeMillis(),
                "groupIds" to groupIds,
                "isSilent" to (type == "MEDICINE") // Only MEDICINE is silent; MISSED_MED triggers push
            )
            
            db.collection("alerts").document(alertId).set(activity).await()
        } catch (e: Exception) {
            e.printStackTrace()
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
