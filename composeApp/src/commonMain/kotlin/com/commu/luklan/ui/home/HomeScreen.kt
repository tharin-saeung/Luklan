package com.commu.luklan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.commu.luklan.ui.components.DropdownSelector
import com.commu.luklan.ui.theme.*
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun HomeScreen(
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMedicineDetail: (Medicine) -> Unit
) {
    val medicineRepository = remember { getMedicineRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var isLoading by remember { mutableStateOf<Boolean>(true) }

    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    val dayInitials = listOf("อา", "จ", "อ", "พ", "พฤ", "ศ", "ส")

    // Date Logic using project utility to avoid Clock resolution issues on Windows
    val now = remember {
        val nowMillis = getCurrentTimeMillis()
        val instant = Instant.fromEpochMilliseconds(nowMillis)
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    
    var selectedDay by remember { mutableStateOf<Int>(now.dayOfMonth) }
    var displayMonth by remember { mutableStateOf<Int>(now.monthNumber) }
    var displayYear by remember { mutableStateOf<Int>(now.year) }
    var showMonthYearPicker by remember { mutableStateOf<Boolean>(false) }

    val daysInMonth = when (displayMonth) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if ((displayYear % 4 == 0 && displayYear % 100 != 0) || (displayYear % 400 == 0)) 29 else 28
        else -> 31
    }
    
    fun loadMedicines() {
        isLoading = true
        scope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                medicineRepository
                    .getMedicines(userId)
                    .onSuccess { allMedicines ->
                        medicines = allMedicines.sortedBy { m -> (m.times.firstOrNull() ?: m.time) }
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
                containerColor = Color.White,
                contentColor = LuklanColors.Primary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp).size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(36.dp))
            }
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with Profile & Notification Bell
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LuklanSpacing.lg, vertical = LuklanSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, LuklanColors.Primary.copy(alpha = 0.1f), CircleShape)
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 32.sp)
                }

                Text(
                    text = "ลูกหลาน",
                    style = LuklanTypography.h3,
                    color = LuklanColors.Primary,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { /* TODO: Notification Page */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Black, modifier = Modifier.size(36.dp))
                }
            }

            // Month/Year Selection Above Dates
            Box(
                modifier = Modifier
                    .padding(horizontal = LuklanSpacing.lg)
                    .clickable { showMonthYearPicker = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${thaiMonths[displayMonth - 1]} ${displayYear + 543}",
                        style = LuklanTypography.h2,
                        color = LuklanColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LuklanColors.Primary)
                }
            }

            // Date Selector (Primary Blue unselected, Orange selected)
            val listState = rememberLazyListState()
            LaunchedEffect(displayMonth, displayYear) {
                val target = if (displayMonth == now.monthNumber && displayYear == now.year) now.dayOfMonth else 1
                if (target > 3) listState.scrollToItem(target - 3) else listState.scrollToItem(0)
            }

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuklanSpacing.md),
                contentPadding = PaddingValues(horizontal = LuklanSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(daysInMonth) { index ->
                    val dayNum = index + 1
                    val isToday = dayNum == now.dayOfMonth && displayMonth == now.monthNumber && displayYear == now.year
                    val isSelected = selectedDay == dayNum && displayMonth == now.monthNumber
                    
                    val dayName = try {
                        val date = LocalDate(displayYear, displayMonth, dayNum)
                        dayInitials[(date.dayOfWeek.ordinal + 1) % 7]
                    } catch (e: Exception) { "" }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .size(width = 65.dp, height = 90.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(if (isSelected) LuklanColors.Secondary else LuklanColors.Primary)
                            .clickable { selectedDay = dayNum }
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dayNum.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = dayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(LuklanSpacing.sm))

            // Section Title with Arrows
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LuklanSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ยาที่ต้องกิน",
                    style = LuklanTypography.h2,
                    fontWeight = FontWeight.Bold,
                    color = LuklanColors.Primary
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { /* Previous Page */ }, modifier = Modifier.size(32.dp).border(1.dp, LuklanColors.Secondary, CircleShape)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = LuklanColors.Secondary)
                    }
                    IconButton(onClick = { /* Next Page */ }, modifier = Modifier.size(32.dp).background(LuklanColors.Primary, CircleShape)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(LuklanSpacing.md))

            // Medicine List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LuklanColors.Primary)
                }
            } else if (medicines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Text("ไม่มียาที่ต้องกิน", color = LuklanColors.TextSecondary, style = LuklanTypography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(medicines) { medicine ->
                        MedicineCardGrouped(
                            medicine = medicine,
                            onClick = { onNavigateToMedicineDetail(medicine) }
                        )
                    }
                    item { Spacer(Modifier.height(120.dp)) }
                }
            }
        }
    }

    // Month/Year Picker Dialog
    if (showMonthYearPicker) {
        var tempMonth by remember { mutableStateOf(displayMonth) }
        var tempYear by remember { mutableStateOf(displayYear) }
        
        AlertDialog(
            onDismissRequest = { showMonthYearPicker = false },
            title = { Text("เลือกเดือนและปี", style = LuklanTypography.h3) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DropdownSelector(
                        label = "เดือน",
                        selectedValue = thaiMonths[tempMonth - 1],
                        options = thaiMonths,
                        onValueChange = { m -> tempMonth = thaiMonths.indexOf(m) + 1 },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val yearOptions = (now.year - 2..now.year + 2).map { (it + 543).toString() }
                    DropdownSelector(
                        label = "ปี",
                        selectedValue = (tempYear + 543).toString(),
                        options = yearOptions,
                        onValueChange = { y -> tempYear = y.toInt() - 543 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    displayMonth = tempMonth
                    displayYear = tempYear
                    showMonthYearPicker = false
                }) { Text("ตกลง", color = LuklanColors.Primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showMonthYearPicker = false }) { Text("ยกเลิก") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun MedicineCardGrouped(
    medicine: Medicine,
    onClick: () -> Unit
) {
    val allTaken = (medicine.times.isNotEmpty() && medicine.times.all { medicine.takenRecords[it] == true })
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = LuklanColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("💊", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val amount = if (medicine.amountPerDose.isNotEmpty()) medicine.amountPerDose else medicine.dosage
                val dosageDisplay = "$amount ${medicine.unit}"
                
                Text(
                    text = dosageDisplay,
                    style = LuklanTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    text = medicine.name,
                    style = LuklanTypography.h3,
                    color = LuklanColors.Secondary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (allTaken) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                
                val time = medicine.times.firstOrNull() ?: medicine.time
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$time น.",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
