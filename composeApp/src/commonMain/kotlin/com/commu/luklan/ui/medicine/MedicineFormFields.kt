package com.commu.luklan.ui.medicine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.commu.luklan.ui.components.DatePickerDialog
import com.commu.luklan.ui.components.DropdownSelector
import com.commu.luklan.ui.components.WheelTimePicker
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class MedicineFormState(
    val name: String = "",
    val dosage: String = "",
    val description: String = "",
    val frequency: String = "ทุกวัน",
    val timeUnit: String = "วัน",
    val frequencyCount: Int = 1,
    val amountPerDose: String = "",
    val quantity: String = "",
    val unit: String = "เม็ด",
    val category: String = "",
    val expiryDate: String = "",
    val storageInstructions: String = "",
    val notes: String = "",
    val time: String = "",
    val times: List<String> = emptyList(),
    val selectedWeekDays: List<Int> = emptyList(),
    val selectedMonthDays: List<Int> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MedicineFormFields(
    state: MedicineFormState,
    onStateChange: (MedicineFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    @Composable
    fun FieldLabel(text: String) {
        Text(text = text, color = LuklanTheme.colors.Secondary, style = LuklanTypography.bodyMedium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
    }
    val focusManager = LocalFocusManager.current
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Options for dropdowns
    val unitOptions = listOf("เม็ด", "แคปซูล", "ช้อนชา", "ช้อนโต๊ะ", "ml", "กล่อง", "ขวด", "หลอด")
    val frequencyOptions = listOf("ทุกวัน", "วันเว้นวัน", "สัปดาห์ละ 2 ครั้ง", "สัปดาห์ละ 3 ครั้ง", "เดือนละครั้ง")
    val categoryOptions = listOf("แก้ปวด", "แก้อักเสบ", "ลดไข้", "แก้แพ้", "หัวใจ", "ความดันโลหิต", "เบาหวาน", "ภูมิแพ้", "อื่นๆ")

    if (showTimePicker) {
        var tempTime by remember {
            mutableStateOf(
                if (state.time.isNotBlank()) state.time
                else {
                    val nowMillis = Clock.System.now().toEpochMilliseconds()
                    val instant = Instant.fromEpochMilliseconds(nowMillis)
                    val timeZone = TimeZone.currentSystemDefault()
                    val localDateTime = instant.toLocalDateTime(timeZone)
                    "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
                }
            )
        }
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("เลือกเวลา", style = LuklanTypography.h3) },
            text = {
                WheelTimePicker(
                    startTime = tempTime,
                    onTimeSelected = { selectedTime -> tempTime = selectedTime }
                )
            },
                confirmButton = {
                TextButton(
                    onClick = {
                        // Append selected time (avoid duplicates) and ensure primary time set
                        val newList = (state.times + tempTime).distinct()
                        val primary = newList.firstOrNull() ?: tempTime
                        onStateChange(state.copy(time = primary, times = newList))
                        showTimePicker = false
                    }
                ) { Text("ตกลง") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("ยกเลิก") }
            },
            containerColor = LuklanTheme.colors.Surface,
            titleContentColor = LuklanTheme.colors.TextPrimary,
            textContentColor = LuklanTheme.colors.TextPrimary
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = state.expiryDate,
            onDateSelected = { selectedDate ->
                onStateChange(state.copy(expiryDate = selectedDate))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Medicine Name
        FieldLabel("ชื่อยา *")
        OutlinedTextField(
            value = state.name,
            onValueChange = { onStateChange(state.copy(name = it)) },
            placeholder = { Text("กรอกชื่อยา", color = LuklanTheme.colors.TextSecondary) },
            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                cursorColor = LuklanTheme.colors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Description
        FieldLabel("รายละเอียด")
        OutlinedTextField(
            value = state.description,
            onValueChange = { onStateChange(state.copy(description = it)) },
            placeholder = { Text("รายละเอียด", color = LuklanTheme.colors.TextSecondary) },
            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                cursorColor = LuklanTheme.colors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Single dose field: keep `amountPerDose` as canonical and keep `dosage` in sync
        OutlinedTextField(
            value = state.amountPerDose,
            onValueChange = { v -> onStateChange(state.copy(amountPerDose = v, dosage = v)) },
            label = { Text("ครั้งละ (เช่น 1 เม็ด)", color = LuklanTheme.colors.Secondary) },
            placeholder = { Text("กรอกปริมาณต่อครั้ง", color = LuklanTheme.colors.TextSecondary) },
            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                focusedLabelColor = LuklanTheme.colors.Secondary,
                unfocusedLabelColor = LuklanTheme.colors.Secondary,
                cursorColor = LuklanTheme.colors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))
        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Unit Dropdown
        DropdownSelector(
            label = "หน่วย",
            selectedValue = state.unit,
            options = unitOptions,
            onValueChange = { onStateChange(state.copy(unit = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Quantity (Total amount available)
        OutlinedTextField(
            value = state.quantity,
            onValueChange = { 
                if (it.all { char -> char.isDigit() }) {
                    onStateChange(state.copy(quantity = it))
                }
            },
            label = { Text("จำนวนที่มี (ทั้งหมด)", color = LuklanTheme.colors.Secondary) },
            placeholder = { Text("กรอกจำนวนที่มี", color = LuklanTheme.colors.TextSecondary) },
            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                focusedLabelColor = LuklanTheme.colors.Secondary,
                unfocusedLabelColor = LuklanTheme.colors.Secondary,
                cursorColor = LuklanTheme.colors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Category Dropdown
        DropdownSelector(
            label = "ประเภทยา",
            selectedValue = state.category,
            options = categoryOptions,
            onValueChange = { onStateChange(state.copy(category = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Frequency controls (move above times) - linked to times behavior
        // When user changes frequencyCount or timeUnit we adjust `times` entries accordingly
        fun adjustTimesForFrequency(fCount: Int, tUnit: String, currentTimes: List<String>): List<String> {
            val result = currentTimes.toMutableList()
            if (tUnit == "วัน") {
                // keep only times (HH:MM). ensure size == fCount
                while (result.size < fCount) result.add("")
                while (result.size > fCount) result.removeAt(result.lastIndex)
            } else if (tUnit == "สัปดาห์") {
                // store entries as DAY@HH:MM, default day = จันทร์
                while (result.size < fCount) result.add("จันทร์@")
                while (result.size > fCount) result.removeAt(result.lastIndex)
            } else if (tUnit == "เดือน") {
                // store entries as DATE@HH:MM (date number)
                while (result.size < fCount) result.add("1@")
                while (result.size > fCount) result.removeAt(result.lastIndex)
            }
            return result
        }
    
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.frequencyCount.toString(),
                onValueChange = { v ->
                    if (v.all { it.isDigit() } && v.length <= 3) {
                        val newCount = v.toIntOrNull() ?: 0
                        val newTimes = adjustTimesForFrequency(newCount, state.timeUnit, state.times)
                        onStateChange(state.copy(frequencyCount = newCount, times = newTimes))
                    }
                },
                label = { Text("จำนวน", color = LuklanTheme.colors.Secondary) },
                modifier = Modifier.width(96.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(text = "ครั้งต่อ", modifier = Modifier.padding(start = 4.dp, end = 4.dp))

            DropdownSelector(
                label = "",
                selectedValue = state.timeUnit,
                options = listOf("วัน", "สัปดาห์", "เดือน"),
                onValueChange = { newUnit ->
                    val newTimes = adjustTimesForFrequency(state.frequencyCount, newUnit, state.times)
                    onStateChange(state.copy(timeUnit = newUnit, times = newTimes))
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Time Picker + multiple times display
        Column(modifier = Modifier.fillMaxWidth()) {
            // Manage editing index/time for the picker
            var editingIndex by remember { mutableStateOf(-1) }
            var editingTempTime by remember { mutableStateOf("") }

            val daysOfWeek = listOf("จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์", "อาทิตย์")

            if (state.timeUnit == "วัน") {
                // Daily: show times as chips in order; user edits each time
                if (state.times.isNotEmpty()) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((index, t) in state.times.withIndex()) {
                            OutlinedButton(onClick = {
                                // edit this time
                                editingIndex = index
                                editingTempTime = if (t.isNotBlank()) t else "08:00"
                                showTimePicker = true
                            }) {
                                Text(if (t.isNotBlank()) t else "(ตั้งเวลา)")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(LuklanTheme.spacing.sm))
                }

                // Read-only display of times joined
                OutlinedTextField(
                    value = if (state.times.isNotEmpty()) state.times.joinToString(", ") else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("เวลาที่ต้องกิน", color = LuklanTheme.colors.Secondary) },
                    placeholder = { Text("--:--", color = LuklanTheme.colors.TextSecondary) },
                    textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                    trailingIcon = {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Select Time"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LuklanTheme.colors.Surface,
                        unfocusedContainerColor = LuklanTheme.colors.Surface,
                        focusedBorderColor = LuklanTheme.colors.TextSecondary,
                        unfocusedBorderColor = LuklanTheme.colors.Indicator,
                        focusedLabelColor = LuklanTheme.colors.Secondary,
                        unfocusedLabelColor = LuklanTheme.colors.Secondary,
                        cursorColor = LuklanTheme.colors.TextPrimary
                    )
                )

                // When picker confirms, update the specific index (if editing) or append
                if (showTimePicker) {
                    // reuse existing dialog code: show wheel and update
                    // We'll present a temporary inline picker via AlertDialog
                    var tempTime by remember { mutableStateOf(if (editingTempTime.isNotBlank()) editingTempTime else "08:00") }
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false; editingIndex = -1 },
                        title = { Text("เลือกเวลา", style = LuklanTypography.h3) },
                        text = { com.commu.luklan.ui.components.WheelTimePicker(startTime = tempTime, onTimeSelected = { tempTime = it }) },
                        confirmButton = {
                            TextButton(onClick = {
                                val newList = state.times.toMutableList()
                                if (editingIndex >= 0 && editingIndex < newList.size) {
                                    newList[editingIndex] = tempTime
                                } else {
                                    newList.add(tempTime)
                                }
                                val primary = newList.firstOrNull() ?: tempTime
                                onStateChange(state.copy(times = newList, time = primary))
                                showTimePicker = false
                                editingIndex = -1
                            }) { Text("ตกลง") }
                        },
                        dismissButton = { TextButton(onClick = { showTimePicker = false; editingIndex = -1 }) { Text("ยกเลิก") } },
                        containerColor = LuklanTheme.colors.Surface,
                        titleContentColor = LuklanTheme.colors.TextPrimary,
                        textContentColor = LuklanTheme.colors.TextPrimary
                    )
                }

            } else if (state.timeUnit == "สัปดาห์") {
                // Weekly: show N rows of (day dropdown + time)
                for (i in 0 until state.frequencyCount.coerceAtLeast(1)) {
                    val entry = state.times.getOrNull(i) ?: "จันทร์@"
                    val parts = entry.split('@')
                    val day = parts.getOrNull(0)?.ifEmpty { "จันทร์" } ?: "จันทร์"
                    val time = parts.getOrNull(1) ?: ""
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        DropdownSelector(
                            label = "วัน",
                            selectedValue = day,
                            options = daysOfWeek,
                            onValueChange = { newDay ->
                                val newTimes = state.times.toMutableList()
                                val t = newTimes.getOrNull(i) ?: ""
                                val tm = t.split('@').getOrNull(1) ?: ""
                                val composed = "${newDay}@${tm}"
                                if (i < newTimes.size) newTimes[i] = composed else newTimes.add(composed)
                                onStateChange(state.copy(times = newTimes))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = if (time.isNotBlank()) time else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("เวลา", color = LuklanTheme.colors.Secondary) },
                            placeholder = { Text("--:--", color = LuklanTheme.colors.TextSecondary) },
                            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuklanTheme.colors.Surface,
                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                                focusedLabelColor = LuklanTheme.colors.Secondary,
                                unfocusedLabelColor = LuklanTheme.colors.Secondary,
                                cursorColor = LuklanTheme.colors.TextPrimary
                            )
                        )
                        Box(modifier = Modifier.size(40.dp).clickable {
                            // edit this time
                            val tmp = if (time.isNotBlank()) time else "08:00"
                            editingTempTime = tmp
                            editingIndex = i
                            showTimePicker = true
                        }) { }
                    }
                    Spacer(modifier = Modifier.height(LuklanTheme.spacing.sm))
                }

                if (showTimePicker) {
                    var tempTime by remember { mutableStateOf(if (editingTempTime.isNotBlank()) editingTempTime else "08:00") }
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false; editingIndex = -1 },
                        title = { Text("เลือกเวลา", style = LuklanTypography.h3) },
                        text = { com.commu.luklan.ui.components.WheelTimePicker(startTime = tempTime, onTimeSelected = { tempTime = it }) },
                        confirmButton = {
                            TextButton(onClick = {
                                val newTimes = state.times.toMutableList()
                                val existingDay = newTimes.getOrNull(editingIndex)?.split('@')?.getOrNull(0) ?: "จันทร์"
                                val composed = "${existingDay}@${tempTime}"
                                if (editingIndex >= 0 && editingIndex < newTimes.size) newTimes[editingIndex] = composed else newTimes.add(composed)
                                onStateChange(state.copy(times = newTimes))
                                showTimePicker = false
                                editingIndex = -1
                            }) { Text("ตกลง") }
                        },
                        dismissButton = { TextButton(onClick = { showTimePicker = false; editingIndex = -1 }) { Text("ยกเลิก") } },
                        containerColor = LuklanTheme.colors.Surface,
                        titleContentColor = LuklanTheme.colors.TextPrimary,
                        textContentColor = LuklanTheme.colors.TextPrimary
                    )
                }

            } else if (state.timeUnit == "เดือน") {
                // Monthly: each entry is DATE@HH:MM
                for (i in 0 until state.frequencyCount.coerceAtLeast(1)) {
                    val entry = state.times.getOrNull(i) ?: "1@"
                    val parts = entry.split('@')
                    val dateStr = parts.getOrNull(0) ?: "1"
                    val time = parts.getOrNull(1) ?: ""
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // date selector (1..31)
                        val dateOptions = (1..31).map { it.toString() }
                        DropdownSelector(
                            label = "วันที่",
                            selectedValue = dateStr,
                            options = dateOptions,
                            onValueChange = { newDate ->
                                val newTimes = state.times.toMutableList()
                                val tm = newTimes.getOrNull(i)?.split('@')?.getOrNull(1) ?: ""
                                val composed = "${newDate}@${tm}"
                                if (i < newTimes.size) newTimes[i] = composed else newTimes.add(composed)
                                onStateChange(state.copy(times = newTimes))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = if (time.isNotBlank()) time else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("เวลา", color = LuklanTheme.colors.Secondary) },
                            placeholder = { Text("--:--", color = LuklanTheme.colors.TextSecondary) },
                            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuklanTheme.colors.Surface,
                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                                focusedLabelColor = LuklanTheme.colors.Secondary,
                                unfocusedLabelColor = LuklanTheme.colors.Secondary,
                                cursorColor = LuklanTheme.colors.TextPrimary
                            )
                        )
                        Box(modifier = Modifier.size(40.dp).clickable {
                            val tmp = if (time.isNotBlank()) time else "08:00"
                            editingTempTime = tmp
                            editingIndex = i
                            showTimePicker = true
                        }) { }
                    }
                    Spacer(modifier = Modifier.height(LuklanTheme.spacing.sm))
                }

                if (showTimePicker) {
                    var tempTime by remember { mutableStateOf(if (editingTempTime.isNotBlank()) editingTempTime else "08:00") }
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false; editingIndex = -1 },
                        title = { Text("เลือกเวลา", style = LuklanTypography.h3) },
                        text = { com.commu.luklan.ui.components.WheelTimePicker(startTime = tempTime, onTimeSelected = { tempTime = it }) },
                        confirmButton = {
                            TextButton(onClick = {
                                val newTimes = state.times.toMutableList()
                                val existingDate = newTimes.getOrNull(editingIndex)?.split('@')?.getOrNull(0) ?: "1"
                                val composed = "${existingDate}@${tempTime}"
                                if (editingIndex >= 0 && editingIndex < newTimes.size) newTimes[editingIndex] = composed else newTimes.add(composed)
                                onStateChange(state.copy(times = newTimes))
                                showTimePicker = false
                                editingIndex = -1
                            }) { Text("ตกลง") }
                        },
                        dismissButton = { TextButton(onClick = { showTimePicker = false; editingIndex = -1 }) { Text("ยกเลิก") } },
                        containerColor = LuklanTheme.colors.Surface,
                        titleContentColor = LuklanTheme.colors.TextPrimary,
                        textContentColor = LuklanTheme.colors.TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Frequency: split into count + unit for flexible rules
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.frequencyCount.toString(),
                onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 3) onStateChange(state.copy(frequencyCount = v.toIntOrNull() ?: 0)) },
                label = { Text("จำนวนครั้ง", color = LuklanTheme.colors.Secondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuklanTheme.colors.Surface,
                    unfocusedContainerColor = LuklanTheme.colors.Surface,
                    focusedBorderColor = LuklanTheme.colors.TextSecondary,
                    unfocusedBorderColor = LuklanTheme.colors.Indicator,
                    focusedLabelColor = LuklanTheme.colors.Secondary,
                    unfocusedLabelColor = LuklanTheme.colors.Secondary,
                    cursorColor = LuklanTheme.colors.TextPrimary
                )
            )

            DropdownSelector(
                label = "หน่วยเวลา",
                selectedValue = state.timeUnit,
                options = listOf("วัน", "สัปดาห์", "เดือน"),
                onValueChange = { onStateChange(state.copy(timeUnit = it)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Expiry Date Picker
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.expiryDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("วันหมดอายุ", color = LuklanTheme.colors.Secondary) },
                placeholder = { Text("เลือกวันหมดอายุ", color = LuklanTheme.colors.TextSecondary) },
                textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                trailingIcon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuklanTheme.colors.Surface,
                    unfocusedContainerColor = LuklanTheme.colors.Surface,
                    focusedBorderColor = LuklanTheme.colors.TextSecondary,
                    unfocusedBorderColor = LuklanTheme.colors.Indicator,
                    focusedLabelColor = LuklanTheme.colors.Secondary,
                    unfocusedLabelColor = LuklanTheme.colors.Secondary,
                    cursorColor = LuklanTheme.colors.TextPrimary
                )
            )
            Box(
                modifier = Modifier.matchParentSize().clickable {
                    focusManager.clearFocus()
                    showDatePicker = true
                }
            )
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Storage Instructions
        OutlinedTextField(
            value = state.storageInstructions,
            onValueChange = { onStateChange(state.copy(storageInstructions = it)) },
            label = { Text("วิธีเก็บรักษา", color = LuklanTheme.colors.Secondary) },
            placeholder = { Text("เช่น เก็บในที่เย็น, เก็บในตู้เย็น", color = LuklanTheme.colors.TextSecondary) },
            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 2,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                focusedLabelColor = LuklanTheme.colors.Secondary,
                unfocusedLabelColor = LuklanTheme.colors.Secondary,
                cursorColor = LuklanTheme.colors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Notes
        OutlinedTextField(
            value = state.notes,
            onValueChange = { onStateChange(state.copy(notes = it)) },
            label = { Text("หมายเหตุ", color = LuklanTheme.colors.Secondary) },
            placeholder = { Text("บันทึกเพิ่มเติม...", color = LuklanTheme.colors.TextSecondary) },
            textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator,
                focusedLabelColor = LuklanTheme.colors.Secondary,
                unfocusedLabelColor = LuklanTheme.colors.Secondary,
                cursorColor = LuklanTheme.colors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.lg))
    }
}
