package com.commu.luklan.ui.medicine

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
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
    val quantity: String = "",
    val unit: String = "เม็ด",
    val category: String = "",
    val expiryDate: String = "",
    val storageInstructions: String = "",
    val notes: String = "",
    val time: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MedicineFormFields(
    state: MedicineFormState,
    onStateChange: (MedicineFormState) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        onStateChange(state.copy(time = tempTime))
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
        OutlinedTextField(
            value = state.name,
            onValueChange = { onStateChange(state.copy(name = it)) },
            label = { Text("ชื่อยา *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text("รายละเอียด") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Dosage
        OutlinedTextField(
            value = state.dosage,
            onValueChange = { onStateChange(state.copy(dosage = it)) },
            label = { Text("ปริมาณต่อครั้ง (เช่น 1, 2, 1/2)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator
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

        // Quantity (Total amount available)
        OutlinedTextField(
            value = state.quantity,
            onValueChange = { 
                if (it.all { char -> char.isDigit() }) {
                    onStateChange(state.copy(quantity = it))
                }
            },
            label = { Text("จำนวนที่มี (ทั้งหมด)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator
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

        // Time Picker
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.time,
                onValueChange = {},
                readOnly = true,
                label = { Text("เวลาที่ต้องกิน *") },
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
                    unfocusedBorderColor = LuklanTheme.colors.Indicator
                )
            )
            Box(
                modifier = Modifier.matchParentSize().clickable {
                    focusManager.clearFocus()
                    showTimePicker = true
                }
            )
        }

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Frequency Dropdown
        DropdownSelector(
            label = "ความถี่ในการกิน",
            selectedValue = state.frequency,
            options = frequencyOptions,
            onValueChange = { onStateChange(state.copy(frequency = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Expiry Date Picker
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.expiryDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("วันหมดอายุ") },
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
                    unfocusedBorderColor = LuklanTheme.colors.Indicator
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
            label = { Text("วิธีเก็บรักษา") },
            placeholder = { Text("เช่น เก็บในที่เย็น, เก็บในตู้เย็น") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 2,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

        // Notes
        OutlinedTextField(
            value = state.notes,
            onValueChange = { onStateChange(state.copy(notes = it)) },
            label = { Text("หมายเหตุ") },
            placeholder = { Text("บันทึกเพิ่มเติม...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuklanTheme.colors.Surface,
                unfocusedContainerColor = LuklanTheme.colors.Surface,
                focusedBorderColor = LuklanTheme.colors.TextSecondary,
                unfocusedBorderColor = LuklanTheme.colors.Indicator
            )
        )

        Spacer(modifier = Modifier.height(LuklanTheme.spacing.lg))
    }
}
