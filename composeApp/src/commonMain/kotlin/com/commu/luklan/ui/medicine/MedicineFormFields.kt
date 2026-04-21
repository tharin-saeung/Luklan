package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTypography

data class MedicineFormState(
    val name: String = "",
    val dosage: String = "",
    val unit: String = "",
    val times: List<String> = emptyList(),
    val startDate: String = "",
    val category: String = "",
    val mealTiming: String = "ก่อนอาหาร",
    val mealTimingMinutes: Int = 15,
    val expiryDate: String = ""
)

@Composable
fun MedicineFormFields(
    state: MedicineFormState,
    onUpdate: (MedicineFormState) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf(-1) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var showMealTimingPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Name
        SummaryItemEditable(
            label = "ชื่อยา",
            value = state.name,
            onValueChange = { onUpdate(state.copy(name = it)) }
        )

        // Dosage and Unit
        Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ปริมาณ", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                TextField(
                    value = state.dosage,
                    onValueChange = { 
                        val filtered = it.filter { c -> c.isDigit() || c == '.' }
                        if (filtered.count { c -> c == '.' } <= 1) {
                            onUpdate(state.copy(dosage = filtered))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(24.dp),
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { showUnitPicker = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(state.unit, color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Meal Timing
        Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text("เวลากิน", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { showMealTimingPicker = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(state.mealTiming, color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
                }
            }
            
            if (state.mealTiming.contains("อาหาร") && !state.mealTiming.contains("พร้อม")) {
                Column(modifier = Modifier.weight(0.8f)) {
                    Text("นาที", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                    TextField(
                        value = state.mealTimingMinutes.toString(),
                        onValueChange = { if (it.all { c -> c.isDigit() }) onUpdate(state.copy(mealTimingMinutes = it.toIntOrNull() ?: 0)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(24.dp),
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

        // Category
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text("ลักษณะ", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable { showCategoryPicker = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(state.category, color = LuklanColors.Primary, fontWeight = FontWeight.Bold)
            }
        }

        // Times to take
        Text("เวลาที่ต้องกิน", color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, start = 12.dp))
        FlowRow(modifier = Modifier.padding(8.dp)) { 
            state.times.forEachIndexed { index, t -> 
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .clickable { editingTimeIndex = index; showTimePicker = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) { 
                    Text("$t น.", color = LuklanColors.Primary, fontWeight = FontWeight.Bold) 
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
                if (editingTimeIndex >= 0) {
                    TextButton(onClick = {
                        val newList = state.times.toMutableList()
                        newList.removeAt(editingTimeIndex)
                        onUpdate(state.copy(times = newList)); showTimePicker = false
                    }) { Text("ลบ", color = Color.Red) }
                }
                TextButton(onClick = { showTimePicker = false }) { Text("ยกเลิก") } 
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
        val options = listOf("แคปซูล", "เม็ด", "ฉีด", "อื่นๆ")
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("เลือกลักษณะ") },
            text = {
                Column {
                    options.forEach { opt ->
                        TextButton(onClick = { onUpdate(state.copy(category = opt)); showCategoryPicker = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(opt, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun SummaryItemEditable(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = LuklanColors.Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
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
