package com.commu.luklan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.theme.*
import kotlinx.coroutines.launch
import luklan.composeapp.generated.resources.Res

@Composable
fun HomeScreen(
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEditMedicine: (Medicine) -> Unit
) {
    val medicineRepository = remember { getMedicineRepository() }
    val authRepository = remember { AuthRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadMedicines() {
        scope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                medicineRepository
                    .getMedicines(userId)
                    .onSuccess {
                        medicines = it.filter { !it.taken }.sortedBy { m -> m.time }
                        isLoading = false
                    }
                    .onFailure { isLoading = false }
            } else {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadMedicines() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMedicine,
                containerColor = LuklanColors.Primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Medicine",
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 28.sp)
                }

                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = LuklanColors.TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Month/Year Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.md),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "วันนี้",
                    style = LuklanTypography.h2,
                    fontWeight = FontWeight.Bold,
                    color = LuklanColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(LuklanSpacing.lg))

            // Section Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LuklanSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ยาที่ต้องกิน",
                    style = LuklanTypography.h3,
                    fontWeight = FontWeight.Bold,
                    color = LuklanColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(LuklanSpacing.md))

            // Medicine List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LuklanColors.Primary)
                }
            } else if (medicines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "ไม่มียาที่ต้องกินในวันนี้",
                        style = LuklanTypography.bodyLarge,
                        color = LuklanColors.TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        top = 0.dp,
                        end = 12.dp,
                        bottom = 0.dp
                    )
                ) {
                    items(medicines) { medicine ->
                        MedicineCardRounded(
                            medicine = medicine,
                            onTaken = {
                                medicines = medicines.filter { it.id != medicine.id }
                                scope.launch {
                                    // ✅ Cancel notification ก่อน
                                    notificationScheduler.cancel(medicine)

                                    medicineRepository
                                        .updateMedicine(medicine.copy(taken = true))
                                        .onFailure { loadMedicines() }
                                }
                            },
                            onEdit = { onNavigateToEditMedicine(medicine) },
                            onDelete = {
                                medicines = medicines.filter { it.id != medicine.id }
                                scope.launch {
                                    // ✅ Cancel notification ก่อนลบ
                                    notificationScheduler.cancel(medicine)

                                    medicineRepository
                                        .deleteMedicine(medicine.id)
                                        .onFailure { loadMedicines() }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineCardRounded(
    medicine: Medicine,
    onTaken: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    BoxWithConstraints {
        // Responsive sizing based on screen width
        val screenWidth = maxWidth
        
        // Smaller padding and sizes for smaller screens (< 375dp like iPhone 12 mini)
        val isSmallScreen = screenWidth < 375.dp
        val horizontalPadding = if (isSmallScreen) 8.dp else 12.dp
        val iconSize = if (isSmallScreen) 66.dp else 74.dp
        val paddingWidth = if (isSmallScreen) 8.dp else 16.dp
        val spacerWidth = if (isSmallScreen) 2.dp else 4.dp
        val cardHeight = if (isSmallScreen) 92.dp else 100.dp

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight),
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.width(paddingWidth))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${medicine.description}",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = medicine.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuklanColors.Secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = medicine.time,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(spacerWidth))
            
            // ✅ ปุ่มกินยาแล้ว
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LuklanColors.Secondary)
                        .clickable { onTaken() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark as taken",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "กินแล้ว",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(spacerWidth / 2))

            // ✅ ปุ่ม Menu (Edit/Delete)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("แก้ไข") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("ลบ", color = LuklanColors.Error) },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = LuklanColors.Error
                            )
                        }
                    )
                }
            }
        }
    }
    }

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
                        onDelete()
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
}