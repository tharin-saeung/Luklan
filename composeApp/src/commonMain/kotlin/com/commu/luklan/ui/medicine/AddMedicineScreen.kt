package com.commu.luklan.ui.medicine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.AuthRepository
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.components.WheelTimePicker
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.Clock as DateTimeClock    // ✅ ใช้ alias แทน
import com.commu.luklan.utils.getCurrentTimeMillis
import kotlin.time.ExperimentalTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalUuidApi::class)
@Composable
fun AddMedicineScreen(onNavigateBack: () -> Unit) {
        val medicineRepository = remember { getMedicineRepository() }
        val authRepository = remember { AuthRepository() }
        val notificationScheduler = remember { getNotificationScheduler() }
        val scope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current

        var medicineName by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("") }
        var showTimePicker by remember { mutableStateOf(false) }
        val timePickerState = rememberTimePickerState()

        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // ...

        if (showTimePicker) {
                var tempTime by remember {
                        mutableStateOf(
                                if (time.isNotBlank()) time
                                else {// ✅ ผสมผสาน kotlin.time และ kotlinx.datetime
                                    // ✅ ใช้ kotlin.time.Clock.System และ kotlinx.datetime.Instant
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
                                                time = tempTime
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

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("เพิ่มยาใหม่", style = LuklanTypography.h3) },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateBack) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = LuklanTheme.colors.Background,
                                                titleContentColor = LuklanTheme.colors.TextPrimary,
                                                navigationIconContentColor =
                                                        LuklanTheme.colors.TextPrimary
                                        )
                        )
                },
                containerColor = LuklanTheme.colors.Background
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(LuklanTheme.spacing.xl)
                ) {
                        OutlinedTextField(
                                value = medicineName,
                                onValueChange = { medicineName = it },
                                label = { Text("ชื่อยา") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                                colors =
                                        OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = LuklanTheme.colors.Surface,
                                                unfocusedContainerColor =
                                                        LuklanTheme.colors.Surface,
                                                focusedBorderColor =
                                                        LuklanTheme.colors.TextSecondary,
                                                unfocusedBorderColor = LuklanTheme.colors.Indicator
                                        )
                        )

                        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

                        OutlinedTextField(
                                value = dosage,
                                onValueChange = { dosage = it },
                                label = { Text("ปริมาณ (เช่น 1 เม็ด)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                                colors =
                                        OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = LuklanTheme.colors.Surface,
                                                unfocusedContainerColor =
                                                        LuklanTheme.colors.Surface,
                                                focusedBorderColor =
                                                        LuklanTheme.colors.TextSecondary,
                                                unfocusedBorderColor = LuklanTheme.colors.Indicator
                                        )
                        )

                        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

                        Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                        value = time,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("เวลาที่ต้องกิน") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape =
                                                RoundedCornerShape(
                                                        LuklanTheme.dimensions.radiusSmall
                                                ),
                                        trailingIcon = {
                                                Icon(
                                                        Icons.Default.AccessTime,
                                                        contentDescription = "Select Time"
                                                )
                                        },
                                        colors =
                                                OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor =
                                                                LuklanTheme.colors.Surface,
                                                        unfocusedContainerColor =
                                                                LuklanTheme.colors.Surface,
                                                        focusedBorderColor =
                                                                LuklanTheme.colors.TextSecondary,
                                                        unfocusedBorderColor =
                                                                LuklanTheme.colors.Indicator
                                                )
                                )
                                Box(
                                        modifier =
                                                Modifier.matchParentSize().clickable {
                                                        focusManager.clearFocus()
                                                        showTimePicker = true
                                                }
                                )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (errorMessage != null) {
                                Text(
                                        text = errorMessage!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = LuklanTypography.bodySmall,
                                        modifier = Modifier.padding(bottom = LuklanTheme.spacing.sm)
                                )
                        }

                        Button(
                                onClick = {
                                        val userId = authRepository.getCurrentUserId()
                                        if (userId != null &&
                                                        medicineName.isNotBlank() &&
                                                        time.isNotBlank()
                                        ) {
                                                isLoading = true
                                                scope.launch {
                                                    val medicine = Medicine(
                                                        name = medicineName,
                                                        id = Uuid.random().toString(), // UUID (Best practice for unique ID generation)
                                                        description = dosage,
                                                        time = time,
                                                        userId = userId,
                                                        taken = false
                                                    )
                                                    medicineRepository
                                                        .addMedicine(medicine)
                                                        .onSuccess {
                                                            notificationScheduler
                                                                .schedule(medicine)
                                                            isLoading = false
                                                            onNavigateBack()
                                                        }
                                                        .onFailure {
                                                            isLoading = false
                                                            errorMessage =
                                                                "Failed to save medicine: ${it.message}"
                                                        }
                                                }
                                        } else if (userId == null) {
                                                errorMessage = "User not logged in"
                                        } else {
                                                errorMessage = "กรุณากรอกข้อมูลให้ครบถ้วน"
                                        }
                                },
                                enabled = !isLoading,
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(LuklanTheme.dimensions.buttonLarge),
                                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusLarge),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = LuklanTheme.colors.Primary
                                        )
                        ) {
                                if (isLoading) {
                                        CircularProgressIndicator(
                                                color = LuklanTheme.colors.OnPrimary,
                                                modifier = Modifier.size(24.dp)
                                        )
                                } else {
                                        Text(
                                                text = "บันทึก",
                                                style = LuklanTypography.buttonLarge,
                                                color = LuklanTheme.colors.OnPrimary
                                        )
                                }
                        }
                }
        }
}
