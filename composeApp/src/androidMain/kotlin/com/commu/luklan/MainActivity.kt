package com.commu.luklan

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var deepLinkMedicineId by mutableStateOf<String?>(null)
    private var deepLinkTime by mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkExactAlarmPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkExactAlarmPermission()
            }
        } else {
            checkExactAlarmPermission()
        }

        createNotificationChannel()

        setContent {
            App(deepLinkMedicineId, deepLinkTime)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        deepLinkMedicineId = intent.getStringExtra("DEEP_LINK_MEDICINE_ID")
        deepLinkTime = intent.getStringExtra("DEEP_LINK_TIME")
    }
    
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Open settings to allow exact alarms
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Medicine Reminders"
            val descriptionText = "Channel for medicine reminders"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel =
                    android.app.NotificationChannel("medicine_channel", name, importance).apply {
                        description = descriptionText
                    }
            val notificationManager: android.app.NotificationManager =
                    getSystemService(android.content.Context.NOTIFICATION_SERVICE) as
                            android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
