package com.commu.luklan.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.Alert
import com.commu.luklan.data.getAlertRepository
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanSpacing
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    targetUserId: String? = null,
    targetUserName: String? = null,
    onBack: () -> Unit,
    onMedicineClick: (com.commu.luklan.data.Medicine) -> Unit
) {
    val alertRepository = remember { getAlertRepository() }
    val authRepository = remember { getAuthRepository() }
    val scope = rememberCoroutineScope()
    var alerts by remember { mutableStateOf<List<Alert>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var groupIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var viewedUserName by remember { mutableStateOf(targetUserName) }

    var alertToDelete by remember { mutableStateOf<Alert?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    fun loadAlerts() {
        val currentUid = authRepository.getCurrentUserId()
        if (currentUid != null) {
            isLoading = true
            scope.launch {
                authRepository.getUserProfile(currentUid).onSuccess { currentUser ->
                    groupIds = currentUser.groupIds
                    
                    // Fetch viewed user's name if needed
                    if (targetUserId != null && targetUserId != currentUid && viewedUserName == null) {
                        authRepository.getUserProfile(targetUserId).onSuccess { viewedUserName = it.name }
                    }

                    // Always fetch alerts using current user's ID (to get access to shared groups)
                    alertRepository.getAlertsForUser(currentUid).onSuccess { list ->
                        alerts = if (targetUserId != null && targetUserId != currentUid) {
                            // VIEWING SPECIFIC PATIENT: Show their med log + SOS from caretakers
                            list.filter { 
                                (it.senderId == targetUserId && it.type != "SOS") || 
                                (it.senderId != targetUserId && it.type == "SOS") 
                            }
                        } else {
                            // HOME VIEW: Show own meds + SOS/Missed from others
                            list.filter { 
                                (it.senderId == currentUid && it.type != "SOS") || 
                                (it.senderId != currentUid && (it.type == "SOS" || it.type == "MISSED_MED")) 
                            }
                        }
                        isLoading = false
                    }.onFailure { isLoading = false }
                }.onFailure { isLoading = false }
            }
        }
    }

    LaunchedEffect(targetUserId) {
        loadAlerts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (viewedUserName != null && targetUserId != authRepository.getCurrentUserId()) 
                            "การแจ้งเตือนของ $viewedUserName" 
                            else "การแจ้งเตือน", 
                        style = LuklanTypography.h1,
                        color = LuklanColors.Primary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LuklanColors.Primary)
                    }
                },
                actions = {
                    if (alerts.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, "ล้างทั้งหมด", tint = LuklanColors.Error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuklanColors.Background)
            )
        },
        containerColor = LuklanColors.Background
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LuklanColors.Primary)
            }
        } else if (alerts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ไม่มีการแจ้งเตือน", color = LuklanColors.TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(LuklanSpacing.md),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts) { alert ->
                    AlertItem(
                        alert = alert,
                        onDelete = { alertToDelete = alert }
                    )
                }
            }
        }
    }

    // Individual Delete Dialog
    alertToDelete?.let { alert ->
        AlertDialog(
            onDismissRequest = { alertToDelete = null },
            title = { Text("ลบการแจ้งเตือน") },
            text = { Text("คุณต้องการลบการแจ้งเตือนนี้ใช่หรือไม่? การกระทำนี้ไม่สามารถย้อนกลับได้") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            alertRepository.deleteAlert(alert.id).onSuccess {
                                alertToDelete = null
                                loadAlerts()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = LuklanColors.Error)
                ) {
                    Text("ลบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { alertToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Delete All Dialog
    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text("ล้างการแจ้งเตือนทั้งหมด") },
            text = { Text("คุณต้องการลบการแจ้งเตือนทั้งหมดใช่หรือไม่? การกระทำนี้ไม่สามารถย้อนกลับได้") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val currentUid = authRepository.getCurrentUserId()
                            if (currentUid != null) {
                                alertRepository.deleteAllAlerts(currentUid, groupIds).onSuccess {
                                    showDeleteAllConfirm = false
                                    loadAlerts()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = LuklanColors.Error)
                ) {
                    Text("ล้างทั้งหมด")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AlertItem(
    alert: Alert,
    onDelete: () -> Unit
) {
    val date = remember(alert.timestamp) {
        val instant = Instant.fromEpochMilliseconds(alert.timestamp)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth}/${dt.monthNumber}/${dt.year + 543} ${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (alert.type == "SOS") LuklanColors.Error.copy(alpha = 0.1f) else LuklanColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alert.type == "SOS") Icons.Default.Warning else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (alert.type == "SOS") LuklanColors.Error else LuklanColors.Primary
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (alert.type == "SOS") "🆘 แจ้งเตือนฉุกเฉิน!" else "การแจ้งเตือน",
                    style = LuklanTypography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (alert.type == "SOS") LuklanColors.Error else LuklanColors.TextPrimary
                )
                Text(
                    text = alert.message,
                    style = LuklanTypography.bodyMedium,
                    color = LuklanColors.TextPrimary
                )
                Text(
                    text = date,
                    style = LuklanTypography.caption,
                    color = LuklanColors.TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "ลบ",
                    tint = LuklanColors.TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
