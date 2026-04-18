package com.commu.luklan.ui.medicine

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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    medicine: Medicine,
    initialSlotTime: String? = null,
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

    val timesToChoose = if (currentMedicine.times.isNotEmpty()) currentMedicine.times else listOf(currentMedicine.time)

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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            
            // Large Icon Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("💊", fontSize = 80.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Medicine Name
            Text(
                text = currentMedicine.name,
                style = LuklanTypography.h1,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Dosage Info Line: Name / Dosage
            val subtext = buildString {
                if (currentMedicine.description.isNotEmpty()) append(currentMedicine.description + " ")
                append("${currentMedicine.amountPerDose} ${currentMedicine.unit}")
            }
            Text(
                text = subtext,
                style = LuklanTypography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Text(
                text = "*กินยาติดต่อกันจนหมด",
                style = LuklanTypography.bodySmall,
                color = LuklanColors.Secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Boxes Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Medication,
                    label = "ประเภทยา",
                    value = currentMedicine.category.ifEmpty { "เม็ด" }
                )
                InfoBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccessTime,
                    label = "เวลาที่กิน",
                    value = currentMedicine.mealTiming.ifEmpty { "หลังอาหาร" }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Section Header
            Text(
                text = "เวลาการกินยา",
                style = LuklanTypography.h2,
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Slots Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(LuklanColors.PrimaryDark.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                timesToChoose.forEachIndexed { index, time ->
                    val isTaken = currentMedicine.takenRecords[time] == true
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                slotToConfirm = Pair(time, index)
                                showConfirmationDialog = true
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "$time น.",
                            style = LuklanTypography.h3,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isTaken) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                        Spacer(Modifier.weight(1f))
                        if (isTaken) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(LuklanColors.Success),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(28.dp).border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Delete Button
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(8.dp))
                    Text("ลบยา", color = Color.White.copy(alpha = 0.7f), style = LuklanTypography.bodyLarge)
                }
            }
        }
    }

    // Taken Confirmation Dialog (Prototype Style)
    if (showConfirmationDialog && slotToConfirm != null) {
        Dialog(
            onDismissRequest = { showConfirmationDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(LuklanColors.Primary)
                    .padding(24.dp)
            ) {
                // Close button X
                IconButton(
                    onClick = { showConfirmationDialog = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Close", tint = Color.White)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.height(8.dp))
                    
                    // Orange Icon in White Circle
                    Box(
                        modifier = Modifier.size(110.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(LuklanColors.Secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💊", fontSize = 48.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        text = currentMedicine.name,
                        style = LuklanTypography.h2,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "รับประทาน ${currentMedicine.amountPerDose} ${currentMedicine.unit}",
                        style = LuklanTypography.h3,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "เวลา ${slotToConfirm!!.first} น.",
                        style = LuklanTypography.h3,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Confirm Button (Green check)
                        Button(
                            onClick = {
                                val time = slotToConfirm!!.first
                                val index = slotToConfirm!!.second
                                val newRecords = currentMedicine.takenRecords.toMutableMap()
                                newRecords[time] = true
                                val allTaken = timesToChoose.all { newRecords[it] == true }
                                val updatedMed = currentMedicine.copy(takenRecords = newRecords, taken = allTaken)
                                currentMedicine = updatedMed
                                scope.launch {
                                    notificationScheduler.cancelSlot(updatedMed, index)
                                    medicineRepository.updateMedicine(updatedMed)
                                }
                                showConfirmationDialog = false
                            },
                            modifier = Modifier.weight(1f).height(90.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(LuklanColors.Success),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("รับประทานแล้ว", color = Color.White, style = LuklanTypography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                        
                            // Not Taken Button (Red cross)
                        Button(
                            onClick = {
                                val time = slotToConfirm!!.first
                                val newRecords = currentMedicine.takenRecords.toMutableMap()
                                newRecords[time] = false
                                val updatedMed = currentMedicine.copy(takenRecords = newRecords, taken = false)
                                currentMedicine = updatedMed
                                scope.launch {
                                    notificationScheduler.schedule(updatedMed)
                                    medicineRepository.updateMedicine(updatedMed)
                                }
                                showConfirmationDialog = false
                            },
                            modifier = Modifier.weight(1f).height(90.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(LuklanColors.Error),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "ยังไม่รับประทาน",
                                    color = Color.White,
                                    style = LuklanTypography.caption,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("ต้องการลบยานี้หรอครับ?", style = LuklanTypography.h3, fontWeight = FontWeight.Bold) },
            text = { Text("คุณต้องการลบยา \"${currentMedicine.name}\" หรือไม่? เราจะหยุดส่งการแจ้งเตือนการกินยานี้ให้คุณ", style = LuklanTypography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            notificationScheduler.cancel(currentMedicine)
                            medicineRepository.deleteMedicine(currentMedicine.id)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("ลบ", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("ยกเลิก", color = LuklanColors.TextSecondary) }
            }
        )
    }
}

@Composable
fun InfoBox(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LuklanColors.PrimaryDark.copy(alpha = 0.4f))
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(LuklanColors.Secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = label, style = LuklanTypography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Text(text = value, style = LuklanTypography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
