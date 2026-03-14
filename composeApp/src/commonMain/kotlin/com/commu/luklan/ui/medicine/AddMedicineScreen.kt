package com.commu.luklan.ui.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Composable
fun AddMedicineScreen(onNavigateBack: () -> Unit) {
        val medicineRepository = remember { getMedicineRepository() }
        val authRepository = remember { AuthRepository() }
        val notificationScheduler = remember { getNotificationScheduler() }
        val scope = rememberCoroutineScope()

        var formState by remember { mutableStateOf(MedicineFormState()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = LuklanTheme.spacing.lg)
                ) {
                        MedicineFormFields(
                                state = formState,
                                onStateChange = { formState = it },
                                modifier = Modifier.weight(1f)
                        )

                        if (errorMessage != null) {
                                Text(
                                        text = errorMessage!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = LuklanTypography.bodySmall,
                                        modifier = Modifier.padding(bottom = LuklanTheme.spacing.sm)
                                )
                        }

                        Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

                        Button(
                                onClick = {
                                        val userId = authRepository.getCurrentUserId()
                                        if (userId != null &&
                                                        formState.name.isNotBlank() &&
                                                        formState.time.isNotBlank()
                                        ) {
                                                isLoading = true
                                                scope.launch {
                                                    val medicine = Medicine(
                                                        id = Uuid.random().toString(),
                                                        name = formState.name,
                                                        description = formState.description,
                                                        dosage = formState.dosage,
                                                        time = formState.time,
                                                        frequency = formState.frequency,
                                                        quantity = formState.quantity.toIntOrNull() ?: 0,
                                                        unit = formState.unit,
                                                        expiryDate = formState.expiryDate,
                                                        category = formState.category,
                                                        storageInstructions = formState.storageInstructions,
                                                        notes = formState.notes,
                                                        userId = userId,
                                                        taken = false,
                                                        createdAt = Clock.System.now().toEpochMilliseconds()
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
                                                .height(LuklanTheme.dimensions.buttonLarge)
                                                .padding(bottom = LuklanTheme.spacing.md),
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
