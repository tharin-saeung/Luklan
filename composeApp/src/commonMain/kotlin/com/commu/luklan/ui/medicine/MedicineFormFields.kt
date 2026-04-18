package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
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
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    val category: String = "เม็ด",
    val mealTiming: String = "หลังอาหาร",
    val startDate: String = "",
    val expiryDate: String = "",
    val storageInstructions: String = "",
    val notes: String = "",
    val time: String = "",
    val times: List<String> = emptyList(),
    val selectedWeekDays: List<Int> = emptyList(),
    val selectedMonthDays: List<Int> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
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
    
    var showTimePicker by remember { mutableStateOf<Boolean>(false) }
    var showDatePicker by remember { mutableStateOf<Boolean>(false) }

    // Options for dropdowns
    val unitOptions = listOf("เม็ด", "แคปซูล", "ช้อนชา", "ช้อนโต๊ะ", "ml", "กล่อง", "ขวด", "หลอด")
    val categoryOptions = listOf("แก้ปวด", "แก้อักเสบ", "ลดไข้", "แก้แพ้", "หัวใจ", "ความดันโลหิต", "เบาหวาน", "ภูมิแพ้", "อื่นๆ")

    if (showTimePicker) {
        var tempTime by remember {
            mutableStateOf<String>(
                if (state.time.isNotBlank()) state.time
                else "08:00"
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
                        val newList = (state.times + tempTime).distinct().sorted()
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

        // Single dose field
        OutlinedTextField(
            value = state.amountPerDose,
            onValueChange = { v -> onStateChange(state.copy(amountPerDose = v, dosage = v)) },
            label = { Text("ครั้งละ (เช่น 1)", color = LuklanTheme.colors.Secondary) },
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

        // Unit Dropdown
        DropdownSelector(
            label = "หน่วย",
            selectedValue = state.unit,
            options = unitOptions,
            onValueChange = { onStateChange(state.copy(unit = it)) },
            modifier = Modifier.fillMaxWidth()
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

        // Frequency controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.frequencyCount.toString(),
                onValueChange = { v ->
                    if (v.all { it.isDigit() }) {
                        onStateChange(state.copy(frequencyCount = v.toIntOrNull() ?: 0))
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
                onValueChange = { onStateChange(state.copy(timeUnit = it)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Meal Timing
        DropdownSelector(
            label = "เวลากิน",
            selectedValue = state.mealTiming,
            options = listOf("หลังอาหาร", "ก่อนอาหาร"),
            onValueChange = { onStateChange(state.copy(mealTiming = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Time Slots
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("เวลาที่ต้องกิน", color = LuklanTheme.colors.Secondary, style = LuklanTypography.bodyMedium)
            state.times.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(modifier = Modifier.weight(1f).background(LuklanTheme.colors.Surface, RoundedCornerShape(12.dp)).padding(12.dp)) {
                        Text(text = t, color = LuklanTheme.colors.TextPrimary)
                    }
                    IconButton(onClick = {
                        val newList = state.times.filter { it != t }
                        onStateChange(state.copy(times = newList, time = newList.firstOrNull() ?: ""))
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = LuklanColors.Error)
                    }
                }
            }
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("เพิ่มเวลา")
            }
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Expiry Date
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.expiryDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("วันหมดอายุ", color = LuklanTheme.colors.Secondary) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = LuklanTheme.colors.Surface, unfocusedContainerColor = LuklanTheme.colors.Surface)
            )
            Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.lg))
    }
}
