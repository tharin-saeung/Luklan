package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.ui.components.WheelTimePicker
import com.commu.luklan.ui.components.FullDatePicker
import com.commu.luklan.ui.components.MedicineIcon
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTypography
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MedicineFormFields(
    state: MedicineFormState,
    onUpdate: (MedicineFormState) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf(-1) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var showMealTimingPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val thaiMonths = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Name
        SummaryItemEditable(
            label = "ชื่อยา",
            value = state.name,
            onValueChange = { onUpdate(state.copy(name = it)) }
        )

        // Category
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text("ลักษณะ", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            Surface(
                onClick = { showCategoryPicker = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (state.category.isNotEmpty()) {
                        MedicineIcon(category = state.category, iconSize = 28.dp)
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(state.category.ifEmpty { "เลือกประเภท" }, color = if (state.category.isEmpty()) Color.Gray else LuklanColors.Primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, null, tint = LuklanColors.Primary)
                }
            }
        }

        // Dosage and Unit
        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ปริมาณที่กิน", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                TextField(
                    value = state.dosage,
                    onValueChange = { 
                        val filtered = it.filter { c -> c.isDigit() || c == '.' }
                        if (filtered.count { c -> c == '.' } <= 1) {
                            onUpdate(state.copy(dosage = filtered))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = LuklanColors.Primary, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("หน่วย", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                Surface(
                    onClick = { showUnitPicker = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 0.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(state.unit.ifEmpty { "เลือกหน่วย" }, color = if (state.unit.isEmpty()) Color.Gray else LuklanColors.Primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null, tint = LuklanColors.Primary)
                    }
                }
            }
        }

        // Current Amount
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text("ปริมาณยาที่มี", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            TextField(
                value = state.currentAmount,
                onValueChange = { 
                    val filtered = it.filter { c -> c.isDigit() || c == '.' }
                    if (filtered.count { c -> c == '.' } <= 1) {
                        onUpdate(state.copy(currentAmount = filtered))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(32.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = LuklanColors.Primary, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Text(state.unit, color = Color.Gray, modifier = Modifier.padding(end = 20.dp)) }
            )
        }

        // Start Date
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text("วันเริ่มกินยา", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            Surface(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                val dateDisplay = try {
                    val parts = state.startDate.split("-")
                    if (parts.size == 3) {
                        "${parts[2]} ${thaiMonths[parts[1].toInt() - 1]} ${parts[0].toInt() + 543}"
                    } else state.startDate
                } catch (e: Exception) { state.startDate }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(dateDisplay.ifEmpty { "เลือกวันที่" }, color = if (state.startDate.isEmpty()) Color.Gray else LuklanColors.Primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.CalendarToday, null, tint = LuklanColors.Primary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Meal Timing
        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text("ช่วงเวลาที่ใช้ยา", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                Surface(
                    onClick = { showMealTimingPicker = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 0.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(state.mealTiming, color = LuklanColors.Primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null, tint = LuklanColors.Primary)
                    }
                }
            }
            if (state.mealTiming.contains("อาหาร") && !state.mealTiming.contains("พร้อม")) {
                Column(modifier = Modifier.weight(0.8f)) {
                    Text("นาที", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                    TextField(
                        value = state.mealTimingMinutes.toString(),
                        onValueChange = { if (it.all { c -> c.isDigit() }) onUpdate(state.copy(mealTimingMinutes = it.toIntOrNull() ?: 0)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(color = LuklanColors.Primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        // Times to take
        Text("เวลาใช้ยา", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, start = 12.dp))
        FlowRow(modifier = Modifier.padding(top = 4.dp)) {
            state.times.forEachIndexed { index, t ->
                Surface(
                    onClick = { editingTimeIndex = index; showTimePicker = true },
                    modifier = Modifier.padding(4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("$t น.", color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, null, tint = LuklanColors.Primary.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    }
                }
            }
            IconButton(
                onClick = { editingTimeIndex = -1; showTimePicker = true },
                modifier = Modifier.padding(4.dp).size(40.dp).clip(CircleShape).background(Color.White.copy(0.3f))
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    }

    // Pickers
    if (showTimePicker) {
        var tempTime by remember { mutableStateOf(if (editingTimeIndex >= 0) state.times[editingTimeIndex] else "08:00") }
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = { TextButton(onClick = {
                val newList = state.times.toMutableList()
                if (editingTimeIndex >= 0) newList[editingTimeIndex] = tempTime else newList.add(tempTime)
                onUpdate(state.copy(times = newList.sorted())); showTimePicker = false
            }) { Text("ตกลง", color = LuklanColors.Primary, fontWeight = FontWeight.Bold) } },
            dismissButton = { 
                Row {
                    if (editingTimeIndex >= 0) {
                        TextButton(onClick = {
                            val newList = state.times.toMutableList()
                            newList.removeAt(editingTimeIndex)
                            onUpdate(state.copy(times = newList))
                            showTimePicker = false
                        }) { Text("ลบเวลา", color = Color.Red) }
                    }
                    TextButton(onClick = { showTimePicker = false }) { Text("ยกเลิก") }
                }
            },
            title = { Text("เลือกเวลา") },
            text = { WheelTimePicker(startTime = tempTime, onTimeSelected = { tempTime = it }) }
        )
    }

    if (showUnitPicker) {
        val unitOptions = listOf("เม็ด", "แคปซูล", "ช้อนชา", "ช้อนโต๊ะ", "ml", "หลอด", "กรัม", "แท่ง")
        AlertDialog(
            onDismissRequest = { showUnitPicker = false },
            title = { Text("เลือกหน่วย") },
            text = {
                Column {
                    unitOptions.forEach { opt ->
                        TextButton(onClick = { onUpdate(state.copy(unit = opt)); showUnitPicker = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(opt, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showMealTimingPicker) {
        val options = listOf("ก่อนอาหาร", "หลังอาหาร", "พร้อมอาหาร", "ก่อนนอน")
        AlertDialog(
            onDismissRequest = { showMealTimingPicker = false },
            title = { Text("เลือกเวลากิน") },
            text = {
                Column {
                    options.forEach { opt ->
                        TextButton(onClick = { 
                            val nextMinutes = if (opt == "ก่อนอาหาร" || opt == "หลังอาหาร") state.mealTimingMinutes else 0
                            onUpdate(state.copy(mealTiming = opt, mealTimingMinutes = nextMinutes))
                            showMealTimingPicker = false 
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(opt, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showCategoryPicker) {
        val options = listOf("แคปซูล", "เม็ด", "น้ำ", "ครีม", "เหน็บ", "ฉีด", "อื่นๆ")
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("เลือกลักษณะ") },
            text = {
                Column {
                    options.forEach { opt ->
                        TextButton(
                            onClick = { onUpdate(state.copy(category = opt)); showCategoryPicker = false }, 
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                MedicineIcon(category = opt, iconSize = 32.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(opt, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showDatePicker) {
        val nowMillis = getCurrentTimeMillis()
        val nowInstant = Instant.fromEpochMilliseconds(nowMillis)
        val nowDateTime = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        val todayIso = "${nowDateTime.year}-${nowDateTime.monthNumber.toString().padStart(2, '0')}-${nowDateTime.dayOfMonth.toString().padStart(2, '0')}"
        
        val initialPickerDate = if (state.startDate.contains("-")) state.startDate else todayIso

        FullDatePicker(
            initialDate = initialPickerDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { finalDate ->
                onUpdate(state.copy(startDate = finalDate))
                showDatePicker = false
            }
        )
    }
}

@Composable
fun SummaryItemEditable(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() }
    )
}
