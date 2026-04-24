package com.commu.luklan.ui.medicine

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getAuthRepository
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.components.WheelTimePicker
import com.commu.luklan.ui.theme.*
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.jetbrains.compose.resources.painterResource
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.*
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MedicineFormState(
    val name: String = "",
    val category: String = "",
    val dosage: String = "",
    val unit: String = "",
    val currentAmount: String = "",
    val startDate: String = "",
    val times: List<String> = emptyList(),
    val mealTiming: String = "",
    val mealTimingMinutes: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun AddMedicineScreen(
    targetUserId: String? = null,
    onNavigateBack: () -> Unit
) {
    val medicineRepository = remember { getMedicineRepository() }
    val authRepository = remember { getAuthRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()

    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    val thaiDays = listOf("อา", "จ", "อ", "พ", "พฤ", "ศ", "ส")
    val mealTimingOptions = listOf("ก่อนอาหาร", "หลังอาหาร", "พร้อมอาหาร", "ก่อนนอน")

    val now = remember {
        val nowMillis = getCurrentTimeMillis()
        val instant = Instant.fromEpochMilliseconds(nowMillis)
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    
    var formState by remember { 
        mutableStateOf(MedicineFormState(startDate = now.dayOfMonth.toString())) 
    }
    
    var displayMonth by remember { mutableStateOf(now.monthNumber) }
    var displayYear by remember { mutableStateOf(now.year) }
    var step by remember { mutableStateOf(1) }
    val maxStep = 6
    
    var showTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf(-1) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun canNavigateNext(): Boolean {
        return when (step) {
            1 -> formState.name.isNotBlank()
            2 -> formState.category.isNotBlank()
            3 -> {
                val d = formState.dosage.toDoubleOrNull() ?: 0.0
                val c = formState.currentAmount.toDoubleOrNull() ?: 0.0
                formState.dosage.isNotBlank() && formState.currentAmount.isNotBlank() && c >= d
            }
            4 -> formState.startDate.isNotBlank()
            5 -> formState.times.isNotEmpty() && formState.mealTiming.isNotBlank()
            else -> true
        }
    }

    Scaffold(containerColor = LuklanColors.Primary) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.weight(1f))
                Text("เพิ่มยา", style = LuklanTypography.h1, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1.3f))
            }
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when (step) {
                    1 -> StepName(formState, onNext = { if (canNavigateNext()) step += 1 }) { formState = it }
                    2 -> StepCategory(formState) { formState = it }
                    3 -> StepAmount(formState, onNext = { if (canNavigateNext()) step += 1 }) { formState = it }
                    4 -> StepStartDate(
                        state = formState, 
                        days = thaiDays, 
                        months = thaiMonths, 
                        dm = displayMonth, 
                        dy = displayYear, 
                        now = now, 
                        onMonthYearChange = { m, y -> displayMonth = m; displayYear = y },
                        onUpdate = { formState = it }
                    )
                    5 -> StepTime(formState, mealTimingOptions, onAdd = { editingTimeIndex = -1; showTimePicker = true }, onEdit = { editingTimeIndex = it; showTimePicker = true }) { formState = it }
                    6 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) { StepSummary(formState) { formState = it } }
                }
            }
            if (errorMessage != null) {
                Text(errorMessage!!, color = LuklanColors.Secondary, style = LuklanTypography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }
            Row(modifier = Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(maxStep) { i -> Box(modifier = Modifier.width(30.dp).height(6.dp).clip(CircleShape).background(if (i + 1 == step) LuklanColors.Secondary else Color.White.copy(0.3f))) }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (step > 1) Button(onClick = { step -= 1; errorMessage = null }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuklanColors.Primary)) { Text("ย้อนกลับ", style = LuklanTypography.buttonLarge) }
                Button(onClick = {
                    if (step < maxStep) {
                        if (!canNavigateNext()) {
                            val d = formState.dosage.toDoubleOrNull() ?: 0.0
                            val c = formState.currentAmount.toDoubleOrNull() ?: 0.0
                            errorMessage = if (step == 3 && formState.currentAmount.isNotBlank() && c < d) "ปริมาณยาที่มีต้องมากกว่าปริมาณที่ใช้" else "กรุณากรอกข้อมูลให้ครบถ้วน"
                        } else { errorMessage = null; step += 1 }
                    } else {
                        val userId = targetUserId ?: authRepository.getCurrentUserId() ?: return@Button
                        isLoading = true
                        scope.launch {
                            val fullDate = "$displayYear-${displayMonth.toString().padStart(2, '0')}-${formState.startDate.padStart(2, '0')}"
                            val med = Medicine(
                                id = Uuid.random().toString(), 
                                name = formState.name, 
                                dosage = formState.dosage,
                                unit = formState.unit,
                                times = formState.times, 
                                startDate = fullDate, 
                                category = formState.category, 
                                mealTiming = formState.mealTiming,
                                mealTimingMinutes = formState.mealTimingMinutes, 
                                currentAmount = formState.currentAmount,
                                userId = userId, 
                                createdAt = getCurrentTimeMillis()
                            )
                            medicineRepository.addMedicine(med).onSuccess { 
                                if (targetUserId == null) notificationScheduler.schedule(med)
                                isLoading = false; onNavigateBack() 
                            }.onFailure { isLoading = false; errorMessage = it.message }
                        }
                    }
                }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LuklanColors.Primary)) {
                    if (isLoading) CircularProgressIndicator(color = LuklanColors.Primary, modifier = Modifier.size(24.dp))
                    else Text(if (step == maxStep) "เสร็จสิ้น" else "ต่อไป", style = LuklanTypography.buttonLarge)
                }
            }
        }
    }

    if (showTimePicker) {
        var tempTime by remember { mutableStateOf(if (editingTimeIndex >= 0) formState.times[editingTimeIndex] else "08:00") }
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = { TextButton(onClick = {
                val newList = formState.times.toMutableList()
                if (editingTimeIndex >= 0) newList[editingTimeIndex] = tempTime else newList.add(tempTime)
                formState = formState.copy(times = newList.sorted()); showTimePicker = false
            }) { Text("ตกลง", color = LuklanColors.Primary, fontWeight = FontWeight.Bold) } },
            dismissButton = { 
                Row {
                    if (editingTimeIndex >= 0) {
                        TextButton(onClick = {
                            val newList = formState.times.toMutableList()
                            newList.removeAt(editingTimeIndex)
                            formState = formState.copy(times = newList)
                            showTimePicker = false
                        }) { Text("ลบเวลา", color = LuklanColors.Error) }
                    }
                    TextButton(onClick = { showTimePicker = false }) { Text("ยกเลิก") }
                }
            },
            title = { Text("เลือกเวลา", style = LuklanTypography.h3) },
            text = { WheelTimePicker(startTime = tempTime, onTimeSelected = { tempTime = it }) }
        )
    }
}

@Composable
fun StepName(state: MedicineFormState, onNext: () -> Unit, onUpdate: (MedicineFormState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ชื่อยา", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        TextField(value = state.name, onValueChange = { onUpdate(state.copy(name = it)) }, placeholder = { Text("กรอกชื่อของยา", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(30.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onNext() }))
    }
}

@Composable
fun StepCategory(state: MedicineFormState, onUpdate: (MedicineFormState) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
        Text("ลักษณะของยา", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        val categories = listOf(
            Triple("แคปซูล", Res.drawable.capsule, "แคปซูล"),
            Triple("เม็ด", Res.drawable.pill, "เม็ด"),
            Triple("น้ำ", Res.drawable.liquid, "ml"),
            Triple("ครีม", Res.drawable.cream, "หลอด"),
            Triple("เหน็บ", Res.drawable.suppository, "เม็ด"),
            Triple("ฉีด", Res.drawable.inject, "หลอด"),
            Triple("อื่นๆ", Res.drawable.other, "ชิ้น")
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CategoryCard(categories[0].first, categories[0].second, state.category == categories[0].first) { 
                        onUpdate(state.copy(category = categories[0].first, unit = categories[0].third))
                    }
                    CategoryCard(categories[1].first, categories[1].second, state.category == categories[1].first) { 
                        onUpdate(state.copy(category = categories[1].first, unit = categories[1].third))
                    }
                }
                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CategoryCard(categories[2].first, categories[2].second, state.category == categories[2].first) { 
                        onUpdate(state.copy(category = categories[2].first, unit = categories[2].third))
                    }
                    CategoryCard(categories[3].first, categories[3].second, state.category == categories[3].first) { 
                        onUpdate(state.copy(category = categories[3].first, unit = categories[3].third))
                    }
                }
                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CategoryCard(categories[4].first, categories[4].second, state.category == categories[4].first) { 
                        onUpdate(state.copy(category = categories[4].first, unit = categories[4].third))
                    }
                    CategoryCard(categories[5].first, categories[5].second, state.category == categories[5].first) { 
                        onUpdate(state.copy(category = categories[5].first, unit = categories[5].third))
                    }
                }
                // Row 4: Centered Last Item
                CategoryCard(categories[6].first, categories[6].second, state.category == categories[6].first) { 
                    onUpdate(state.copy(category = categories[6].first, unit = categories[6].third))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun StepAmount(state: MedicineFormState, onNext: () -> Unit, onUpdate: (MedicineFormState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("ปริมาณยาที่ใช้ต่อครั้ง", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(24.dp))
        
        // Dosage
        Row(modifier = Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = state.dosage,
                onValueChange = { 
                    val filtered = it.filter { c -> c.isDigit() || c == '.' }
                    if (filtered.count { c -> c == '.' } <= 1) {
                        onUpdate(state.copy(dosage = filtered))
                    }
                },
                placeholder = { Text("กรอกปริมาณที่ใช้", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = LuklanColors.Primary, unfocusedTextColor = LuklanColors.Primary),
                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true
            )
            
            var expanded by remember { mutableStateOf(false) }
            val unitOptions = listOf("เม็ด", "แคปซูล", "ช้อนชา", "ช้อนโต๊ะ", "ml", "หลอด", "กรัม", "แท่ง")
            
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Surface(modifier = Modifier.fillMaxSize().clickable { expanded = true }, shape = RoundedCornerShape(30.dp), color = Color.White) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(state.unit, color = LuklanColors.Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Icon(Icons.Default.ArrowDropDown, null, tint = LuklanColors.Primary)
                    }
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                    unitOptions.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt, color = LuklanColors.Primary) }, onClick = { onUpdate(state.copy(unit = opt)); expanded = false })
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text("ปริมาณยาที่มีทั้งหมด", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(24.dp))

        // Current Amount
        Row(modifier = Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = state.currentAmount,
                onValueChange = { 
                    val filtered = it.filter { c -> c.isDigit() || c == '.' }
                    if (filtered.count { c -> c == '.' } <= 1) {
                        onUpdate(state.copy(currentAmount = filtered))
                    }
                },
                placeholder = { Text("กรอกปริมาณที่มี", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = LuklanColors.Primary,
                    unfocusedTextColor = LuklanColors.Primary
                ),
                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onNext() }),
                singleLine = true
            )
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(state.unit, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun StepStartDate(state: MedicineFormState, days: List<String>, months: List<String>, dm: Int, dy: Int, now: LocalDateTime, onMonthYearChange: (Int, Int) -> Unit, onUpdate: (MedicineFormState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("เลือกวันเริ่มกินยา", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        
        val listState = rememberLazyListState()
        val daysInMonth = try {
            val firstDayOfNextMonth = if (dm == 12) LocalDate(dy + 1, 1, 1) else LocalDate(dy, dm + 1, 1)
            firstDayOfNextMonth.minus(DatePeriod(days = 1)).dayOfMonth
        } catch (e: Exception) { 31 }

        LaunchedEffect(dm, dy) {
            val d = state.startDate.toIntOrNull() ?: 1
            if (d > daysInMonth) onUpdate(state.copy(startDate = daysInMonth.toString()))
            if (d > 3) listState.scrollToItem(d - 3) else listState.scrollToItem(0)
        }

        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 24.dp)) {
            items(daysInMonth) { i ->
                val dayNum = i + 1
                val isSelected = state.startDate == dayNum.toString()
                val dayName = try { val date = LocalDate(dy, dm, dayNum); days[(date.dayOfWeek.ordinal + 1) % 7] } catch (e: Exception) { "" }
                val isToday = dayNum == now.dayOfMonth && dm == now.monthNumber && dy == now.year

                Column(
                    modifier = Modifier.size(65.dp, 95.dp).clip(RoundedCornerShape(32.dp))
                        .background(if (isSelected) LuklanColors.Secondary else Color.White.copy(0.15f))
                        .border(if (isSelected) 0.dp else 1.dp, Color.White.copy(0.3f), RoundedCornerShape(32.dp))
                        .clickable { onUpdate(state.copy(startDate = dayNum.toString())) }
                        .padding(vertical = 12.dp), 
                    verticalArrangement = Arrangement.Center, 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(dayName, color = Color.White, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    Text(dayNum.toString(), fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                    if (isToday) {
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        var showPicker by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clickable { showPicker = true }
        ) {
            Text("${months[dm-1]} ${dy + 543}", color = Color.White, style = LuklanTypography.h3, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }

        if (showPicker) {
            com.commu.luklan.ui.components.MonthYearPicker(
                initialMonth = dm,
                initialYear = dy,
                onDismiss = { showPicker = false },
                onConfirm = { m, y ->
                    onMonthYearChange(m, y)
                    showPicker = false 
                }
            )
        }
    }
}

@Composable
fun CategoryCard(label: String, icon: org.jetbrains.compose.resources.DrawableResource, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = if (isSelected) LuklanColors.Secondary else Color.White

    Box(modifier = Modifier.size(width = 150.dp, height = 150.dp), contentAlignment = Alignment.BottomCenter) {
        Card(
            onClick = onClick, 
            modifier = Modifier.fillMaxWidth().height(115.dp), 
            shape = RoundedCornerShape(24.dp), 
            colors = CardDefaults.cardColors(containerColor = if (isSelected) LuklanColors.Secondary else Color.White), 
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (isSelected) Color.White else LuklanColors.Primary, modifier = Modifier.padding(bottom = 35.dp))
            }
        }
        
        // Icon Frame
        Surface(
            modifier = Modifier.align(Alignment.TopCenter).size(80.dp),
            shape = CircleShape,
            color = borderColor, // Border color as outer color
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp) // Thicker border simulation
                    .background(LuklanColors.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icon), 
                    contentDescription = null, 
                    modifier = Modifier.size(50.dp).clickable(interactionSource = interactionSource, indication = null) { onClick() }
                )
            }
        }
    }
}

@Composable
fun StepTime(state: MedicineFormState, mealOptions: List<String>, onAdd: () -> Unit, onEdit: (Int) -> Unit, onUpdate: (MedicineFormState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("เวลาใช้ยา", style = LuklanTypography.h2, color = Color.White)
        Spacer(Modifier.height(32.dp))
        var expanded by remember { mutableStateOf(false) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Box {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White.copy(0.15f)).clickable { expanded = true }.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(if (state.mealTiming.isEmpty()) "เลือกเวลาการกินยา" else state.mealTiming, color = if (state.mealTiming.isEmpty()) Color.White.copy(0.6f) else Color.White, style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp)); Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                    mealOptions.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt, color = LuklanColors.Primary) }, onClick = {
                            val nextMinutes = if (opt == "ก่อนอาหาร" || opt == "หลังอาหาร") state.mealTimingMinutes else 0
                            onUpdate(state.copy(mealTiming = opt, mealTimingMinutes = nextMinutes)); expanded = false
                        })
                    }
                }
            }
            if (state.mealTiming.contains("อาหาร") && !state.mealTiming.contains("พร้อม")) {
                Spacer(Modifier.width(12.dp))
                TextField(value = if (state.mealTimingMinutes == 0) "" else state.mealTimingMinutes.toString(), onValueChange = { if (it.all { c -> c.isDigit() }) onUpdate(state.copy(mealTimingMinutes = it.toIntOrNull() ?: 0)) }, modifier = Modifier.width(85.dp).height(52.dp), shape = RoundedCornerShape(26.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = LuklanColors.Primary, unfocusedTextColor = LuklanColors.Primary), textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), placeholder = { Text("0", color = Color.LightGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) })
                Spacer(Modifier.width(8.dp)); Text("นาที", color = Color.White, style = LuklanTypography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(48.dp))
        state.times.forEachIndexed { i, t ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onEdit(i) }, shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = LuklanColors.Secondary)) {
                Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) { 
                    Text(text = "$t น.", color = Color.White, style = LuklanTypography.h3, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f)); Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(20.dp)) 
                }
            }
        }
        IconButton(onClick = onAdd, modifier = Modifier.padding(top = 16.dp).size(64.dp).clip(CircleShape).background(Color.White)) { 
            Icon(Icons.Default.Add, null, tint = LuklanColors.Secondary, modifier = Modifier.size(36.dp)) 
        }
    }
}

@Composable
fun StepSummary(state: MedicineFormState, onUpdate: (MedicineFormState) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MedicineFormFields(state = state, onUpdate = onUpdate)
    }
}
