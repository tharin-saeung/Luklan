package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.components.DropdownSelector
import com.commu.luklan.ui.theme.LuklanColors
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
        mutableStateOf<MedicineFormState>(
            MedicineFormState(
                name = medicine.name,
                dosage = medicine.dosage,
                description = medicine.description,
                frequency = medicine.frequency,
                timeUnit = medicine.timeUnit,
                frequencyCount = medicine.frequencyCount,
                amountPerDose = medicine.amountPerDose,
                quantity = medicine.quantity.toString(),
                unit = medicine.unit,
                startDate = medicine.startDate,
                category = medicine.category,
                mealTiming = medicine.mealTiming,
                expiryDate = medicine.expiryDate,
                storageInstructions = medicine.storageInstructions,
                notes = medicine.notes,
                time = medicine.time,
                times = medicine.times
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("แก้ไขข้อมูลยา", style = LuklanTypography.h3, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LuklanColors.Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = LuklanColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Twin Curved Header Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        LuklanColors.Primary,
                        RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .offset(y = 25.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(4.dp, LuklanColors.Background, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 48.sp)
                }
            }

            Spacer(Modifier.height(40.dp))

            // Editable Summary Cards
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name & Dosage Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ชื่อยาและปริมาณ", style = LuklanTypography.h4, color = LuklanColors.Primary)
                        
                        OutlinedTextField(
                            value = formState.name,
                            onValueChange = { formState = formState.copy(name = it) },
                            placeholder = { Text("กรอกชื่อยา") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.amountPerDose,
                                onValueChange = { formState = formState.copy(amountPerDose = it) },
                                label = { Text("ปริมาณ") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = formState.unit,
                                onValueChange = { formState = formState.copy(unit = it) },
                                label = { Text("หน่วย") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        val categoryOptions = listOf("เม็ด", "แคปซูล", "ฉีด", "อื่นๆ")
                        DropdownSelector(
                            label = "ประเภทยา",
                            selectedValue = formState.category,
                            options = categoryOptions,
                            onValueChange = { formState = formState.copy(category = it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Schedule Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ตารางเวลา", style = LuklanTypography.h4, color = LuklanColors.Primary)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.frequencyCount.toString(),
                                onValueChange = { v -> formState = formState.copy(frequencyCount = v.toIntOrNull() ?: 0) },
                                label = { Text("จำนวนครั้ง") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text("ต่อ")
                            OutlinedTextField(
                                value = formState.timeUnit,
                                onValueChange = { formState = formState.copy(timeUnit = it) },
                                label = { Text("หน่วยเวลา") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        formState.times.forEachIndexed { index, t ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = LuklanColors.Primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("มื้อที่ ${index + 1}: $t", style = LuklanTypography.bodyLarge)
                            }
                        }
                    }
                }

                // Inventory & Expiry Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("สินค้าคงคลังและวันหมดอายุ", style = LuklanTypography.h4, color = LuklanColors.Primary)
                        
                        OutlinedTextField(
                            value = formState.quantity,
                            onValueChange = { formState = formState.copy(quantity = it) },
                            label = { Text("จำนวนคงเหลือทั้งหมด") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = formState.expiryDate,
                            onValueChange = { formState = formState.copy(expiryDate = it) },
                            label = { Text("วันหมดอายุ (ปปปป-ดด-วว)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = LuklanColors.Error,
                    style = LuklanTypography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Save Button (DesignSystem Style)
            Button(
                onClick = {
                    if (formState.name.isNotBlank()) {
                        isLoading = true
                        scope.launch {
                            val frequencyStr = if (formState.timeUnit == "วัน") "วันละ ${formState.frequencyCount} ครั้ง" else "${formState.timeUnit}ละ ${formState.frequencyCount} ครั้ง"
                            val updatedMedicine = medicine.copy(
                                name = formState.name,
                                description = formState.description,
                                dosage = formState.amountPerDose,
                                time = formState.times.firstOrNull() ?: formState.time,
                                times = formState.times,
                                frequency = frequencyStr,
                                timeUnit = formState.timeUnit,
                                frequencyCount = formState.frequencyCount,
                                amountPerDose = formState.amountPerDose,
                                quantity = formState.quantity.toIntOrNull() ?: 0,
                                unit = formState.unit,
                                startDate = formState.startDate,
                                expiryDate = formState.expiryDate,
                                category = formState.category,
                                mealTiming = formState.mealTiming,
                                storageInstructions = formState.storageInstructions,
                                notes = formState.notes
                            )
                            medicineRepository.updateMedicine(updatedMedicine)
                                .onSuccess {
                                    notificationScheduler.cancel(medicine)
                                    notificationScheduler.schedule(updatedMedicine)
                                    isLoading = false
                                    onNavigateBack()
                                }
                                .onFailure {
                                    isLoading = false
                                    errorMessage = "บันทึกไม่สำเร็จ: ${it.message}"
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Primary)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("บันทึกการแก้ไข", style = LuklanTypography.buttonLarge)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}