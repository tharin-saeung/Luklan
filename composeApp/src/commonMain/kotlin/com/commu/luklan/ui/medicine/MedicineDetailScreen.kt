package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    medicine: Medicine,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onMedicineTaken: () -> Unit
) {
    val medicineRepository = remember { getMedicineRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTakenDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ยา", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LuklanColors.Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuklanColors.Background
                )
            )
        },
        containerColor = LuklanColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Medicine Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("💊", fontSize = 64.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Medicine Name
            Text(
                text = medicine.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LuklanColors.Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dosage with unit and frequency
            val dosageText = buildString {
                if (medicine.dosage.isNotEmpty()) {
                    append(medicine.dosage)
                    if (medicine.unit.isNotEmpty()) {
                        append(" ${medicine.unit}")
                    }
                }
                if (medicine.frequency.isNotEmpty()) {
                    if (isNotEmpty()) append(" ")
                    append(medicine.frequency)
                }
            }
            
            if (dosageText.isNotEmpty()) {
                Text(
                    text = dosageText,
                    fontSize = 18.sp,
                    color = LuklanColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Time Schedule Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LuklanColors.Surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "เวลาทึ่ต้องกิน",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuklanColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = LuklanColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = medicine.time,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuklanColors.TextPrimary
                        )
                    }

                    if (medicine.frequency.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = medicine.frequency,
                            fontSize = 14.sp,
                            color = LuklanColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Section
            if (medicine.description.isNotEmpty() || medicine.category.isNotEmpty() || 
                medicine.storageInstructions.isNotEmpty() || medicine.notes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LuklanColors.Surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (medicine.description.isNotEmpty()) {
                            DetailRow(icon = Icons.Default.Info, label = "รายละเอียด", value = medicine.description)
                        }
                        if (medicine.category.isNotEmpty()) {
                            DetailRow(icon = Icons.Default.Category, label = "ประเภท", value = medicine.category)
                        }
                        if (medicine.quantity > 0) {
                            DetailRow(icon = Icons.Default.Inventory, label = "จำนวน", value = "${medicine.quantity} ${medicine.unit}")
                        }
                        if (medicine.expiryDate.isNotEmpty()) {
                            DetailRow(icon = Icons.Default.Event, label = "วันหมดอายุ", value = medicine.expiryDate)
                        }
                        if (medicine.storageInstructions.isNotEmpty()) {
                            DetailRow(icon = Icons.Default.Storage, label = "วิธีเก็บรักษา", value = medicine.storageInstructions)
                        }
                        if (medicine.notes.isNotEmpty()) {
                            DetailRow(icon = Icons.Default.StickyNote2, label = "หมายเหตุ", value = medicine.notes)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LuklanColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("แก้ไข", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                // Take Medicine Button
                Button(
                    onClick = { showTakenDialog = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuklanColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("กินแล้ว", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = LuklanColors.Surface,
            title = {
                Text(
                    "ต้องการลบยานี้หรอครับ?",
                    style = LuklanTypography.h3,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "คุณต้องการลบยา \"${medicine.name}\" หรือไม่? เราจะหยุดส่งการแจ้งเตือนการกินยานี้ให้คุณ",
                    style = LuklanTypography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            notificationScheduler.cancel(medicine)
                            medicineRepository.deleteMedicine(medicine.id)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuklanColors.Error
                    )
                ) {
                    Text("ลบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก", color = LuklanColors.TextSecondary)
                }
            }
        )
    }

    // Taken Dialog
    if (showTakenDialog) {
        AlertDialog(
            onDismissRequest = { showTakenDialog = false },
            containerColor = LuklanColors.Surface,
            title = {
                Text(
                    "ต้องการลบยานี้หรอครับ?",
                    style = LuklanTypography.h3,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "คุณต้องการลบยา \"${medicine.name}\" หรือไม่? เราจะหยุดส่งการแจ้งเตือนการกินยานี้ให้คุณ",
                    style = LuklanTypography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTakenDialog = false
                        scope.launch {
                            notificationScheduler.cancel(medicine)
                            medicineRepository.updateMedicine(medicine.copy(taken = true))
                            onMedicineTaken()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuklanColors.Primary
                    )
                ) {
                    Text("ลบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTakenDialog = false }) {
                    Text("ยกเลิก", color = LuklanColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = LuklanColors.Primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = LuklanColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = LuklanColors.TextPrimary
            )
        }
    }
}
