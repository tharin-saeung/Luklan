package com.commu.luklan.ui.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
 
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.commu.luklan.ui.ocr.OcrResultStore
import com.commu.luklan.ui.components.DropdownSelector
import com.commu.luklan.ui.components.WheelTimePicker
import com.commu.luklan.ui.components.DatePickerDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun AddMedicineScreen(onNavigateBack: () -> Unit) {
        val medicineRepository = remember { getMedicineRepository() }
        val authRepository = remember { AuthRepository() }
        val notificationScheduler = remember { getNotificationScheduler() }
        val scope = rememberCoroutineScope()

                val focusManager = LocalFocusManager.current
                var formState by remember { mutableStateOf(MedicineFormState()) }
                // If OCR produced a form, load it and clear the store (harmless if OCR disabled)
                LaunchedEffect(Unit) {
                        OcrResultStore.lastForm?.let {
                                formState = it
                                OcrResultStore.lastForm = null
                        }
                }

                // Multi-step wizard state
                var step by remember { mutableStateOf(1) }
                val maxStep = 6
                var showTimePicker by remember { mutableStateOf(false) }
                var editingTimeIndex by remember { mutableStateOf(-1) }
                var showExpiryDatePicker by remember { mutableStateOf(false) }
                var showMonthDatePicker by remember { mutableStateOf(false) }
                // Shared times list (so it persists across steps)
                val times = remember { mutableStateListOf<String>() }
                // Initialize times from formState when available
                LaunchedEffect(formState.times) {
                        if (formState.times.isNotEmpty() && times.isEmpty()) {
                                times.clear()
                                times.addAll(formState.times)
                        }
                }
                // Keep times list or month-day selections in sync with frequencyCount
                LaunchedEffect(formState.frequencyCount, formState.timeUnit) {
                        if (formState.timeUnit == "วัน" && formState.frequencyCount > 0) {
                                val target = formState.frequencyCount
                                while (times.size < target) times.add("")
                                while (times.size > target) times.removeAt(times.lastIndex)
                                formState = formState.copy(times = times.toList())
                        } else if (formState.timeUnit == "เดือน" && formState.frequencyCount > 0) {
                                // ensure selected month days list matches frequencyCount
                                val target = formState.frequencyCount
                                val selected = formState.selectedMonthDays.toMutableList()
                                while (selected.size < target) selected.add(0)
                                while (selected.size > target) selected.removeAt(selected.lastIndex)
                                formState = formState.copy(selectedMonthDays = selected.toList())
                        }
                }

                // Auto-select time unit when user picks a category
                LaunchedEffect(formState.category) {
                        val mapping = mapOf(
                                "เม็ด" to "วัน",
                                "แคปซูล" to "วัน",
                                "ฉีด" to "วัน",
                                "อื่นๆ" to "วัน"
                        )
                        val mapped = mapping[formState.category] ?: formState.timeUnit
                        if (mapped != formState.timeUnit) {
                                formState = formState.copy(timeUnit = mapped)
                        }
                }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Scaffold(
                topBar = {
                        CenterAlignedTopAppBar(
                                title = { Text("เพิ่มยาใหม่", style = LuklanTypography.h3, color = LuklanTheme.colors.OnPrimary) },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateBack) {
                                                Icon(
                                                        MaterialIcons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back",
                                                        tint = LuklanTheme.colors.OnPrimary
                                                )
                                        }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = LuklanTheme.colors.Primary,
                                        titleContentColor = LuklanTheme.colors.OnPrimary,
                                        navigationIconContentColor = LuklanTheme.colors.OnPrimary
                                )
                        )
                },
                containerColor = LuklanTheme.colors.Primary
        ) { paddingValues ->
                Column(
                        modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = LuklanTheme.spacing.lg)
                ) {
                        // Step content
                        Spacer(Modifier.height(LuklanTheme.spacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                                when (step) {
                                        1 -> {
                                                // Name, amount per dose, total quantity
                                                Text("ชื่อยา", color = LuklanTheme.colors.Secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                                OutlinedTextField(
                                                        value = formState.name,
                                                        onValueChange = { formState = formState.copy(name = it) },
                                                        placeholder = { Text("กรอกชื่อยา", color = LuklanTheme.colors.TextSecondary) },
                                                        textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                                focusedContainerColor = LuklanTheme.colors.Surface,
                                                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                                                focusedBorderColor = Color.Black,
                                                                unfocusedBorderColor = Color.Black,
                                                                cursorColor = LuklanTheme.colors.TextPrimary
                                                        )
                                                )
                                                Spacer(Modifier.height(12.dp))
                                                Text("ปริมาณยาที่ใช้ต่อครั้ง", color = LuklanTheme.colors.Secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                                OutlinedTextField(
                                                        value = formState.amountPerDose,
                                                        onValueChange = { formState = formState.copy(amountPerDose = it) },
                                                        placeholder = { Text("กรอกปริมาณยาที่ใช้ต่อครั้ง", color = LuklanTheme.colors.TextSecondary) },
                                                        textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                                focusedContainerColor = LuklanTheme.colors.Surface,
                                                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                                                focusedBorderColor = Color.Black,
                                                                unfocusedBorderColor = Color.Black,
                                                                cursorColor = LuklanTheme.colors.TextPrimary
                                                        )
                                                )
                                                Spacer(Modifier.height(12.dp))
                                                Text("ปริมาณยาทั้งหมด", color = LuklanTheme.colors.Secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                                OutlinedTextField(
                                                        value = formState.quantity,
                                                        onValueChange = { formState = formState.copy(quantity = it) },
                                                        placeholder = { Text("กรอกปริมาณยาทั้งหมด", color = LuklanTheme.colors.TextSecondary) },
                                                        textStyle = TextStyle(color = LuklanTheme.colors.TextPrimary),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                                        keyboardActions = KeyboardActions(onNext = { step += 1; focusManager.clearFocus() }),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                                focusedContainerColor = LuklanTheme.colors.Surface,
                                                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                                                focusedBorderColor = Color.Black,
                                                                unfocusedBorderColor = Color.Black,
                                                                cursorColor = LuklanTheme.colors.TextPrimary
                                                        )
                                                )
                                        }
                                        2 -> {
                                                // ลักษณะของยา (ประเภทยา)
                                                val categoryOptions = listOf("เม็ด","แคปซูล","ฉีด","อื่นๆ")
                                                Text("ลักษณะของยา", color = LuklanTheme.colors.Secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                                DropdownSelector(
                                                        label = "ลักษณะของยา",
                                                        selectedValue = if (formState.category.isNotBlank()) formState.category else categoryOptions.first(),
                                                        options = categoryOptions,
                                                        onValueChange = { formState = formState.copy(category = it) },
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                        }
                                        3 -> {
                                                // จำนวนครั้งต่อ วัน/สัปดาห์/เดือน
                                                Text("จำนวนครั้งต่อ", color = LuklanTheme.colors.Secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                                OutlinedTextField(
                                                        value = formState.frequencyCount.takeIf { it>0 }?.toString() ?: "",
                                                        onValueChange = { v -> formState = formState.copy(frequencyCount = v.toIntOrNull() ?: 0) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                                focusedContainerColor = LuklanTheme.colors.Surface,
                                                                unfocusedContainerColor = LuklanTheme.colors.Surface,
                                                                focusedBorderColor = Color.Black,
                                                                unfocusedBorderColor = Color.Black,
                                                                cursorColor = Color.Black
                                                        )
                                                )
                                                Spacer(Modifier.height(12.dp))
                                                Text("หน่วยเวลา", color = LuklanTheme.colors.Secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                                DropdownSelector(
                                                        label = "หน่วยเวลา",
                                                        selectedValue = formState.timeUnit,
                                                        options = listOf("วัน","สัปดาห์","เดือน"),
                                                        onValueChange = { formState = formState.copy(timeUnit = it) },
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                if (formState.timeUnit == "สัปดาห์") {
                                                        Text("เลือกวันของสัปดาห์")
                                                        Spacer(Modifier.height(8.dp))
                                                        // Weekday chips Mon(1) .. Sun(7)
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                val weekLabels = listOf("จ","อ","พ","พฤ","ศ","ส","อา")
                                                                weekLabels.forEachIndexed { i, label ->
                                                                        val dayIndex = i + 1
                                                                        val selected = formState.selectedWeekDays.contains(dayIndex)
                                                                        OutlinedButton(
                                                                                onClick = {
                                                                                        val new = formState.selectedWeekDays.toMutableList()
                                                                                        if (selected) new.remove(dayIndex) else new.add(dayIndex)
                                                                                        formState = formState.copy(selectedWeekDays = new.sorted())
                                                                                },
                                                                                colors = ButtonDefaults.outlinedButtonColors(
                                                                                        containerColor = if (selected) LuklanTheme.colors.Surface else LuklanTheme.colors.Surface,
                                                                                        contentColor = if (selected) LuklanTheme.colors.Primary else LuklanTheme.colors.TextPrimary
                                                                                )
                                                                        ) { Text(label) }
                                                                }
                                                        }
                                                } else if (formState.timeUnit == "เดือน") {
                                                        Text("เลือกวันที่ของเดือน")
                                                        Spacer(Modifier.height(8.dp))
                                                        // Show selected month days and a button to pick more via DatePicker
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                val visible = formState.selectedMonthDays.filter { it > 0 }
                                                                if (visible.isNotEmpty()) {
                                                                        visible.forEach { d ->
                                                                                Surface(shape = RoundedCornerShape(16.dp), color = LuklanTheme.colors.Surface) {
                                                                                        Text(text = d.toString(), modifier = Modifier.padding(8.dp), color = LuklanTheme.colors.TextPrimary)
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                        Spacer(Modifier.height(8.dp))
                                                        Button(onClick = { showMonthDatePicker = true }, colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Surface, contentColor = LuklanTheme.colors.Primary)) { Text("เลือกรายการวันที่") }
                                                }
                                        }
                                        4 -> {
                                                                // เวลาใช้ยา - sync with จำนวนครั้งเมื่อเลือก "วัน"
                                                                Text("เวลาใช้ยา", color = LuklanTheme.colors.Secondary)
                                                                Spacer(Modifier.height(8.dp))
                                                                Column {
                                                                        if (formState.timeUnit == "วัน") {
                                                                                // show one field per frequencyCount, not editable add/remove
                                                                                times.forEachIndexed { idx, t ->
                                                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                                                                Surface(
                                                                                                        modifier = Modifier
                                                                                                                .weight(1f)
                                                                                                                .clickable { editingTimeIndex = idx; showTimePicker = true },
                                                                                                        shape = RoundedCornerShape(12.dp),
                                                                                                        color = LuklanTheme.colors.Surface,
                                                                                                        shadowElevation = 2.dp
                                                                                                ) {
                                                                                                        Text(
                                                                                                                text = if (t.isNotBlank()) t else "--:--",
                                                                                                                color = LuklanTheme.colors.TextPrimary,
                                                                                                                modifier = Modifier.padding(16.dp)
                                                                                                        )
                                                                                                }
                                                                                                Spacer(Modifier.width(8.dp))
                                                                                                Button(onClick = { editingTimeIndex = idx; showTimePicker = true }, colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Surface, contentColor = LuklanTheme.colors.Primary)) { Text("แก้ไข") }
                                                                                        }
                                                                                        Spacer(Modifier.height(8.dp))
                                                                                }
                                                                        } else {
                                                                                // non-daily: single primary time only
                                                                                val primary = times.firstOrNull() ?: ""
                                                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                                                        Surface(
                                                                                                modifier = Modifier
                                                                                                        .weight(1f)
                                                                                                        .clickable { editingTimeIndex = 0; showTimePicker = true },
                                                                                                shape = RoundedCornerShape(12.dp),
                                                                                                color = LuklanTheme.colors.Surface,
                                                                                                shadowElevation = 2.dp
                                                                                        ) {
                                                                                                Text(
                                                                                                        text = if (primary.isNotBlank()) primary else "--:--",
                                                                                                        color = LuklanTheme.colors.TextPrimary,
                                                                                                        modifier = Modifier.padding(16.dp)
                                                                                                )
                                                                                        }
                                                                                        Spacer(Modifier.width(8.dp))
                                                                                        Button(onClick = { editingTimeIndex = 0; showTimePicker = true }, colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Surface, contentColor = LuklanTheme.colors.Primary)) { Text("แก้ไข") }
                                                                                }
                                                                                Spacer(Modifier.height(8.dp))
                                                                        }
                                                                        LaunchedEffect(times.toList()) { formState = formState.copy(times = times.toList()) }
                                                                }
                                        }
                                        5 -> {
                                                // วันหมดอายุ
                                                Text("วันหมดอายุ", color = LuklanTheme.colors.Secondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                                OutlinedTextField(
                                                        value = formState.expiryDate,
                                                        onValueChange = { formState = formState.copy(expiryDate = it) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        readOnly = true
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Button(onClick = { showExpiryDatePicker = true }, colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Surface, contentColor = LuklanTheme.colors.Primary)) { Text("เลือกวันหมดอายุ") }
                                        }
                                        6 -> {
                                                // Review
                                                Text("ตรวจสอบข้อมูลก่อนบันทึก", style = LuklanTypography.h3)
                                                Spacer(Modifier.height(12.dp))
                                                Text("ชื่อยา: ${formState.name}")
                                                Text("ปริมาณต่อครั้ง: ${formState.amountPerDose}")
                                                Text("ปริมาณทั้งหมด: ${formState.quantity}")
                                                Text("ลักษณะ: ${formState.category}")
                                                Text("จำนวนครั้ง: ${formState.frequencyCount} ครั้งต่อ ${formState.timeUnit}")
                                                Text("เวลา: ${if (formState.times.isNotEmpty()) formState.times.joinToString(", ") else formState.time}")
                                                Text("วันหมดอายุ: ${formState.expiryDate}")
                                        }
                                }
                        }

                        // Time picker dialog
                        if (showTimePicker) {
                                var tempTime by remember { mutableStateOf(if (editingTimeIndex in times.indices && times[editingTimeIndex].isNotBlank()) times[editingTimeIndex] else "08:00") }
                                AlertDialog(
                                        onDismissRequest = { showTimePicker = false },
                                        confirmButton = {
                                                TextButton(onClick = {
                                                        if (editingTimeIndex >= 0 && editingTimeIndex in times.indices) {
                                                                times[editingTimeIndex] = tempTime
                                                        } else if (editingTimeIndex == -1) {
                                                                times.add(tempTime)
                                                        }
                                                        formState = formState.copy(times = times.toList())
                                                        showTimePicker = false
                                                }) { Text("ตกลง", color = LuklanTheme.colors.Primary) }
                                        },
                                        dismissButton = {
                                                TextButton(onClick = { showTimePicker = false }) { Text("ยกเลิก", color = LuklanTheme.colors.TextPrimary) }
                                        },
                                        text = {
                                                Column { WheelTimePicker(startTime = tempTime, onTimeSelected = { tempTime = it }) }
                                        },
                                        containerColor = LuklanTheme.colors.SurfaceVariant
                                )
                        }

                        // Date picker dialog for expiry
                        if (showExpiryDatePicker) {
                                DatePickerDialog(
                                        initialDate = formState.expiryDate,
                                        onDateSelected = {
                                                formState = formState.copy(expiryDate = it)
                                                showExpiryDatePicker = false
                                        },
                                        onDismiss = { showExpiryDatePicker = false }
                                )
                        }

                        // Date picker dialog for month-day selection
                        if (showMonthDatePicker) {
                                DatePickerDialog(
                                        initialDate = "",
                                        onDateSelected = { selected ->
                                                // selected is yyyy-MM-dd, extract day
                                                val parts = selected.split("-")
                                                val day = parts.lastOrNull()?.toIntOrNull()
                                                if (day != null) {
                                                        val new = formState.selectedMonthDays.toMutableList()
                                                        val placeholderIndex = new.indexOfFirst { it == 0 }
                                                        if (placeholderIndex >= 0) {
                                                                // replace first placeholder
                                                                if (!new.contains(day)) new[placeholderIndex] = day
                                                        } else {
                                                                if (!new.contains(day)) new.add(day)
                                                        }
                                                        formState = formState.copy(selectedMonthDays = new.sorted())
                                                }
                                                showMonthDatePicker = false
                                        },
                                        onDismiss = { showMonthDatePicker = false }
                                )
                        }

                        if (errorMessage != null) {
                                Text(
                                        text = errorMessage!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = LuklanTypography.bodySmall,
                                        modifier = Modifier.padding(bottom = LuklanTheme.spacing.sm)
                                )
                        }

                        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

                        // Bottom controls: Previous / Next / Save
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                if (step > 1) {
                                        OutlinedButton(onClick = { if (step>1) step -= 1 }, colors = ButtonDefaults.outlinedButtonColors(contentColor = LuklanTheme.colors.Primary, containerColor = LuklanTheme.colors.Surface)) { Text("ย้อนกลับ") }
                                } else Spacer(Modifier.width(8.dp))

                                if (step < maxStep) {
                                        Button(onClick = { step += 1 }, colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Surface, contentColor = LuklanTheme.colors.Primary)) { Text("ต่อไป") }
                                } else {
                                        Button(onClick = {
                                                val userId = authRepository.getCurrentUserId()
                                                if (userId == null) { errorMessage = "User not logged in"; return@Button }
                                                if (formState.name.isBlank()) { errorMessage = "กรุณากรอกชื่อยา"; return@Button }
                                                // ensure times count matches frequencyCount when unit is วัน
                                                val times = if (formState.times.isNotEmpty()) formState.times else listOf(formState.time).filter { it.isNotBlank() }
                                                if (formState.timeUnit == "วัน" && times.size != formState.frequencyCount) { errorMessage = "จำนวนเวลาต้องเท่ากับจำนวนครั้งต่อวัน"; return@Button }
                                                isLoading = true
                                                scope.launch {
                                                        val frequencyStr = if (formState.frequency.isNotBlank()) formState.frequency else if (formState.frequencyCount > 0) {
                                                                if (formState.timeUnit == "วัน") "วันละ ${formState.frequencyCount} ครั้ง" else "${formState.timeUnit}ละ ${formState.frequencyCount} ครั้ง"
                                                        } else formState.frequency
                                                        val medicine = Medicine(
                                                                id = Uuid.random().toString(),
                                                                name = formState.name,
                                                                description = "",
                                                                dosage = if (formState.amountPerDose.isNotBlank()) formState.amountPerDose else formState.dosage,
                                                                time = times.firstOrNull() ?: formState.time,
                                                                times = times,
                                                                frequency = frequencyStr,
                                                                timeUnit = formState.timeUnit,
                                                                frequencyCount = formState.frequencyCount,
                                                                amountPerDose = formState.amountPerDose,
                                                                quantity = formState.quantity.toIntOrNull() ?: 0,
                                                                unit = "",
                                                                expiryDate = formState.expiryDate,
                                                                category = formState.category,
                                                                storageInstructions = "",
                                                                notes = "",
                                                                userId = userId,
                                                                taken = false,
                                                                createdAt = Clock.System.now().toEpochMilliseconds()
                                                        )
                                                        medicineRepository.addMedicine(medicine)
                                                                .onSuccess {
                                                                        notificationScheduler.schedule(medicine)
                                                                        isLoading = false
                                                                        onNavigateBack()
                                                                }
                                                                .onFailure {
                                                                        isLoading = false
                                                                        errorMessage = "Failed to save medicine: ${it.message}"
                                                                }
                                                }
                                        }, colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Surface, contentColor = LuklanTheme.colors.Primary)) { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("บันทึก") }
                                }
                        }
                }
        }
}
