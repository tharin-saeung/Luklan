package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import com.commu.luklan.data.getNotificationScheduler
import com.commu.luklan.ui.theme.LuklanColors
import com.commu.luklan.ui.theme.LuklanTheme.LuklanTypography
import kotlinx.coroutines.launch
import com.commu.luklan.data.AppCache
import com.commu.luklan.data.getAuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicineScreen(medicine: Medicine, onNavigateBack: (Medicine?) -> Unit) {
    val medicineRepo = remember { getMedicineRepository() }
    val scheduler = remember { getNotificationScheduler() }
    val scope = rememberCoroutineScope()
    
    var state by remember { mutableStateOf(MedicineFormState(
        name = medicine.name,
        dosage = medicine.dosage,
        unit = medicine.unit,
        startDate = medicine.startDate,
        expiryDate = medicine.expiryDate,
        category = medicine.category,
        mealTiming = medicine.mealTiming,
        mealTimingMinutes = medicine.mealTimingMinutes,
        currentAmount = medicine.currentAmount,
        times = medicine.times,
        photoUrl = medicine.photoUrl,
        forgotTimes = medicine.forgotTimes,
        forgotDurationMinutes = medicine.forgotDurationMinutes
    )) }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LuklanColors.Primary
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onNavigateBack(null) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.weight(1f))
                Text("แก้ไขข้อมูลยา", style = LuklanTypography.h1, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    MedicineFormFields(state = state, userId = medicine.userId, onUpdate = { state = it })
                }
            }

            if (error != null) {
                Text(error!!, color = LuklanColors.Error, style = LuklanTypography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (state.name.isNotBlank()) {
                        isLoading = true
                        scope.launch {
                            val up = medicine.copy(
                                name = state.name,
                                dosage = state.dosage,
                                times = state.times,
                                unit = state.unit,
                                category = state.category,
                                mealTiming = state.mealTiming,
                                mealTimingMinutes = state.mealTimingMinutes,
                                currentAmount = state.currentAmount,
                                startDate = state.startDate,
                                expiryDate = state.expiryDate,
                                photoUrl = state.photoUrl,
                                forgotTimes = state.forgotTimes,
                                forgotDurationMinutes = state.forgotDurationMinutes
                            )
                            medicineRepo.updateMedicine(up).onSuccess {
                                val currentUserId = getAuthRepository().getCurrentUserId()
                                if (medicine.userId == currentUserId) {
                                    scheduler.cancel(medicine)
                                    scheduler.schedule(up)
                                }
                                
                                // Update Cache
                                val currentMedicines = AppCache.medicinesCache[medicine.userId]?.toMutableList() ?: mutableListOf()
                                val index = currentMedicines.indexOfFirst { it.id == medicine.id }
                                if (index != -1) {
                                    currentMedicines[index] = up
                                    AppCache.medicinesCache[medicine.userId] = currentMedicines
                                }

                                isLoading = false
                                onNavigateBack(up)
                            }.onFailure { 
                                isLoading = false
                                error = it.message 
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Secondary, contentColor = Color.White)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("บันทึกการแก้ไข", style = LuklanTypography.buttonLarge)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("ต้องการลบยานี้หรอครับ?", style = LuklanTypography.h3, fontWeight = FontWeight.Bold) },
            text = { Text("คุณต้องการลบยา \"${medicine.name}\" หรือไม่? เราจะหยุดส่งการแจ้งเตือนการใช้ยานี้ให้คุณ", style = LuklanTypography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            val currentUserId = getAuthRepository().getCurrentUserId()
                            if (medicine.userId == currentUserId) {
                                scheduler.cancel(medicine)
                            }
                            medicineRepo.deleteMedicine(medicine.id).onSuccess {
                                // Update Cache
                                val currentMedicines = AppCache.medicinesCache[medicine.userId]?.toMutableList() ?: mutableListOf()
                                currentMedicines.removeAll { it.id == medicine.id }
                                AppCache.medicinesCache[medicine.userId] = currentMedicines
                            }
                            onNavigateBack(null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuklanColors.Error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("ลบ", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("ยกเลิก", color = LuklanColors.TextSecondary) }
            }
        )
    }
}
