package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.components.WheelTimePicker
import com.commu.luklan.ui.components.DropdownSelector
import com.commu.luklan.ui.theme.*
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun AddMedicineScreen(onNavigateBack: () -> Unit) {
    val medicineRepository = remember { getMedicineRepository() }
    val authRepository = remember { AuthRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()

    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    val thaiDays = listOf("อา", "จ", "อ", "พ", "พฤ", "ศ", "ส")
    val mealTimingOptions = listOf("หลังอาหาร", "ก่อนอาหาร")

    // Date Logic using project utility to avoid Clock resolution issues on Windows
    val now = remember {
        val nowMillis = getCurrentTimeMillis()
        val instant = Instant.fromEpochMilliseconds(nowMillis)
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    
    // Form State
    var formState by remember { 
        mutableStateOf<MedicineFormState>(
            MedicineFormState(
                startDate = now.dayOfMonth.toString(),
                mealTiming = "หลังอาหาร",
                category = "เม็ด"
            )
        ) 
    }
    
    // Display State for Calendar Step
    var displayMonth by remember { mutableStateOf<Int>(now.monthNumber) }
    var displayYear by remember { mutableStateOf<Int>(now.year) }
    
    var step by remember { mutableStateOf<Int>(1) }
    val maxStep = 6
    
    var showTimePicker by remember { mutableStateOf<Boolean>(false) }
    var showMonthYearPicker by remember { mutableStateOf<Boolean>(false) }
    var editingTimeIndex by remember { mutableStateOf<Int>(-1) }
    var isLoading by remember { mutableStateOf<Boolean>(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun canNavigateNext(): Boolean {
        return when (step) {
            1 -> formState.name.isNotBlank()
            2 -> formState.amountPerDose.isNotBlank()
            3 -> formState.startDate.isNotBlank()
            4 -> formState.category.isNotBlank()
            5 -> formState.times.isNotEmpty()
            else -> true
        }
    }

    Scaffold(
        containerColor = LuklanColors.Primary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row (Higher title, Back icon included)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (step > 1) step -= 1 else onNavigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "เพิ่มยา",
                    style = LuklanTypography.h1,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1.3f))
            }

            Spacer(Modifier.height(24.dp))

            // Main Content Area (Centered)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> StepName(formState, onNext = { if (canNavigateNext()) step += 1 }) { formState = it }
                    2 -> StepAmount(formState, onNext = { if (canNavigateNext()) step += 1 }) { formState = it }
                    3 -> StepStartDate(
                        state = formState, 
                        days = thaiDays, 
                        months = thaiMonths,
                        displayMonth = displayMonth,
                        displayYear = displayYear,
                        onShowPicker = { showMonthYearPicker = true }
                    ) { formState = it }
                    4 -> StepCategory(formState) { formState = it }
                    5 -> StepTime(
                        state = formState, 
                        mealOptions = mealTimingOptions, 
                        onAdd = { editingTimeIndex = -1; showTimePicker = true },
                        onEdit = { idx -> editingTimeIndex = idx; showTimePicker = true }
                    ) { formState = it }
                    6 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        StepSummary(formState, thaiMonths, displayMonth, displayYear)
                    }
                }
            }

            // Error Message
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = LuklanColors.Secondary, style = LuklanTypography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }

            // Progress Indicator (Bottom)
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(maxStep) { i ->
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (i + 1 == step) LuklanColors.Secondary else Color.White.copy(alpha = 0.3f))
                    )
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (step > 1) {
                    Button(
                        onClick = { step -= 1; errorMessage = null },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuklanColors.Primary)
                    ) {
                        Text("ย้อนกลับ", style = LuklanTypography.buttonLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (step < maxStep) {
                            if (!canNavigateNext()) {
                                errorMessage = "กรุณากรอกข้อมูลให้ครบถ้วน"
                            } else {
                                errorMessage = null
                                step += 1
                            }
                        } else {
                            // Save Logic
                            val userId = authRepository.getCurrentUserId()
                            if (userId == null) { errorMessage = "Session expired"; return@Button }
                            
                            isLoading = true
                            scope.launch {
                                // Full formatted date for DB
                                val fullStartDate = "$displayYear-${displayMonth.toString().padStart(2, '0')}-${formState.startDate.padStart(2, '0')}"
                                
                                val medicine = Medicine(
                                    id = Uuid.random().toString(),
                                    name = formState.name,
                                    dosage = formState.amountPerDose,
                                    time = formState.times.firstOrNull() ?: "",
                                    times = formState.times,
                                    frequencyCount = formState.times.size,
                                    timeUnit = "วัน",
                                    amountPerDose = formState.amountPerDose,
                                    unit = formState.unit,
                                    startDate = fullStartDate,
                                    category = formState.category,
                                    mealTiming = formState.mealTiming,
                                    userId = userId,
                                    createdAt = getCurrentTimeMillis()
                                )
                                medicineRepository.addMedicine(medicine)
                                    .onSuccess {
                                        notificationScheduler.schedule(medicine)
                                        isLoading = false
                                        onNavigateBack()
                                    }
                                    .onFailure {
                                        isLoading = false
                                        errorMessage = "บันทึกไม่สำเร็จ: ${it.message}"
                                    }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuklanColors.Primary)
                ) {
                    if (isLoading) CircularProgressIndicator(color = LuklanColors.Primary, modifier = Modifier.size(24.dp))
                    else Text(if (step == maxStep) "เสร็จสิ้น" else "ต่อไป", style = LuklanTypography.buttonLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        var tempTime by remember { 
            mutableStateOf<String>(
                if (editingTimeIndex >= 0) formState.times[editingTimeIndex] else "08:00"
            ) 
        }
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newList = formState.times.toMutableList()
                    if (editingTimeIndex >= 0) {
                        newList[editingTimeIndex] = tempTime
                    } else {
                        newList.add(tempTime)
                    }
                    formState = formState.copy(times = newList.sorted())
                    showTimePicker = false
                }) { Text("ตกลง", color = LuklanColors.Primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("ยกเลิก") }
            },
            title = { Text(if (editingTimeIndex >= 0) "แก้ไขเวลา" else "เพิ่มเวลา", style = LuklanTypography.h3) },
            text = { WheelTimePicker(startTime = tempTime, onTimeSelected = { tempTime = it }) },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Custom Month/Year Dropdown Picker
    if (showMonthYearPicker) {
        var tempMonth by remember { mutableStateOf<Int>(displayMonth) }
        var tempYear by remember { mutableStateOf<Int>(displayYear) }
        
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
                    
                    val yearOptions = (now.year..now.year + 5).map { (it + 543).toString() }
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
fun StepName(state: MedicineFormState, onNext: () -> Unit, onUpdate: (MedicineFormState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ยาที่ใช้", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        TextField(
            value = state.name,
            onValueChange = { onUpdate(state.copy(name = it)) },
            placeholder = { Text("กรอกชื่อของยา", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onNext() })
        )
    }
}

@Composable
fun StepAmount(state: MedicineFormState, onNext: () -> Unit, onUpdate: (MedicineFormState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ปริมาณยาที่ใช้", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        TextField(
            value = state.amountPerDose,
            onValueChange = { onUpdate(state.copy(amountPerDose = it, dosage = it)) },
            placeholder = { Text("กรอกปริมาณของยา", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onNext() }),
            singleLine = true
        )
    }
}

@Composable
fun StepStartDate(
    state: MedicineFormState, 
    days: List<String>, 
    months: List<String>,
    displayMonth: Int,
    displayYear: Int,
    onShowPicker: () -> Unit,
    onUpdate: (MedicineFormState) -> Unit
) {
    val monthName = months[displayMonth - 1]
    val thaiYear = displayYear + 543
    
    // Calculate days in selected month
    val daysInMonth = when (displayMonth) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if ((displayYear % 4 == 0 && displayYear % 100 != 0) || (displayYear % 400 == 0)) 29 else 28
        else -> 31
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("เลือกวันเริ่มกินยา", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        
        val listState = rememberLazyListState()
        
        // Auto-scroll to selected date
        LaunchedEffect(state.startDate, displayMonth, displayYear) {
            val day = state.startDate.toIntOrNull() ?: 1
            if (day > 3) {
                listState.scrollToItem(day - 3)
            } else {
                listState.scrollToItem(0)
            }
        }

        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(daysInMonth) { i ->
                val dayNum = i + 1
                val isSelected = state.startDate == dayNum.toString()
                
                // Safe day calculation
                val dayName = try {
                    val date = LocalDate(displayYear, displayMonth, dayNum)
                    days[(date.dayOfWeek.ordinal + 1) % 7]
                } catch (e: Exception) {
                    ""
                }

                Column(
                    modifier = Modifier
                        .size(65.dp, 95.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (isSelected) LuklanColors.Secondary else LuklanColors.Primary)
                        .clickable { onUpdate(state.copy(startDate = dayNum.toString())) }
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(dayName, color = Color.White, fontSize = 16.sp)
                    Text(dayNum.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Month/Year Button
        Surface(
            modifier = Modifier.clickable { onShowPicker() },
            color = Color.Transparent
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$monthName $thaiYear", color = Color.White, style = LuklanTypography.h3)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Month/Year", tint = Color.White)
            }
        }
    }
}

@Composable
fun StepCategory(state: MedicineFormState, onUpdate: (MedicineFormState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ลักษณะของยา", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        
        val categories = listOf("แคปซูล", "เม็ด", "ฉีด", "อื่นๆ")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryCard(categories[0], "💊", state.category == categories[0]) { onUpdate(state.copy(category = categories[0])) }
                CategoryCard(categories[1], "💊", state.category == categories[1]) { onUpdate(state.copy(category = categories[1])) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryCard(categories[2], "💉", state.category == categories[2]) { onUpdate(state.copy(category = categories[2])) }
                CategoryCard(categories[3], "➕", state.category == categories[3]) { onUpdate(state.copy(category = categories[3])) }
            }
        }
    }
}

@Composable
fun CategoryCard(label: String, icon: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(150.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) LuklanColors.Secondary else Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(if (isSelected) Color.White.copy(0.2f) else LuklanColors.Secondary.copy(0.1f)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 36.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (isSelected) Color.White else LuklanColors.Primary)
        }
    }
}

@Composable
fun StepTime(state: MedicineFormState, mealOptions: List<String>, onAdd: () -> Unit, onEdit: (Int) -> Unit, onUpdate: (MedicineFormState) -> Unit) {
    @OptIn(ExperimentalLayoutApi::class)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("เวลาการกินยา", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White.copy(0.15f)).padding(horizontal = 20.dp, vertical = 10.dp).clickable {
                val next = if (state.mealTiming == mealOptions[0]) mealOptions[1] else mealOptions[0]
                onUpdate(state.copy(mealTiming = next))
            }
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(12.dp))
            Text(state.mealTiming, color = Color.White, style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
        }

        Spacer(Modifier.height(32.dp))
        
        state.times.forEachIndexed { index, t ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onEdit(index) },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = LuklanColors.Secondary)
            ) {
                Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$t น.", color = Color.White, style = LuklanTypography.h3, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        if (state.times.size < 6) {
            IconButton(
                onClick = onAdd,
                modifier = Modifier.padding(top = 16.dp).size(64.dp).clip(CircleShape).background(Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = LuklanColors.Secondary, modifier = Modifier.size(36.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepSummary(state: MedicineFormState, months: List<String>, displayMonth: Int, displayYear: Int) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SummaryItem("ชื่อยา", state.name)
        SummaryItem("ปริมาณ", "${state.amountPerDose} ${state.unit}")
        SummaryItem("วันที่เริ่มกินยา", "${state.startDate} ${months[displayMonth - 1]} ${displayYear + 543}")
        SummaryItem("ประเภทยา", state.category)
        
        Text("เวลากินยา", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, start = 12.dp), fontSize = 18.sp)
        FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.times.forEach { t ->
                Box(modifier = Modifier.padding(bottom = 8.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text("$t น.", fontWeight = FontWeight.Bold, color = LuklanColors.Primary, fontSize = 18.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(modifier: Modifier, horizontalArrangement: Arrangement.HorizontalOrVertical, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        content()
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp), fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Color.White).padding(horizontal = 24.dp, vertical = 14.dp)) {
            Text(value, style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Bold, color = LuklanColors.Primary, fontSize = 20.sp)
        }
    }
}
