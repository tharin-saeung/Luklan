package com.commu.luklan.ui.medicine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.theme.*
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MedicineDetailScreen(
    medicine: Medicine,
    initialSlotTime: String? = null,
    selectedDate: String? = null, // yyyy-MM-dd
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onMedicineTaken: () -> Unit
) {
    val medicineRepository = remember { getMedicineRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()
    
    var currentMedicine by remember { mutableStateOf(medicine) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var slotToConfirm by remember { mutableStateOf<Pair<String, Int>?>(null) }

    val timesToChoose = currentMedicine.times

    // Use current date as fallback if none provided
    val activeDateStr = remember(selectedDate) {
        selectedDate ?: run {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
        }
    }

    // Trigger confirmation dialog if initialSlotTime is provided (Deep Link)
    LaunchedEffect(initialSlotTime) {
        if (initialSlotTime != null) {
            val index = timesToChoose.indexOf(initialSlotTime)
            if (index >= 0) {
                slotToConfirm = Pair(initialSlotTime, index)
                showConfirmationDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onBack() }.padding(16.dp)
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("หน้าหลัก", color = Color.White, style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuklanColors.Primary)
            )
        },
        containerColor = LuklanColors.Primary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Pill Icon
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val iconRes = when(currentMedicine.category) {
                            "แคปซูล" -> Res.drawable.capsule
                            "ฉีด" -> Res.drawable.inject
                            "อื่นๆ" -> Res.drawable.other
                            else -> Res.drawable.pill
                        }
                        Image(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = currentMedicine.name,
                    style = LuklanTypography.h1,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${currentMedicine.dosage} ${currentMedicine.unit}",
                    style = LuklanTypography.h2,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Surface(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = currentMedicine.mealTiming,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        style = LuklanTypography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Time Slots Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(LuklanColors.PrimaryDark.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                timesToChoose.forEachIndexed { index, time ->
                    val historyKey = "${activeDateStr}_$time"
                    val isTaken = currentMedicine.takenHistory.containsKey(historyKey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                slotToConfirm = Pair(time, index)
                                showConfirmationDialog = true
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "$time น.",
                                color = Color.White,
                                style = LuklanTypography.h2,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Icon(
                            imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isTaken) "Taken" else "Not taken",
                            tint = if (isTaken) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    if (index < timesToChoose.size - 1) {
                        Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Delete Button
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "ลบรายการยานี้",
                    color = Color.White.copy(alpha = 0.6f),
                    style = LuklanTypography.bodyLarge
                )
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }

    // Confirmation Dialog
    if (showConfirmationDialog && slotToConfirm != null) {
        val time = slotToConfirm!!.first
        val historyKey = "${activeDateStr}_$time"
        val isAlreadyTaken = currentMedicine.takenHistory.containsKey(historyKey)

        Dialog(onDismissRequest = { showConfirmationDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAlreadyTaken) "ยกเลิกการกินยา?" else "บันทึกการกินยา?",
                        style = LuklanTypography.h2,
                        color = LuklanColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "รอบเวลา $time น.",
                        style = LuklanTypography.bodyLarge,
                        color = LuklanColors.TextSecondary
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Confirm Button (Green check)
                        Button(
                            onClick = {
                                val time = slotToConfirm!!.first
                                val index = slotToConfirm!!.second
                                val historyKey = "${activeDateStr}_$time"
                                
                                val newHistory = currentMedicine.takenHistory.toMutableMap()
                                newHistory[historyKey] = getCurrentTimeMillis()
                                
                                val updatedMed = currentMedicine.copy(takenHistory = newHistory)
                                currentMedicine = updatedMed
                                scope.launch {
                                    notificationScheduler.cancelSlot(updatedMed, index)
                                    medicineRepository.updateMedicine(updatedMed)
                                }
                                showConfirmationDialog = false
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("กินแล้ว", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Not Taken Button (Red cross)
                        Button(
                            onClick = {
                                val time = slotToConfirm!!.first
                                val historyKey = "${activeDateStr}_$time"

                                val newHistory = currentMedicine.takenHistory.toMutableMap()
                                newHistory.remove(historyKey)
                                
                                val updatedMed = currentMedicine.copy(takenHistory = newHistory)
                                currentMedicine = updatedMed
                                scope.launch {
                                    notificationScheduler.schedule(updatedMed)
                                    medicineRepository.updateMedicine(updatedMed)
                                }
                                showConfirmationDialog = false
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("ยังไม่กิน", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    TextButton(
                        onClick = { showConfirmationDialog = false },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("ยกเลิก", color = LuklanColors.TextSecondary)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ลบรายการยา", fontWeight = FontWeight.Bold) },
            text = { Text("คุณต้องการลบรายการยานี้ใช่หรือไม่? การกระทำนี้ไม่สามารถย้อนกลับได้") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            medicineRepository.deleteMedicine(currentMedicine.id)
                            notificationScheduler.cancel(currentMedicine)
                            onBack()
                        }
                    }
                ) {
                    Text("ลบ", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}
