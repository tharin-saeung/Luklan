package com.commu.luklan.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.ui.theme.*
import com.commu.luklan.ui.components.MedicineIcon
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime

data class HistoryEntry(
    val medicine: Medicine,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    targetUserId: String? = null,
    onBack: () -> Unit
) {
    val medicineRepository = remember { getMedicineRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var historyEntries by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadHistory() {
        isLoading = true
        scope.launch {
            val userId = targetUserId ?: authRepository.getCurrentUserId()
            if (userId != null) {
                medicineRepository
                    .getMedicines(userId)
                    .onSuccess { medicines ->
                        val entries = mutableListOf<HistoryEntry>()
                        medicines.forEach { med ->
                            med.takenHistory.forEach { (_, timestamp) ->
                                entries.add(HistoryEntry(med, timestamp))
                            }
                        }
                        historyEntries = entries.sortedByDescending { it.timestamp }
                        isLoading = false
                    }
                    .onFailure { isLoading = false }
            } else {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadHistory() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ประวัติการใช้ยา",
                        style = LuklanTypography.h1,
                        color = LuklanColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = LuklanColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuklanColors.Background
                )
            )
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LuklanColors.Primary)
                }
            } else if (historyEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "No history",
                            tint = LuklanColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "ยังไม่มีประวัติการใช้ยา",
                            style = LuklanTypography.bodyLarge,
                            color = LuklanColors.TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(LuklanSpacing.lg)
                ) {
                    items(historyEntries) { entry ->
                        HistoryMedicineCard(medicine = entry.medicine, timestamp = entry.timestamp)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryMedicineCard(medicine: Medicine, timestamp: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LuklanSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(LuklanColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                MedicineIcon(category = medicine.category, iconSize = 32.dp)
            }

            Spacer(modifier = Modifier.width(LuklanSpacing.md))

            // Medicine Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuklanColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Dosage info
                val dosageText = buildString {
                    if (medicine.dosage.isNotEmpty()) {
                        append(medicine.dosage)
                        if (medicine.unit.isNotEmpty()) {
                            append(" ${medicine.unit}")
                        }
                    }
                }
                
                if (dosageText.isNotEmpty()) {
                    Text(
                        text = dosageText,
                        fontSize = 14.sp,
                        color = LuklanColors.TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time taken
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = LuklanColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDateTime(timestamp),
                        fontSize = 13.sp,
                        color = LuklanColors.TextSecondary
                    )
                }
            }

            // Checkmark indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Taken",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatDateTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    
    val thaiMonths = listOf(
        "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
        "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    )
    
    val day = dateTime.dayOfMonth
    val month = thaiMonths[dateTime.monthNumber - 1]
    val year = dateTime.year + 543 // Convert to Buddhist year
    
    val hour = when {
        dateTime.hour == 0 -> "00"
        else -> dateTime.hour.toString()
    }
    val minute = if (dateTime.minute < 10) "0${dateTime.minute}" else "${dateTime.minute}"
    
    return "$day $month $year, $hour:$minute น."
}
