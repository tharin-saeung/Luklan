package com.commu.luklan.ui.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch

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
        category = medicine.category,
        mealTiming = medicine.mealTiming,
        mealTimingMinutes = medicine.mealTimingMinutes,
        currentAmount = medicine.currentAmount,
        times = medicine.times
    )) }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = LuklanColors.Primary
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onNavigateBack(null) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.weight(1f))
                Text("แก้ไขข้อมูลยา", style = LuklanTypography.h1, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1.3f))
            }
            
            Spacer(Modifier.height(24.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    MedicineFormFields(state = state, onUpdate = { state = it })
                }
            }

            if (error != null) {
                Text(error!!, color = Color.Red, style = LuklanTypography.bodySmall, fontWeight = FontWeight.Bold)
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
                                startDate = state.startDate
                            )
                            medicineRepo.updateMedicine(up).onSuccess {
                                scheduler.cancel(medicine)
                                scheduler.schedule(up)
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
}
