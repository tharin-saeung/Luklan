package com.commu.luklan.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.commu.luklan.data.Medicine
import com.commu.luklan.data.getMedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

data class SummaryState(
    val adherencePercentage: Double = 0.0,
    val summaryMessage: String = "",
    val isLoading: Boolean = false,
    val totalLogs: Int = 0,
    val totalExpected: Int = 0,
    val error: String? = null,
    val medicines: List<Medicine> = emptyList()
)

class SummaryViewModel : ViewModel() {
    private val medicineRepository = getMedicineRepository()
    
    private val _state = MutableStateFlow(SummaryState())
    val state: StateFlow<SummaryState> = _state.asStateFlow()

    fun fetchMonthlyAdherence(userId: String, month: Int, year: Int) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            medicineRepository.getMedicines(userId).onSuccess { medicines ->
                calculateMonthlyAdherence(medicines, month, year)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun calculateMonthlyAdherence(medicines: List<Medicine>, month: Int, year: Int) {
        var totalExpectedDoses = 0
        var totalTakenDoses = 0

        val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
        val lastDayOfMonth = try {
            nextMonth.minus(DatePeriod(days = 1)).dayOfMonth
        } catch (e: Exception) {
            30
        }

        val nowMillis = com.commu.luklan.utils.getCurrentTimeMillis()
        val instant = Instant.fromEpochMilliseconds(nowMillis)
        val today = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        val lastDay = if (year == today.year && month == today.monthNumber) {
            minOf(today.dayOfMonth, lastDayOfMonth)
        } else if (year > today.year || (year == today.year && month > today.monthNumber)) {
            0
        } else {
            lastDayOfMonth
        }

        medicines.forEach { medicine ->
            for (i in 1..lastDay) {
                val checkDateStr = "${year}-${month.toString().padStart(2, '0')}-${i.toString().padStart(2, '0')}"
                
                if (medicine.startDate.isNotEmpty() && checkDateStr >= medicine.startDate) {
                    val expectedDosesForDay = medicine.times.size
                    totalExpectedDoses += expectedDosesForDay
                    
                    medicine.times.forEach { time ->
                        val key = "${checkDateStr}_$time"
                        if (medicine.takenHistory.containsKey(key)) {
                            totalTakenDoses++
                        }
                    }
                }
            }
        }

        val percentage = if (totalExpectedDoses > 0) {
            (totalTakenDoses.toDouble() / totalExpectedDoses) * 100
        } else {
            0.0
        }

        val message = when {
            totalExpectedDoses == 0 -> "ไม่มีข้อมูลยาที่ต้องใช้ในเดือนนี้"
            percentage >= 90.0 -> "คุณใช้ยาสม่ำเสมอมาก!\nมีวินัยในการใช้ยาดีเยี่ยม!"
            percentage >= 70.0 -> "คุณใช้ยาสม่ำเสมอค่อนข้างมาก\nรักษาความสม่ำเสมอต่อไปนะครับ"
            percentage >= 50.0 -> "พยายามใช้ยาให้สม่ำเสมอขึ้น\nโปรดตรวจสอบการใช้ยาเมื่อได้รับแจ้งเตือน"
            else -> "ควรปรับปรุงการใช้ยาให้สม่ำเสมอขึ้น\nโปรดแจ้งผู้ดูแลตรวจสอบการใช้ยา"
        }

        _state.update { 
            it.copy(
                adherencePercentage = percentage,
                summaryMessage = message,
                totalLogs = totalTakenDoses,
                totalExpected = totalExpectedDoses,
                isLoading = false,
                medicines = medicines
            ) 
        }
    }
}