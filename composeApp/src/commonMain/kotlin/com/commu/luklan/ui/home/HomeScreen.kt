package com.commu.luklan.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.ui.theme.*
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.jetbrains.compose.resources.painterResource
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun HomeScreen(
    targetUserId: String? = null,
    targetUserName: String? = null,
    onBack: (() -> Unit)? = null,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMedicineDetail: (Medicine, String) -> Unit,
    onNavigateToHistory: (String?) -> Unit = {},
    onNavigateToMedicineGroups: (String?) -> Unit = {},
    onNavigateToNotificationCenter: (String) -> Unit = {}
) {
    val authRepository = remember { getAuthRepository() }
    val medicineRepository = remember { getMedicineRepository() }
    val scope = rememberCoroutineScope()
    
    val userId = targetUserId ?: authRepository.getCurrentUserId() ?: ""
    val isCaretakerView = targetUserId != null && targetUserId.isNotEmpty()

    var medicines = remember { mutableStateListOf<Medicine>() }
    var userProfile by remember { mutableStateOf<com.commu.luklan.data.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditMode by remember { mutableStateOf(false) }
    var showCaretakerMenu by remember { mutableStateOf(false) }

    // Date Logic
    val now = remember {
        val nowMillis = getCurrentTimeMillis()
        val instant = Instant.fromEpochMilliseconds(nowMillis)
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    
    var selectedMonth by remember { mutableStateOf(now.monthNumber) }
    var selectedYear by remember { mutableStateOf(now.year) }
    var selectedDay by remember { mutableStateOf(now.dayOfMonth) }
    
    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    val dayInitials = listOf("อา", "จ", "อ", "พ", "พฤ", "ศ", "ส")

    fun loadData() {
        if (userId.isEmpty()) return
        isLoading = true
        scope.launch {
            // Fetch User Profile
            authRepository.getUserProfile(userId).onSuccess { userProfile = it }
            
            // Fetch Medicines
            medicineRepository.getMedicines(userId).onSuccess { list ->
                medicines.clear()
                medicines.addAll(list.sortedBy { it.order })
                
                // Sync notifications for self only
                if (!isCaretakerView) {
                    val scheduler = com.commu.luklan.data.getNotificationScheduler()
                    list.forEach { scheduler.schedule(it) }
                }
                
                isLoading = false
            }.onFailure { isLoading = false }
        }
    }

    LaunchedEffect(userId) { loadData() }

    Box(modifier = Modifier.fillMaxSize().background(LuklanColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LuklanSpacing.lg, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCaretakerView && onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LuklanColors.Primary)
                    }
                } else {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) { 
                        if (!userProfile?.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfile?.photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = LuklanColors.Primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Text(
                    text = userProfile?.name ?: targetUserName ?: "ลูกหลาน",
                    style = LuklanTypography.h3,
                    color = LuklanColors.Primary,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onNavigateToNotificationCenter(userId) }) {
                    Icon(Icons.Default.Notifications, null, tint = LuklanColors.Primary, modifier = Modifier.size(32.dp))
                }
            }

            // Month Selector (Prototype Style)
            var showMonthYearPicker by remember { mutableStateOf(false) }
            
            Row(
                modifier = Modifier
                    .padding(horizontal = LuklanSpacing.lg)
                    .clickable { showMonthYearPicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = thaiMonths[selectedMonth - 1],
                    style = LuklanTypography.h1,
                    color = LuklanColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 4.dp) // Optical center fix for Thai baseline
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${selectedYear + 543}",
                    style = LuklanTypography.h1,
                    color = LuklanColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown, 
                    null, 
                    tint = LuklanColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (showMonthYearPicker) {
                com.commu.luklan.ui.components.MonthYearPicker(
                    initialMonth = selectedMonth,
                    initialYear = selectedYear,
                    onDismiss = { showMonthYearPicker = false },
                    onConfirm = { m, y ->
                        selectedMonth = m
                        selectedYear = y
                        showMonthYearPicker = false
                    }
                )
            }

            // Date Row
            val listState = rememberLazyListState()
            
            val daysInMonth = try {
                val firstDayOfNextMonth = if (selectedMonth == 12) {
                    LocalDate(selectedYear + 1, 1, 1)
                } else {
                    LocalDate(selectedYear, selectedMonth + 1, 1)
                }
                firstDayOfNextMonth.minus(DatePeriod(days = 1)).dayOfMonth
            } catch (e: Exception) {
                31
            }

            LaunchedEffect(selectedMonth, selectedYear) {
                if (selectedDay > daysInMonth) selectedDay = daysInMonth
                if (selectedDay > 3) listState.scrollToItem(selectedDay - 3) else listState.scrollToItem(0)
            }

            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = LuklanSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(daysInMonth) { index ->
                    val dayNum = index + 1
                    val isSelected = selectedDay == dayNum
                    val dayName = try {
                        val date = LocalDate(selectedYear, selectedMonth, dayNum)
                        dayInitials[(date.dayOfWeek.ordinal + 1) % 7]
                    } catch (e: Exception) { "" }
                    
                    val isToday = dayNum == now.dayOfMonth && selectedMonth == now.monthNumber && selectedYear == now.year

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.size(width = 65.dp, height = 90.dp).clip(RoundedCornerShape(32.dp))
                            .background(if (isSelected) LuklanColors.Secondary else LuklanColors.Primary)
                            .clickable { selectedDay = dayNum }.padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            dayNum.toString(), 
                            fontSize = 22.sp, 
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, 
                            color = Color.White
                        )
                        Text(
                            dayName, 
                            fontSize = 14.sp, 
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        
                        if (isToday) {
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            // Medicine List
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LuklanSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ยาที่ต้องกิน", style = LuklanTypography.h2, color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
                
                Surface(
                    onClick = { isEditMode = !isEditMode },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isEditMode) LuklanColors.Secondary.copy(0.2f) else LuklanColors.Secondary.copy(0.1f)
                ) {
                    Text(
                        if (isEditMode) "เสร็จสิ้น" else "แก้ไข",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = LuklanColors.Secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val dateStr = "${selectedYear}-${selectedMonth.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
            
            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
            val todayStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
            
            val filteredMedicines = medicines.filter { it.isAvailableOnDate(dateStr, todayStr) }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LuklanColors.Primary)
                }
            } else if (filteredMedicines.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("ไม่มีรายการยา", color = LuklanColors.TextSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(LuklanSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMedicines) { med ->
                        val isFullyTaken = med.times.all { time ->
                            med.takenHistory.containsKey("${dateStr}_$time")
                        }

                        MedicineCardGrouped(
                            medicine = med,
                            isEditMode = isEditMode,
                            isTakenToday = isFullyTaken,
                            onDelete = {
                                medicines.remove(med)
                                scope.launch { medicineRepository.deleteMedicine(med.id) }
                            },
                            onClick = { 
                                onNavigateToMedicineDetail(med, dateStr) 
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // FAB Section
        if (!isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                // Menu Button (Bottom Left) - Only for Caretaker View
                if (isCaretakerView) {
                    Surface(
                        onClick = { showCaretakerMenu = !showCaretakerMenu },
                        modifier = Modifier.size(64.dp).align(Alignment.BottomStart),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (showCaretakerMenu) Icons.Default.Close else Icons.Default.Menu,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = LuklanColors.Primary
                            )
                        }
                    }
                }

                // Add Medicine Button (Bottom Right)
                Surface(
                    onClick = onNavigateToAddMedicine,
                    modifier = Modifier.size(64.dp).align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = LuklanColors.Primary
                        )
                    }
                }
            }

            // Expandable Caretaker Menu
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 32.dp, bottom = 104.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item 1: Medication Groups
                AnimatedVisibility(
                    visible = isCaretakerView && showCaretakerMenu,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 2 },
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { it / 2 }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            showCaretakerMenu = false
                            onNavigateToMedicineGroups(userId) 
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Medication, "Groups", tint = LuklanColors.Primary)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                "กลุ่มยา",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = LuklanTypography.bodyMedium,
                                color = LuklanColors.Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Item 2: History
                AnimatedVisibility(
                    visible = isCaretakerView && showCaretakerMenu,
                    enter = fadeIn(animationSpec = tween(300, 100)) + slideInVertically(animationSpec = tween(300, 100)) { it / 2 },
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { it / 2 }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            showCaretakerMenu = false
                            onNavigateToHistory(userId) 
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.History, "History", tint = LuklanColors.Primary)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                "ประวัติการกินยา",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = LuklanTypography.bodyMedium,
                                color = LuklanColors.Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineCardGrouped(
    medicine: Medicine,
    isEditMode: Boolean = false,
    isTakenToday: Boolean = false,
    onDelete: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { if (!isEditMode) onClick() },
        shape = RoundedCornerShape(50.dp), // Pill Shape
        colors = CardDefaults.cardColors(containerColor = if (isTakenToday) LuklanColors.Primary.copy(alpha = 0.6f) else LuklanColors.Primary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine Icon in White Circle
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .graphicsLayer { alpha = if (isTakenToday) 0.6f else 1.0f }
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                when (medicine.category) {
                    "แคปซูล" -> Image(painterResource(Res.drawable.capsule), null, modifier = Modifier.size(45.dp))
                    "เม็ด" -> Image(painterResource(Res.drawable.pill), null, modifier = Modifier.size(45.dp))
                    "น้ำ" -> Image(painterResource(Res.drawable.liquid), null, modifier = Modifier.size(45.dp))
                    "ครีม" -> Image(painterResource(Res.drawable.cream), null, modifier = Modifier.size(45.dp))
                    "เหน็บ" -> Image(painterResource(Res.drawable.suppository), null, modifier = Modifier.size(45.dp))
                    "ฉีด" -> Image(painterResource(Res.drawable.inject), null, modifier = Modifier.size(45.dp))
                    "อื่นๆ" -> Image(painterResource(Res.drawable.other), null, modifier = Modifier.size(45.dp))
                    else -> {
                        Text(text = "💊", fontSize = 38.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${medicine.dosage} ${medicine.unit}", 
                    style = LuklanTypography.bodySmall, 
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = medicine.name, 
                    style = LuklanTypography.h3, 
                    color = if (isTakenToday) LuklanColors.Secondary.copy(alpha = 0.7f) else LuklanColors.Secondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule, 
                        contentDescription = null, 
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (medicine.times.isNotEmpty()) medicine.times.joinToString(" น., ") + " น." else "",
                        style = LuklanTypography.bodySmall, 
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            if (isEditMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Cancel, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            } else if (isTakenToday) {
                Box(
                    modifier = Modifier.padding(end = 16.dp).size(32.dp).clip(CircleShape).background(LuklanColors.Success),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight, 
                    contentDescription = null, 
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    }
}
