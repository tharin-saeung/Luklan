package com.commu.luklan.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    targetUserId: String? = null,
    onBack: () -> Unit
) {
    val alertRepository = remember { getAlertRepository() }
    val authRepository = remember { getAuthRepository() }
    var alerts by remember { mutableStateOf<List<Alert>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(targetUserId) {
        val currentUid = authRepository.getCurrentUserId()
        val uidToFetch = targetUserId ?: currentUid
        
        if (currentUid != null && uidToFetch != null) {
            alertRepository.getAlertsForUser(currentUid).onSuccess { list ->
                // Filter to show only alerts from the target user
                alerts = list.filter { it.senderId == uidToFetch }
                isLoading = false
            }.onFailure { isLoading = false }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("การแจ้งเตือน", style = LuklanTypography.h2, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LuklanColors.Primary)
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
                    AlertItem(alert)
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AlertItem(alert: Alert) {
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
            
            Column {
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
        }
    }
}
