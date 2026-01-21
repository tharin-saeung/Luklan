package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.ui.theme.LuklanTheme
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicineScreen(medicine: Medicine, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf(medicine.name) }
    var description by remember { mutableStateOf(medicine.description) }
    var time by remember { mutableStateOf(medicine.time) }
    var isLoading by remember { mutableStateOf(false) }

    val medicineRepository = remember { getMedicineRepository() }
    val scope = rememberCoroutineScope()

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
                    containerColor = LuklanTheme.colors.Background
                )
            )
        },
        containerColor = LuklanTheme.colors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = LuklanTheme.spacing.md)
        ) {
            Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ชื่อยา") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuklanTheme.colors.Surface,
                    unfocusedContainerColor = LuklanTheme.colors.Surface,
                    focusedBorderColor = LuklanTheme.colors.Primary,
                    unfocusedBorderColor = LuklanTheme.colors.Indicator
                )
            )

            Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("ปริมาณ (เช่น 1 เม็ด)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuklanTheme.colors.Surface,
                    unfocusedContainerColor = LuklanTheme.colors.Surface,
                    focusedBorderColor = LuklanTheme.colors.Primary,
                    unfocusedBorderColor = LuklanTheme.colors.Indicator
                )
            )

            Spacer(modifier = Modifier.height(LuklanTheme.spacing.md))

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("เวลาที่ต้องกิน") },
                placeholder = { Text("19:34") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusSmall),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuklanTheme.colors.Surface,
                    unfocusedContainerColor = LuklanTheme.colors.Surface,
                    focusedBorderColor = LuklanTheme.colors.Primary,
                    unfocusedBorderColor = LuklanTheme.colors.Indicator
                )
            )

            Spacer(modifier = Modifier.height(LuklanTheme.spacing.xl))

            Button(
                onClick = {
                    if (name.isNotBlank() && description.isNotBlank() && time.isNotBlank()) {
                        isLoading = true
                        scope.launch {
                            val updatedMedicine = medicine.copy(
                                name = name,
                                description = description,
                                time = time
                            )
                            medicineRepository.updateMedicine(updatedMedicine)
                                .onSuccess { onNavigateBack() }
                                .onFailure { isLoading = false }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(LuklanTheme.dimensions.buttonLarge),
                shape = RoundedCornerShape(LuklanTheme.dimensions.radiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = LuklanTheme.colors.Primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = LuklanTheme.colors.OnPrimary)
                } else {
                    Text("บันทึก", style = LuklanTypography.bodyLarge)
                }
            }
        }
    }
}