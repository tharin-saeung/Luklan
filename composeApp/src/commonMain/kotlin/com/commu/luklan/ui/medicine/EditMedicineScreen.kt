package com.commu.luklan.ui.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicineScreen(medicine: Medicine, onNavigateBack: () -> Unit) {
    val medicineRepository = remember { getMedicineRepository() }
    val notificationScheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()

    var formState by remember {
        mutableStateOf(
            MedicineFormState(
                name = medicine.name,
                dosage = medicine.dosage,
                description = medicine.description,
                frequency = medicine.frequency,
                quantity = medicine.quantity.toString(),
                unit = medicine.unit,
                category = medicine.category,
                expiryDate = medicine.expiryDate,
                storageInstructions = medicine.storageInstructions,
                notes = medicine.notes,
                time = medicine.time
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("แก้ไขยา", style = LuklanTypography.h3) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LuklanTheme.colors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuklanTheme.colors.Background,
                    titleContentColor = LuklanTheme.colors.TextPrimary,
                    navigationIconContentColor = LuklanTheme.colors.TextPrimary
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
                    if (formState.name.isNotBlank() && formState.time.isNotBlank()) {
                        isLoading = true
                        scope.launch {
                            val updatedMedicine = medicine.copy(
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
                                notes = formState.notes
                            )
                            medicineRepository.updateMedicine(updatedMedicine)
                                .onSuccess {
                                    // Reschedule notification with updated info
                                    notificationScheduler.cancel(medicine)
                                    notificationScheduler.schedule(updatedMedicine)
                                    isLoading = false
                                    onNavigateBack()
                                }
                                .onFailure {
                                    isLoading = false
                                    errorMessage = "Failed to update medicine: ${it.message}"
                                }
                        }
                    } else {
                        errorMessage = "กรุณากรอกข้อมูลให้ครบถ้วน"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LuklanTheme.dimensions.buttonLarge)
                    .padding(bottom = LuklanTheme.spacing.md),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuklanTheme.colors.Primary
                ),
                enabled = !isLoading
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